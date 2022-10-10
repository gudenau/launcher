package net.gudenau.launcher.api.util;

import net.gudenau.launcher.impl.util.LoggerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A basic logging framework. Messages will be swallowed if lower than the configured verbosity.
 */
public sealed interface Logger permits LoggerImpl {
    /**
     * Creates or gets a new {@link Logger} instance.
     *
     * @param name The name of the {@link Logger}
     * @return The {@link Logger} instance
     */
    @NotNull
    static Logger forName(@NotNull String name) {
        Objects.requireNonNull(name, "name can't be null");
        return LoggerImpl.forName(name);
    }
    
    /**
     * Logs a message to the console.
     *
     * @param level The level of the message
     * @param exception The exception that caused the message, if present
     * @param message The message itself
     */
    void log(@NotNull LogLevel level, @Nullable Throwable exception, @NotNull CharSequence message);
    
    /**
     * Logs a message to the console.
     *
     * @param level The level of the message
     * @param message The message itself
     */
    default void log(@NotNull LogLevel level, @NotNull CharSequence message) {
        log(level, null, message);
    }
    
    /**
     * Logs a message to the console.
     *
     * @param level The level of the message
     * @param format The format string
     * @param args The arguments for the format string
     */
    default void log(@NotNull LogLevel level, @NotNull String format, Object... args) {
        log(level, null, format, args);
    }
    
    /**
     * Logs a message to the console.
     *
     * @param level The level of the message
     * @param exception The exception that caused the message, if present
     * @param format The format string
     * @param args The arguments for the format string
     */
    default void log(@NotNull LogLevel level, @Nullable Throwable exception, @NotNull String format, Object... args) {
        if(exception == null && args.length >= 1 && args[0] instanceof Throwable arg) {
            warning(new Throwable(), "Exception accidentally passed as format argument");
            log(level, arg, format);
        } else {
            log(level, exception, format.formatted(args));
        }
    }
    
    /**
     * Logs a {@link LogLevel#FATAL} message to the console.
     *
     * @param exception The exception that caused the message, if present
     * @param message The message itself
     */
    default void fatal(@Nullable Throwable exception, @NotNull CharSequence message) {
        log(LogLevel.FATAL, exception, message);
    }
    
    /**
     * Logs a {@link LogLevel#FATAL} message to the console.
     *
     * @param message The message itself
     */
    default void fatal(@NotNull CharSequence message) {
        log(LogLevel.FATAL, null, message);
    }
    
    /**
     * Logs a {@link LogLevel#FATAL} message to the console.
     *
     * @param format The format string
     * @param args The arguments for the format string
     */
    default void fatal(@NotNull String format, Object... args) {
        log(LogLevel.FATAL, null, format, args);
    }
    
    /**
     * Logs a {@link LogLevel#FATAL} message to the console.
     *
     * @param exception The exception that caused the message, if present
     * @param format The format string
     * @param args The arguments for the format string
     */
    default void fatal(@Nullable Throwable exception, @NotNull String format, Object... args) {
        log(LogLevel.FATAL, exception, format, args);
    }
    
    /**
     * Logs a {@link LogLevel#ERROR} message to the console.
     *
     * @param exception The exception that caused the message, if present
     * @param message The message itself
     */
    default void error(@Nullable Throwable exception, @NotNull CharSequence message) {
        log(LogLevel.ERROR, exception, message);
    }
    
    /**
     * Logs a {@link LogLevel#ERROR} message to the console.
     *
     * @param message The message itself
     */
    default void error(@NotNull CharSequence message) {
        log(LogLevel.ERROR, null, message);
    }
    
    /**
     * Logs a {@link LogLevel#ERROR} message to the console.
     *
     * @param format The format string
     * @param args The arguments for the format string
     */
    default void error(@NotNull String format, Object... args) {
        log(LogLevel.ERROR, null, format, args);
    }
    
    /**
     * Logs a {@link LogLevel#ERROR} message to the console.
     *
     * @param exception The exception that caused the message, if present
     * @param format The format string
     * @param args The arguments for the format string
     */
    default void error(@Nullable Throwable exception, @NotNull String format, Object... args) {
        log(LogLevel.ERROR, exception, format, args);
    }
    
    /**
     * Logs a {@link LogLevel#WARNING} message to the console.
     *
     * @param exception The exception that caused the message, if present
     * @param message The message itself
     */
    default void warning(@Nullable Throwable exception, @NotNull CharSequence message) {
        log(LogLevel.WARNING, exception, message);
    }
    
    /**
     * Logs a {@link LogLevel#WARNING} message to the console.
     *
     * @param message The message itself
     */
    default void warning(@NotNull CharSequence message) {
        log(LogLevel.WARNING, null, message);
    }
    
    /**
     * Logs a {@link LogLevel#WARNING} message to the console.
     *
     * @param format The format string
     * @param args The arguments for the format string
     */
    default void warning(@NotNull String format, Object... args) {
        log(LogLevel.WARNING, null, format, args);
    }
    
    /**
     * Logs a {@link LogLevel#WARNING} message to the console.
     *
     * @param exception The exception that caused the message, if present
     * @param format The format string
     * @param args The arguments for the format string
     */
    default void warning(@Nullable Throwable exception, @NotNull String format, Object... args) {
        log(LogLevel.WARNING, exception, format, args);
    }
    
    /**
     * Logs a {@link LogLevel#INFO} message to the console.
     *
     * @param exception The exception that caused the message, if present
     * @param message The message itself
     */
    default void info(@Nullable Throwable exception, @NotNull CharSequence message) {
        log(LogLevel.INFO, exception, message);
    }
    
    /**
     * Logs a {@link LogLevel#INFO} message to the console.
     *
     * @param message The message itself
     */
    default void info(@NotNull CharSequence message) {
        log(LogLevel.INFO, null, message);
    }
    
    /**
     * Logs a {@link LogLevel#INFO} message to the console.
     *
     * @param format The format string
     * @param args The arguments for the format string
     */
    default void info(@NotNull String format, Object... args) {
        log(LogLevel.INFO, null, format, args);
    }
    
    /**
     * Logs a {@link LogLevel#INFO} message to the console.
     *
     * @param exception The exception that caused the message, if present
     * @param format The format string
     * @param args The arguments for the format string
     */
    default void info(@Nullable Throwable exception, @NotNull String format, Object... args) {
        log(LogLevel.INFO, exception, format, args);
    }
    
    /**
     * Logs a {@link LogLevel#DEBUG} message to the console.
     *
     * @param exception The exception that caused the message, if present
     * @param message The message itself
     */
    default void debug(@Nullable Throwable exception, @NotNull CharSequence message) {
        log(LogLevel.DEBUG, exception, message);
    }
    
    /**
     * Logs a {@link LogLevel#DEBUG} message to the console.
     *
     * @param message The message itself
     */
    default void debug(@NotNull CharSequence message) {
        log(LogLevel.DEBUG, null, message);
    }
    
    /**
     * Logs a {@link LogLevel#DEBUG} message to the console.
     *
     * @param format The format string
     * @param args The arguments for the format string
     */
    default void debug(@NotNull String format, Object... args) {
        log(LogLevel.DEBUG, null, format, args);
    }
    
    /**
     * Logs a {@link LogLevel#DEBUG} message to the console.
     *
     * @param exception The exception that caused the message, if present
     * @param format The format string
     * @param args The arguments for the format string
     */
    default void debug(@Nullable Throwable exception, @NotNull String format, Object... args) {
        log(LogLevel.DEBUG, exception, format, args);
    }
}
