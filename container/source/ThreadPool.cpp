#include <cstdlib>

#include <pthread.h>

#include "logger.h"

#include "ThreadPool.h"

struct ThreadPoolEntry {
    Runnable task;
    void* data;
};

// The lint lies, mutex, {thread, pool}Cond are initialized.
ThreadPool::ThreadPool(int poolSize, int queueSize) { // NOLINT(cppcoreguidelines-pro-type-member-init)
    this->running = true;

    this->poolSize = poolSize;

    this->queueSize = queueSize;
    this->queueHead = 0;
    this->queueTail = 0;

    this->threads = (pthread_t*) calloc(poolSize, sizeof(pthread_t));
    ASSERT(!this->threads, "Failed to allocate ThreadPool::threads");
    this->queue = (ThreadPoolEntry*) calloc(queueSize, sizeof(ThreadPoolEntry));
    ASSERT(!this->queue, "Failed to allocate ThreadPool::queue");

    ASSERT(pthread_mutex_init(&this->mutex, nullptr), "Failed to initialize ThreadPool::readMutex");
    ASSERT(pthread_cond_init(&this->threadCond, nullptr), "Failed to initialize ThreadPool::threadCond");
    ASSERT(pthread_cond_init(&this->queueCond, nullptr), "Failed to initialize ThreadPool::queueCond");

    for(int i = 0; i < poolSize; i++) {
        ASSERT(pthread_create(
            &this->threads[i], nullptr, [](void* user) -> void* {
                ((ThreadPool*) user)->handler();
                return nullptr;
            },
            this
        ), "Failed to create ThreadPool thread %d", i);
    }
}

ThreadPool::~ThreadPool(void) {
    // The lock is important here, we have no idea what other threads are doing.
    ASSERT(pthread_mutex_lock(&this->mutex), "Failed to lock readMutex in ThreadPool::~ThreadPool");
    this->running = false;
    ASSERT(pthread_cond_broadcast(&this->threadCond), "Failed to broadcast threadCond in ThreadPool::~ThreadPool");
    ASSERT(pthread_mutex_unlock(&this->mutex), "Failed to unlock readMutex in ThreadPool::~ThreadPool");

    for(int i = 0; i < this->poolSize; i++) {
        ASSERT(pthread_join(threads[i], nullptr), "Failed to join thread %d in ThreadPool::~ThreadPool", i);
    }

    ASSERT(pthread_cond_destroy(&this->queueCond), "Failed to destroy ThreadPool::queueCond");
    ASSERT(pthread_cond_destroy(&this->threadCond), "Failed to destroy ThreadPool::threadCond");
    ASSERT(pthread_mutex_destroy(&this->mutex), "Failed to destroy ThreadPool::readMutex");

    free(this->queue);
    free(this->threads);
}

void ThreadPool::submit(Runnable action, void* user) {
    ASSERT(pthread_mutex_lock(&this->mutex), "Failed to lock readMutex in ThreadPool::submit");
    ASSERT(!this->running, "ThreadPool::submit called after ThreadPool::~ThreadPool");
    while(this->queueHead == (this->queueTail + 1) % this->queueSize) {
        ASSERT(pthread_cond_wait(&this->queueCond, &this->mutex), "Failed to wait on ThreadPool::queueCond");
    }
    auto entry = &this->queue[this->queueHead];
    entry->task = action;
    entry->data = user;
    this->queueHead = (this->queueHead + 1) % this->queueSize;
    ASSERT(pthread_cond_signal(&this->threadCond), "Failed to signal threadCond in ThreadPool::submit");
    ASSERT(pthread_mutex_unlock(&this->mutex), "Failed to unlock readMutex in ThreadPool::submit");
}

void ThreadPool::handler(void) {
    for(;;) {
        Runnable task;
        void* user;
        ASSERT(pthread_mutex_lock(&this->mutex), "Failed to lock readMutex in ThreadPool::handler");
        while(this->queueHead == this->queueTail) {
            if(!this->running) {
                ASSERT(pthread_mutex_unlock(&this->mutex), "Failed to unlock readMutex in ThreadPool::handler");
                return;
            }
            ASSERT(pthread_cond_wait(&this->threadCond, &this->mutex), "Failed to wait on threadCond in ThreadPool::handler");
        }
        auto entry = &this->queue[queueTail];
        task = entry->task;
        user = entry->data;
        entry->task = nullptr;
        entry->data = nullptr;
        queueTail = (queueTail + 1) % queueSize;
        ASSERT(pthread_cond_signal(&this->queueCond), "Failed to signal ThreadPool::queueCond");
        ASSERT(pthread_mutex_unlock(&this->mutex), "Failed to unlock readMutex in ThreadPool::handler");
        (*task)(user);
    }
}
