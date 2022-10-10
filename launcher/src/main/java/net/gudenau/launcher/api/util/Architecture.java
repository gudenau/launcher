package net.gudenau.launcher.api.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * All CPU architectures known to the launcher.
 */
public enum Architecture {
    /**
     * AMD64. The 64 bit version of Intel's x86 ISA.
     * <p>
     * AKA x64, x86_64 and probably others.
     */
    AMD64("amd64"),
    /**
     * The default non-existent architecture used in cases where we don't know the current one.
     */
    UNKNOWN("unknown", (arch) -> false),
    ;
    
    // Cache it, just in case.
    private static final Architecture ARCH;
    static {
        var arch = UNKNOWN;
        var name = System.getProperty("os.arch").toLowerCase();
        for (var value : values()) {
            if (value.predicate.test(name)) {
                arch = value;
                break;
            }
        }
        ARCH = arch;
    }
    
    /**
     * Gets the CPU {@link Architecture} of the current JVM.
     *
     * @return The current {@link Architecture}
     */
    @NotNull
    public static Architecture get() {
        return ARCH;
    }
    
    /**
     * The human-readable name of this CPU architecture.
     */
    private final String name;
    /**
     * The predicate for this CPU architecture.
     */
    private final Predicate<String> predicate;
    
    /**
     * Creates a new {@link Architecture} instance with a simple equality {@link Predicate}.
     *
     * @param name The human-readable architecture name
     */
    Architecture(String name) {
        this(name, (arch) -> arch.equals(name));
    }
    
    /**
     * Creates a new {@link Architecture} instance with the provided {@link Predicate}.
     *
     * @param name The human-readable architecture name
     */
    Architecture(String name, Predicate<String> predicate) {
        this.name = name;
        this.predicate = predicate;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
