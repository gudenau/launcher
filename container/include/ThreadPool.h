#pragma once

#include <pthread.h>

/**
 * The callback of a submitted job.
 */
typedef void (*Runnable)(void* user);

/**
 * An internal data structure to the thread pool used to track submitted jobs.
 */
struct ThreadPoolEntry;

/**
 * A basic thread pool implementation.
 */
class ThreadPool {
public:
    /**
     * Creates a new pool with poolSize threads and queueSize space for waiting jobs.
     *
     * @param poolSize The thread count
     * @param queueSize The maximum queue size
     */
    ThreadPool(int poolSize, int queueSize);
    /**
     * Prevents job submission and waits for all pending jobs to finish.
     */
    ~ThreadPool(void);

    /**
     * Submits a new job, blocks until the queue has room to accept a new job.
     *
     * @param action The job callback
     * @param user Data passed to the callback
     */
    void submit(Runnable action, void* user);

private:
    /**
     * Thread handles for this pool.
     */
    pthread_t* threads;

    /**
     * The job queue for this pool.
     */
    ThreadPoolEntry* queue;

    /**
     * The mutex to ensure only one thread mutates shared state at a time.
     */
    pthread_mutex_t mutex;
    /**
     * The condition used to notify worker threads that new work is ready.
     */
    pthread_cond_t threadCond;
    /**
     * The condition used to notify any waiting submits for the queue to have room.
     */
    pthread_cond_t queueCond;

    /**
     * True if the pool is accepting new jobs.
     */
    bool running;

    /**
     * The thread count for this pool.
     */
    int poolSize;

    /**
     * The max size of the pool queue.
     */
    int queueSize;
    /**
     * The head of the queue.
     */
    int queueHead;
    /**
     * The tail of the queue.
     */
    int queueTail;

    /**
     * The callback for the thread pool workers.
     */
    void handler(void);
};
