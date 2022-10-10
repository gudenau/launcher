package net.gudenau.launcher.api.util;

import net.gudenau.launcher.api.util.functional.ExceptionalRunnable;
import net.gudenau.launcher.api.util.functional.ExceptionalSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * A basic shared lock helper.
 */
public final class SharedLock {
    /**
     * The {@link ReadWriteLock} used for shared locking.
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    /**
     * The {@link Lock} used for read accesses.
     */
    private final Lock read = lock.readLock();
    /**
     * The {@link Lock} used for write accesses.
     */
    private final Lock write = lock.writeLock();
    
    /**
     * Performs an action with the read {@link Lock} obtained.
     *
     * @param action The action to perform
     */
    public void read(@NotNull Runnable action) {
        read.lock();
        try {
            action.run();
        } finally {
            read.unlock();
        }
    }
    
    /**
     * Performs an action with the read {@link Lock} obtained.
     *
     * @param action The action to perform
     * @return The result of the action
     */
    public <T> T read(Supplier<T> action) {
        read.lock();
        try {
            return action.get();
        } finally {
            read.unlock();
        }
    }
    
    /**
     * Performs an action with the read {@link Lock} obtained.
     *
     * @param action The action to perform
     */
    public <E extends Throwable> void exceptionalRead(ExceptionalRunnable<E> action) throws E {
        read.lock();
        try {
            action.runExceptionally();
        } finally {
            read.unlock();
        }
    }
    
    /**
     * Performs an action with the read {@link Lock} obtained.
     *
     * @param action The action to perform
     * @return The result of the action
     */
    public <T, E extends Throwable> T exceptionalRead(ExceptionalSupplier<T, E> action) throws E {
        read.lock();
        try {
            return action.getExceptionally();
        } finally {
            read.unlock();
        }
    }
    
    /**
     * Performs an action with the write {@link Lock} obtained.
     *
     * @param action The action to perform
     */
    public void write(Runnable action) {
        write.lock();
        try {
            action.run();
        } finally {
            write.unlock();
        }
    }
    
    /**
     * Performs an action with the write {@link Lock} obtained.
     *
     * @param action The action to perform
     * @return The result of the action
     */
    public <T> T write(Supplier<T> action) {
        write.lock();
        try {
            return action.get();
        } finally {
            write.unlock();
        }
    }
    
    /**
     * Performs an action with the write {@link Lock} obtained.
     *
     * @param action The action to perform
     */
    public <E extends Throwable> void exceptionalWrite(ExceptionalRunnable<E> action) throws E {
        write.lock();
        try {
            action.runExceptionally();
        } finally {
            write.unlock();
        }
    }
    
    /**
     * Performs an action with the write {@link Lock} obtained.
     *
     * @param action The action to perform
     * @return The result of the action
     */
    public <T, E extends Throwable> T exceptionalWrite(ExceptionalSupplier<T, E> action) throws E {
        write.lock();
        try {
            return action.getExceptionally();
        } finally {
            write.unlock();
        }
    }
}
