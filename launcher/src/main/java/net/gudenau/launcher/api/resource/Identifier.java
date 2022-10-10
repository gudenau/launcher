package net.gudenau.launcher.api.resource;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A uniform way to identify objects and resources. Identifiers must be all lowercase. Namespaces must match the pattern
 * "^[a-z][a-z\\d_]+$" and paths must match the pattern "^[a-z][a-z\d_/.]+$".
 * <p>
 * The {@link String} representation of an {@link Identifier} is the namespace, a colon and then the path. I.E.
 * "namespace:path".
 *
 * @param namespace The namespace of this identifier
 * @param path The path of this identifier
 */
public record Identifier(
    @NotNull String namespace,
    @NotNull String path
) {
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("^[a-z][a-z\\d_]+$");
    private static final Pattern PATH_PATTERN = Pattern.compile("^[a-z][a-z\\d_/.]+$");
    
    /**
     * Creates a new {@link Identifier} instance from the provided {@link String}.
     *
     * @param identifier The {@link String} to parse
     * @return The new {@link Identifier}
     */
    @NotNull
    public static Identifier of(@NotNull String identifier) {
        Objects.requireNonNull(identifier, "identifier can't be null");
        
        var split = identifier.split(":");
        // If the split is not 2 it is either missing a separator or has more than one.
        if (split.length != 2) {
            throw new IllegalArgumentException("Identifier \"" + identifier + "\" was invalid");
        }
        
        return new Identifier(split[0], split[1]);
    }
    
    public Identifier {
        Objects.requireNonNull(namespace, "namespace can't be null");
        Objects.requireNonNull(path, "path can't be null");
        
        if (!NAMESPACE_PATTERN.matcher(namespace).find()) {
            throw new IllegalArgumentException("Namespace \"" + namespace + "\" contained illegal characters, only [a-z0-9_] are allowed");
        }
    
        if (!PATH_PATTERN.matcher(path).find()) {
            throw new IllegalArgumentException("Path \"" + path + "\" contained illegal characters, only [a-z0-9_/.] are allowed");
        }
    }
    
    @Override
    public String toString() {
        return namespace + ':' + path;
    }
}
