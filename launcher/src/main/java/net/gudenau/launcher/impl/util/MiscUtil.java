package net.gudenau.launcher.impl.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.gudenau.launcher.api.util.Identified;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public final class MiscUtil {
    private static final Path WORK_DIR = Path.of(".").toAbsolutePath();
    
    public static String className(Class<?> type) {
        var module = type.getModule();
        if (module.isNamed()) {
            return module.getName() + '/' + type.getName();
        } else {
            return "UNNAMED/" + type.getName();
        }
    }
    
    public static Path getPath(String path) {
        return WORK_DIR.resolve(path);
    }
    
    public static Path getPath(String... path) {
        var current = WORK_DIR;
        for (var segment : path) {
            current = current.resolve(segment);
        }
        return current;
    }
    
    public static Path createDirectory(String path) throws IOException {
        var location = getPath(path);
        Files.createDirectories(location);
        return location;
    }
    
    @NotNull
    public static UUID uuid(@NotNull Collection<? extends Identified> existing) {
        var ids = existing.stream()
            .map(Identified::id)
            .collect(Collectors.toUnmodifiableSet());
        
        UUID id;
        do {
            id = UUID.randomUUID();
        } while (ids.contains(id));
        
        return id;
    }
    
    public static void ensureParentsExist(Path path) throws IOException {
        var parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
    
    @NotNull
    public static JsonElement readJson(@NotNull Path path) throws IOException {
        try(var reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader);
        } catch (Throwable e) {
            throw new IOException("Failed to read JSON file: " + path);
        }
    }
    
    @NotNull
    public static JsonObject readJsonObject(@NotNull Path path) throws IOException {
        if(readJson(path) instanceof JsonObject object) {
            return object;
        } else {
            throw new IOException("Failed to read JSON file, expected a root object: " + path);
        }
    }
    
    @NotNull
    public static JsonArray readJsonArray(@NotNull Path path) throws IOException {
        if(readJson(path) instanceof JsonArray array) {
            return array;
        } else {
            throw new IOException("Failed to read JSON file, expected a root array: " + path);
        }
    }
}
