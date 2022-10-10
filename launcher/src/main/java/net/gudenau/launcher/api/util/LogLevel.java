package net.gudenau.launcher.api.util;

/**
 * All possible log levels.
 */
public enum LogLevel {
    /**
     * A fatal error, likely to lead to the program terminating.
     */
    FATAL,
    /**
     * A general error, the program should be able to recover.
     */
    ERROR,
    /**
     * Warning messages.
     */
    WARNING,
    /**
     * Informative messages.
     */
    INFO,
    /**
     * Debug messages.
     */
    DEBUG,
}
