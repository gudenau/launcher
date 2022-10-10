package net.gudenau.launcher.impl.resource;

import net.gudenau.launcher.api.resource.Identifier;
import net.gudenau.launcher.api.resource.ResourceManager;
import net.gudenau.launcher.api.resource.ResourceProvider;
import net.gudenau.launcher.api.util.SharedLock;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ResourceManagerImpl implements ResourceManager {
    public static final ResourceManager INSTANCE = new ResourceManagerImpl();
    // Make sure this is the same as Identifier.NAMESPACE_PATTERN
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("^[a-z][a-z\\d_]+$");
    
    private final SharedLock resourceProvidersLock = new SharedLock();
    private final Map<String, ResourceProvider> resourceProviders = new HashMap<>();
    
    @Override
    public void registerProvider(@NotNull ResourceProvider provider) {
        Objects.requireNonNull(provider, "provider can't be null");
    
        var namespace = provider.namespace();
        Objects.requireNonNull(namespace, "provider's namespace can't be null");
        if (!NAMESPACE_PATTERN.matcher(namespace).find()) {
            throw new IllegalArgumentException("Namespace \"" + namespace + "\" contained illegal characters, only [a-z0-9_] are allowed");
        }
        
        resourceProvidersLock.write(() -> {
            if(resourceProviders.putIfAbsent(namespace, provider) != null) {
                throw new IllegalStateException("Provider for namespace " + namespace + " was already registered");
            }
        });
    }
    
    @Override
    @NotNull
    public Path getPath(@NotNull Identifier identifier) {
        Objects.requireNonNull(identifier, "identifier can't be null");
        
        return resourceProvidersLock.read(() -> resourceProviders.get(identifier.namespace()))
            .getPath(identifier);
    }
    
    @Override
    @NotNull
    public Stream<Path> matching(@NotNull String path) {
        Objects.requireNonNull(path, "path can't be null");
        
        return resourceProvidersLock.read(() ->
            resourceProviders.entrySet().stream()
                .map((entry) -> {
                    var namespace = entry.getKey();
                    var provider = entry.getValue();
                    return provider.getPath(new Identifier(namespace, path));
                })
                .filter(Files::exists)
        );
    }
    
    @Override
    @NotNull
    public Set<String> matchingNamespaces(@NotNull String path) {
        Objects.requireNonNull(path, "path can't be null");
        
        return resourceProvidersLock.read(() ->
            resourceProviders.entrySet().stream()
                .filter((entry) -> Files.exists(entry.getValue().getPath(new Identifier(entry.getKey(), path))))
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet())
        );
    }
}
