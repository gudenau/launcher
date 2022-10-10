#include <cstring>
#include <cerrno>
#include <cstdlib>
#include <csignal>

#include <sched.h>
#include <unistd.h>
#include <dlfcn.h>
#include <linux/sched.h>
#include <sys/syscall.h>
#include <sys/types.h>
#include <sys/prctl.h>

#include <jni.h>

#include "logger.h"
#include "Communication.h"
#include "File.h"
#include "ThreadPool.h"

/**
 * The callback used for running code on the "child" process after a fork.
 *
 * @param readPipe The read handle to a IPC pipe
 * @param writePipe The write handle to a IPC pipe
 * @return The exit code to use when terminating
 */
typedef int (*ChildCallback)(int readPipe, int writePipe);

/**
 * The callback used for running code on the "parent" process after a fork.

 * @param readPipe The read handle to a IPC pipe
 * @param writePipe The write handle to a IPC pipe
 * @return The exit code to use when terminating
 */
typedef int (*ParentCallback)(pid_t pid, int readPipe, int writePipe);

/**
 * A somewhat complex function, sets up a namespace for a pseudo-root account, forks the current process, sets up more
 * namespaces and passes execution to the two callbacks.
 *
 * @param parent The parent callback
 * @param child The child callback
 */
static void setupNamespace(ParentCallback parent, ChildCallback child) {
    ASSERT(!parent, "parent was null");
    ASSERT(!child, "child was null");

    // Get the current UID and GID for later use, after creating the user namespace we can't get the real ones.
    auto userId = getuid();
    auto groupId = getgid();

    // Pretend to be root, this grants capabilities we need for proper namespaces.
    ASSERT(unshare(CLONE_NEWUSER), "Failed to create new user namespace: %s", strerror(errno));

    // Write the UID map for the current namespace, this makes our UID 0 look like the external UID.
    auto file = new File("/proc/self/uid_map");
    file->print("0 %d 1\n", userId);
    delete file;

    // Prevent GID switching, unfortunately required to set the GID map.
    file = new File("/proc/self/setgroups");
    file->print("deny\n");
    delete file;

    // Write the UID map for the current namespace, this makes our GID 0 look like the external GID.
    file = new File("/proc/self/gid_map");
    file->print("0 %d 1\n", groupId);
    delete file;

    // Create the IPC pipes.
    int pipeA[2];
    ASSERT(pipe(pipeA), "Failed to create pipeA: %s", strerror(errno));

    int pipeB[2];
    ASSERT(pipe(pipeB), "Failed to create pipeB: %s", strerror(errno));

    // Clone the process with the new namespaces setup, we can do this because of the fake root from before.
    clone_args cloneArgs {};
    cloneArgs.flags =
        // New namespaces
        CLONE_NEWNS | CLONE_NEWPID | CLONE_NEWIPC | CLONE_NEWNET | /* FIXME This breaks X, figure out a fix. CLONE_NEWUTS | */
        // Ensure the child has the default signal handlers
        CLONE_CLEAR_SIGHAND;
    cloneArgs.exit_signal = SIGCHLD;

    auto result = syscall(SYS_clone3, &cloneArgs, sizeof(cloneArgs));
    if(result == -1) {
        FATAL("Failed to clone: %s\n", strerror(errno));
        abort();
    }

    // Parent gets the PID, child gets 0
    if(result) {
        // Set up a signal handler to kill the parent when the child dies.
        signal(SIGCHLD, [](int signal)->void{
            (void)signal;
            exit(0);
        });

        // Close the wrong end of the IPC pipe
        close(pipeB[0]);
        close(pipeA[1]);
        exit(parent((pid_t) result, pipeA[0], pipeB[1]));
    } else {
        if(prctl(PR_SET_PDEATHSIG, SIGUSR1)) {
            FATAL("Failed to set parent death signal");
        }
        // Set up a signal handler to kill the child when the parent dies.
        signal(SIGUSR1, [](int signal)->void{
            (void)signal;
            exit(0);
        });

        // Close the wrong end of the IPC pipe
        close(pipeA[0]);
        close(pipeB[1]);
        exit(child(pipeB[0], pipeA[1]));
    }
}

/**
 * Launches the JVM used for the Java side of the launcher.
 *
 * @param readPipe The IPC read handle
 * @param writePipe The IPC write handle
 * @return The exit code of this process
 */
