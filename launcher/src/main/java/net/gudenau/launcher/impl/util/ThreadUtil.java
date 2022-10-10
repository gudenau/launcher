package net.gudenau.launcher.impl.util;

import net.gudenau.launcher.api.util.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ThreadUtil {
    private static final Logger LOGGER = Logger.forName("launcher");
    private static final BlockingQueue<Runnable> TASKS = new LinkedBlockingQueue<>();
    private static final Executor EXECUTOR = ThreadUtil::enqueue;
    private static final int THREAD_COUNT = Configuration.THREAD_COUNT.get();
    private static volatile boolean shouldRun = true;
    private static Thread[] THREADS;
    
    static {
        Runnable handler = ThreadUtil::thread;
        var group = new ThreadGroup("Workers");
        group.setMaxPriority(Thread.NORM_PRIORITY - 1);
    
        THREADS = new Thread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            var thread = new Thread(group, handler, "Worker" + i);
            thread.setDaemon(true);
            thread.start();
        }
    }
    
    private static void enqueue(Runnable job) {
        try {
            TASKS.put(job);
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to enqueue job: " + job, e);
        }
    }
    
    private static void thread() {
        while (shouldRun) {
            Runnable task;
            try {
                task = TASKS.take();
            } catch (InterruptedException e) {
                LOGGER.error(e, "Failed to take job");
                continue;
            }
            try {
                task.run();
            } catch (Throwable t) {
                throw new RuntimeException("Failed to execute task: " + task, t);
            }
        }
    }
    
    @NotNull
    public static CompletableFuture<Void> submit(@NotNull Runnable task) {
        return CompletableFuture.runAsync(task, EXECUTOR);
    }
    
    @NotNull
    public static <T> CompletableFuture<T> submit(@NotNull Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, EXECUTOR);
    }
    
    public static void waitFor(Collection<Runnable> jobs) {
        try {
            CompletableFuture.allOf(jobs.stream()
                .map(ThreadUtil::submit)
                .toArray(CompletableFuture[]::new)
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(
                "Failed to execute jobs: " +
                jobs.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(",", "[", "]")),
                e
            );
        }
    }
    
    public static void waitFor(Runnable... jobs) {
        try {
            CompletableFuture.allOf(Stream.of(jobs)
                .map(ThreadUtil::submit)
                .toArray(CompletableFuture[]::new)
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to execute jobs: " + Arrays.toString(jobs), e);
        }
    }
    
    private static final Executor UI_EXECUTOR = (task) -> {
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }
    };
    
    @NotNull
    public static CompletableFuture<Void> submitUi(@NotNull Runnable task) {
        return CompletableFuture.runAsync(task, UI_EXECUTOR);
    }
    
    @NotNull
    public static <T> CompletableFuture<T> submitUi(@NotNull Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, UI_EXECUTOR);
    }
    
    public static void waitForUi(@NotNull Runnable task) {
        try {
            submitUi(task).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to wait for UI task: " + task, e);
        }
    }
    
    public static <T> T waitForUi(@NotNull Supplier<T> task) {
        try {
            return submitUi(task).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to wait for UI task: " + task, e);
        }
    }
}
