package net.gudenau.launcher.api.resource;

import net.gudenau.launcher.impl.resource.ClassResourceProvider;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Objects;

/**
 * A provider of resources.
 * <p>
 * A resource provider can only have a single namespace assigned to it.
 */
public interface ResourceProvider {
    /**
     * Creates a basic {@link ResourceProvider} for a given class. This provider will use
     * {@link java.security.CodeSource} of the provided {@link Class} to create a read-only
     * {@link java.nio.file.FileSystem} that backs the created provider.
     *
     * @param namespace The namespace to use
     * @param type The source of the resources
     * @return The new {@link ResourceProvider} instance
     */
    @NotNull
    static ResourceProvider of(@NotNull String namespace, @NotNull Class<?> type) {
        Objects.requireNonNull(namespace, "namespace can't be null");
        Objects.requireNonNull(type, "type can't be null");
        
        return new ClassResourceProvider(namespace, type);
    }
    
    /**
     * Gets the namespace for this {@link ResourceProvider}.
     *
     * @return The namespace
     */
    @NotNull String namespace();
    
    /**
     * Resolves an {@link Identifier} to a {@link Path}.
     *
     * @implNote The created {@link Path} does not have to exist and should not be checked
     * @param identifier The {@link Identifier} to resolve
     * @return The corresponding {@link Path}
     */
    @NotNull Path getPath(@NotNull Identifier identifier);
}