static int launchJvm(int readPipe, int writePipe) {
    //TODO Make this download the JVM and use the shared object from that.
    auto handle  = dlopen("/lib/jvm/default/lib/server/libjvm.so", RTLD_NOW);
    if(!handle) {
        FATAL("Failed to open libjvm.so: %s\n", dlerror());
        abort();
    }

    auto JNI_CreateJavaVM = (jint (*)(JavaVM **pvm, void **penv, void *args))dlsym(handle, "JNI_CreateJavaVM");
    if(!JNI_CreateJavaVM) {
        FATAL("Failed to find symbol JNI_CreateJavaVM: %s\n", dlerror());
        abort();
    }

    // TODO More intelligent JVM options, download the stub and use the downloaded path
    auto options = new JavaVMOption[2];
    options[0].optionString = (char*) "-Djava.class.path=/home/gudenau/projects/cpp/GameLauncher/launcher/modules/stub/build/libs/stub-1.0.0.jar";
    options[0].extraInfo = nullptr;
    options[1].optionString = (char*) "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005";
    options[1].extraInfo = nullptr;

    JavaVMInitArgs vmArguments = {};
    vmArguments.version = JNI_VERSION_19;
    vmArguments.nOptions = 2;
    vmArguments.options = options;
    vmArguments.ignoreUnrecognized = false;

    JavaVM* jvm;
    JNIEnv* environment;
    ASSERT(JNI_CreateJavaVM(&jvm, (void**) &environment, &vmArguments), "Failed to create JVM");
    delete[] options;

    // Find the stub class, the main method and run it.
    auto stubClass = environment->FindClass("net/gudenau/launcher/stub/Stub");
    if(!stubClass) {
        FATAL("Failed to find stub class");
        abort();
    }

    auto stubMethod = environment->GetStaticMethodID(stubClass, "init", "(II)V");
    if(!stubMethod) {
        FATAL("Failed to find stub entrypoint");
        abort();
    }

    environment->CallStaticVoidMethod(stubClass, stubMethod, readPipe, writePipe);

    // Verify that the exit was clean.
    if(environment->ExceptionCheck()) {
        FATAL("Failed to run stub");
        environment->ExceptionDescribe();
        abort();
    }

    // Calling this ensures that the early main return of the launcher doesn't terminate the entire JVM. This will wait
    // for all non-daemon threads to exit.
    jvm->DestroyJavaVM();

    return 0;
}

extern "C" int main(int argc, char** argv){
    //TODO Argument parsing
    (void)argc;
    (void)argv;

    setupNamespace([](pid_t pid, int readPipe, int writePipe)->int{
        (void) pid;

        // Set up the IPC system and its thread pool.
        // TODO Allow setting custom pool settings, maybe base the default off of host capabilities.
        auto comms = new Communication(readPipe, writePipe);
        auto pool = new ThreadPool(4, 16);
        struct Data {
            Communication* comms;
            Packet* packet;
        };

        for(;;) {
            auto packet = comms->readPacket();
            auto data = (Data*) calloc(1, sizeof(Data));
            ASSERT(!data, "Failed to allocate thread pool task state");
            data->comms = comms;
            data->packet = packet;

            pool->submit([](void* user)->void{
                auto data = (Data*) user;
                auto comms = data->comms;
                auto packet = data->packet;

                switch(packet->id()) {
                    case PacketId_ContainerVersion: {
                        // This pack writes static data and never reads, this is okay.
                        comms->writePacket(packet);
                    } break;
                }

                delete packet;
                delete data;
            }, data);
        }
    }, [](int readPipe, int writePipe)->int{
        //TODO Pick better names
        //sethostname("Launcher", 8);
        //setdomainname("Launcher", 8);

        return launchJvm(readPipe, writePipe);
    });

    /* TODO Mount namespace stuff.
     *  mkdir("./root");
     *  mount(nullptr, "./root", "tmpfs", MS_NOEXEC, "size=1M");
     *  mkdir("./oldRoot");
     *  pivot_root("./root", "./root/oldRoot");
     *  chdir("/");
     *  umount2("/oldRoot", MNT_DETACH);
     *  rmdir("/oldRoot");
     */

    return 0;
}
