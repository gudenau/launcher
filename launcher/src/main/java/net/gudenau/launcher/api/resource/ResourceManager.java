package net.gudenau.launcher.api.resource;

import net.gudenau.launcher.impl.resource.ResourceManagerImpl;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The manager of resources.
 * <p>
 * Provides a uniform view of files from any source.
 */
public sealed interface ResourceManager permits ResourceManagerImpl {
    /**
     * Gets the {@link ResourceManager} singleton.
     *
     * @return The {@link ResourceManager} singleton
     */
    @NotNull
    static ResourceManager get() {
        return ResourceManagerImpl.INSTANCE;
    }
    
    /**
     * Registers a new {@link ResourceProvider}. The namespace it provides resources from is gotten from the provider
     * itself.
     *
     * @param provider The {@link ResourceProvider} to register
     */
    void registerProvider(@NotNull ResourceProvider provider);
    
    /**
     * Gets the {@link Path} representation of an {@link Identifier}.
     *
     * @param identifier The {@link Identifier} to process
     * @return The {@link Path}
     */
    @NotNull Path getPath(@NotNull Identifier identifier);
    
    /**
     * Gets to an existing {@link Path} that an {@link Identifier} points to. If the path does not exist a
     * {@link FileNotFoundException} will be thrown.
     *
     * @param identifier The {@link Identifier} to process
     * @return The {@link Path} of an inode
     * @throws FileNotFoundException if the {@link Identifier} does not exist
     */
    @NotNull
    default Path resolvePath(@NotNull Identifier identifier) throws FileNotFoundException  {
        var path = getPath(identifier);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Could not find resource " + identifier);
        }
        return path;
    }
    
    /**
     * Gets to an existing file that an {@link Identifier} points to. If the file does not exist a
     * {@link FileNotFoundException} will be thrown.
     *
     * @param identifier The {@link Identifier} to process
     * @return The {@link Path} of a file
     * @throws FileNotFoundException if the {@link Identifier} does not exist
     */
    @NotNull
    default Path resolveFile(@NotNull Identifier identifier) throws IOException  {
        var path = getPath(identifier);
        if (!Files.isRegularFile(path)) {
            throw new FileNotFoundException("Resource " + identifier + " was not a file");
        }
        return path;
    }
    
    /**
     * Gets to an existing directory that an {@link Identifier} points to. If the directory does not exist a
     * {@link FileNotFoundException} will be thrown.
     *
     * @param identifier The {@link Identifier} to process
     * @return The {@link Path} of a directory
     * @throws FileNotFoundException if the {@link Identifier} does not exist
     */
    @NotNull
    default Path resolveDirectory(@NotNull Identifier identifier) throws IOException  {
        var path = getPath(identifier);
        if (!Files.isDirectory(path)) {
            throw new FileNotFoundException("Resource " + identifier + " was not a directory");
        }
        return path;
    }
    
    /**
     * Opens a new {@link InputStream} for the provided {@link Identifier}.
     *
     * @param identifier The identifier to open
     * @return The new {@link InputStream}
     * @throws IOException If there was an error opening the file
     * @throws FileNotFoundException If the file does not exist or is a directory
     */
    @NotNull
    default InputStream newStream(@NotNull Identifier identifier) throws IOException {
        return Files.newInputStream(resolveFile(identifier), StandardOpenOption.READ);
    }
    
    /**
     * Opens a new UTF-8 {@link BufferedReader} for the provided {@link Identifier}.
     *
     * @param identifier The identifier to open
     * @return The new {@link BufferedReader}
     * @throws IOException If there was an error opening the file
     * @throws FileNotFoundException If the file does not exist or is a directory
     */
    @NotNull
    default BufferedReader newReader(@NotNull Identifier identifier) throws IOException {
        return Files.newBufferedReader(resolveFile(identifier), StandardCharsets.UTF_8);
    }
    
    /**
     * Opens a new read-only {@link ByteChannel} for the provided {@link Identifier}.
     *
     * @param identifier The identifier to open
     * @return The new {@link ByteChannel}
     * @throws IOException If there was an error opening the file
     * @throws FileNotFoundException If the file does not exist or is a directory
     */
    @NotNull
    default ByteChannel newChannel(@NotNull Identifier identifier) throws IOException {
        return Files.newByteChannel(resolveFile(identifier), StandardOpenOption.READ);
    }
    
    /**
     * Returns all known {@link Path}s that match the given path.
     * <p>
     * If you have a set of resources in the following locations:
     * <ul>
     * <li>namespace_a:image</lu>
     * <li>namespace_a:json</lu>
     * <li>namespace_b:image</lu>
     * <li>namespace_b:json</lu>
     * </ul>
     * and you provide this method with `json` it will return a {@link Stream} with {@link Path} for the
     * {@link Identifier}s:
     * <ul>
     * <li>namespace_a:json</li>
     * <li>namespace_b:json</li>
     * </ul>
     *
     * @param path The path to search for
     * @return The {@link Stream} of matching paths
     */
    @NotNull
    Stream<Path> matching(@NotNull String path);
    
    /**
     * Returns all known {@link Path}s that match the given path and are regular files.
     * <p>
     * This is equivalent to
     * {@snippet :
     * matching(path).filter(Files::isRegularFile)
     * }
     *
     * @see ResourceManager#matching(String)
     * @param path The file path to look for
     * @return The {@link Stream} of matching paths
     */
    @NotNull
    default Stream<Path> files(@NotNull String path) {
        return matching(path).filter(Files::isRegularFile);
    }
    
    /**
     * Returns all known {@link Path}s that match the given path and are directories.
     * <p>
     * This is equivalent to
     * {@snippet :
     * matching(path).filter(Files::isDirectory)
     * }
     *
     * @see ResourceManager#matching(String)
     * @param path The directory path to look for
     * @return The {@link Stream} of matching paths
     */
    @NotNull
    default Stream<Path> directories(@NotNull String path) {
        return matching(path).filter(Files::isDirectory);
    }
    
    /**
     * Returns all known namespaces that have the given path.
     * 
     * @see ResourceManager#matching(String) 
     * @param path The path to look for
     * @return The {@link Set} of matching namespaces
     */
    @NotNull
    Set<String> matchingNamespaces(@NotNull String path);
}
