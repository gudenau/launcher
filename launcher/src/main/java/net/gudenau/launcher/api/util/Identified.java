package net.gudenau.launcher.api.util;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A utility interface used by other interfaces that require a {@link UUID}.
 */
public interface Identified {
    /**
     * The {@link UUID} of this object.
     *
     * @implSpec The returned {@link UUID} must be the same between runs and different calls to the same object.
     * @return The {@link UUID} for this object
     */
    @NotNull UUID id();
}
