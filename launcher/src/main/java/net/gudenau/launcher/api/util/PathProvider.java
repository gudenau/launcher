package net.gudenau.launcher.api.util;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A simple interface that adds a bunch of {@link Path}-related methods to a class.
 */
public interface PathProvider {
    /**
     * Gets the {@link Path} for this object.
     *
     * @return the {@link Path} for this object
     */
    @NotNull Path path();
    
    /**
     * Gets the {@link Path} for this object with the provided segment appended to it.
     *
     * @param segment A {@link Path} segment
     * @return The {@link Path}
     */
    @NotNull
    default Path path(@NotNull String segment) {
        return path().resolve(segment);
    }
    
    /**
     * Gets the {@link Path} for this object with the provided segments appended to it.
     *
     * @param segments Some {@link Path} segments
     * @return The {@link Path}
     */
    @NotNull
    default Path path(@NotNull String... segments) {
        var path = path();
        for (var segment : segments) {
            path = path.resolve(segment);
        }
        return path;
    }
    
    /**
     * Gets the {@link Path} for this object with the provided segment appended to it and checks that it is a file.
     *
     * @param segment A {@link Path} segment
     * @return The {@link Path}
     * @throws FileNotFoundException if the file doesn't exist or is a directory
     */
    @NotNull
    default Path filePath(@NotNull String segment) throws FileNotFoundException {
        var path = path(segment);
        if (!Files.isRegularFile(path)) {
            throw new FileNotFoundException(path.toString());
        }
        return path;
    }
    
    /**
     * Gets the {@link Path} for this object with the provided segments appended to it and checks that it is a file.
     *
     * @param segments Some {@link Path} segments
     * @return The {@link Path}
     * @throws FileNotFoundException if the file doesn't exist or is a directory
     */
    @NotNull
    default Path filePath(@NotNull String... segments) throws FileNotFoundException {
        var path = path(segments);
        if (!Files.isRegularFile(path)) {
            throw new FileNotFoundException(path.toString());
        }
        return path;
    }
    
    /**
     * Gets the {@link Path} for this object with the provided segment appended to it and checks that it is a
     * directory.
     *
     * @param segment A {@link Path} segment
     * @return The {@link Path}
     * @throws FileNotFoundException if the directory doesn't exist or is a file
     */
    @NotNull
    default Path directoryPath(@NotNull String segment) throws FileNotFoundException {
        var path = path(segment);
        if (!Files.isDirectory(path)) {
            throw new FileNotFoundException(path.toString());
        }
        return path;
    }
    
    /**
     * Gets the {@link Path} for this object with the provided segments appended to it and checks that it is a
     * directory.
     *
     * @param segments Some {@link Path} segments
     * @return The {@link Path}
     * @throws FileNotFoundException if the directory doesn't exist or is a file
     */
    @NotNull
    default Path directoryPath(@NotNull String... segments) throws FileNotFoundException {
        var path = path(segments);
        if (!Files.isDirectory(path)) {
            throw new FileNotFoundException(path.toString());
        }
        return path;
    }
    
    /**
     * Gets the {@link Path} for this object with the provided segment appended to it and ensures that it is a
     * directory by creating missing paths.
     *
     * @param segment A {@link Path} segment
     * @return The {@link Path}
     * @throws IOException if the directories could not be created
     */
    @NotNull
    default Path ensureDirectoryPath(@NotNull String segment) throws IOException {
        var path = path(segment);
        Files.createDirectories(path);
        return path;
    }
    
    /**
     * Gets the {@link Path} for this object with the provided segments appended to it and ensures that it is a
     * directory by creating missing paths.
     *
     * @param segments Some {@link Path} segments
     * @return The {@link Path}
     * @throws IOException if the directories could not be created
     */
    @NotNull
    default Path ensureDirectoryPath(@NotNull String... segments) throws IOException {
        var path = path(segments);
        Files.createDirectories(path);
        return path;
    }
}
