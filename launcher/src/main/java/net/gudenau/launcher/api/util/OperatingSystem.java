package net.gudenau.launcher.api.util;

import java.util.function.Predicate;

/**
 * An operating system that is known to the launcher.
 */
public enum OperatingSystem {
    /**
     * Anything that runs the Linux kernel and follows "Linux" standards.
     */
    LINUX("linux"),
    /**
     * Windows, the weird NT one.
     */
    WINDOWS("windows"),
    /**
     * An unknown operating system.
     */
    UNKNOWN("unknown", (os) -> false),
    ;
    
    // Cache it, why not?
    private static final OperatingSystem OS;
    static {
        var os = UNKNOWN;
        var name = System.getProperty("os.name").toLowerCase();
        for (var value : values()) {
            if (value.predicate.test(name)) {
                os = value;
                break;
            }
        }
        OS = os;
    }
    
    /**
     * Gets the {@link OperatingSystem} that the current JVM is running on.
     *
     * @return The current {@link OperatingSystem}
     */
    public static OperatingSystem get() {
        return OS;
    }
    
    /**
     * The human-readable name of the operating system.
     */
    private final String name;
    /**
     * The predicate for this operating system.
     */
    private final Predicate<String> predicate;
    
    /**
     * Creates a new {@link OperatingSystem} instance with a contains-based {@link Predicate}.
     *
     * @param name The human-readable operating system name
     */
    OperatingSystem(String name) {
        this(name, (os) -> os.contains(name));
    }
    
    /**
     * Creates a new {@link OperatingSystem} instance with the provided {@link Predicate}
     *
     * @param name The human-readable operating system name
     * @param predicate The predicate to use
     */
    OperatingSystem(String name, Predicate<String> predicate) {
        this.name = name;
        this.predicate = predicate;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
