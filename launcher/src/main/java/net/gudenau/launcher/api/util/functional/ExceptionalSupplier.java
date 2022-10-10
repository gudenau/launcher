package net.gudenau.launcher.api.util.functional;

import java.util.function.Supplier;

/**
 * An *exceptional* {@link Supplier}.
 * <p>
 * A basic {@link Runnable} that can throw an exception when supplying a value.
 *
 * @param <T> The type of the value
 * @param <E> The type of the exception
 */
@FunctionalInterface
public interface ExceptionalSupplier<T, E extends Throwable> extends Supplier<T> {
    T getExceptionally() throws E;
    
    @Override
    default T get() {
        try {
            return getExceptionally();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to get " + this, e);
        }
    }
}
