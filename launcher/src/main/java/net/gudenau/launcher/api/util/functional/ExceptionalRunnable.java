package net.gudenau.launcher.api.util.functional;

/**
 * An *exceptional* {@link Runnable}.
 * <p>
 * A basic {@link Runnable} that can throw an exception when run.
 *
 * @param <E> The type of the exception
 */
@FunctionalInterface
public interface ExceptionalRunnable<E extends Throwable> extends Runnable {
    void runExceptionally() throws E;
    
    @Override
    default void run() {
        try {
            runExceptionally();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to execute " + this, e);
        }
    }
}
