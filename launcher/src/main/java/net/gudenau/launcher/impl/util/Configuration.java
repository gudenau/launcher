package net.gudenau.launcher.impl.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public final class Configuration<T> {
    private static final Serializer<Integer> INTEGER_SERIALIZER = serializer(Integer::valueOf, String::valueOf);
    private static final Serializer<Boolean> BOOLEAN_SERIALIZER = serializer(Boolean::valueOf, String::valueOf);
    private static final Serializer<String> STRING_SERIALIZER = serializer(Function.identity(), Function.identity());
    
    private static final Set<Configuration<?>> VALUES = new HashSet<>();
    
    private static Configuration<Integer> integer(String name, int value) {
        return new Configuration<>(name, value, INTEGER_SERIALIZER);
    }
    
    private static Configuration<Boolean> bool(String name, boolean value) {
        return new Configuration<>(name, value, BOOLEAN_SERIALIZER);
    }
    
    private static Configuration<String> string(String name, String value) {
        return new Configuration<>(name, value, STRING_SERIALIZER);
    }
    
    public static Set<Configuration<?>> values() {
        return Collections.unmodifiableSet(VALUES);
    }
    
    public static final Configuration<Boolean> DISABLE_HIDING = bool("disable_hiding", false);
    public static final Configuration<Integer> THREAD_COUNT = integer("thread_count", Runtime.getRuntime().availableProcessors() << 1);
    public static final Configuration<String> LOG_LEVEL = string("log_level", "debug");
    
    private static final Path CONFIG_PATH = MiscUtil.getPath("configuration.json");
    static {
        AutoSaver.registerSaver(Configuration::save);
        
        try {
            load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration.json");
        }
    }
    
    private static void save() throws IOException {
        if(values().stream().noneMatch((config) -> config.dirty)){
            return;
        }
        
        var object = new JsonObject();
        for (var config : values()) {
            config.dirty = false;
            
            var value = config.get();
            if (value instanceof Boolean bool) {
                object.addProperty(config.name, bool);
            } else if (value instanceof Integer integer) {
                object.addProperty(config.name, integer);
            } else {
                throw new RuntimeException("You dummy");
            }
        }
        
        MiscUtil.ensureParentsExist(CONFIG_PATH);
        try (var writer = Files.newBufferedWriter(CONFIG_PATH)) {
            writer.write(object.toString());
        }
    }
    
    @SuppressWarnings({"RedundantCast", "unchecked"})
    private static void load() throws IOException {
        if (!Files.isRegularFile(CONFIG_PATH)) {
            return;
        }
        
        JsonObject object;
        try (var reader = Files.newBufferedReader(CONFIG_PATH)) {
            if(!(JsonParser.parseReader(reader) instanceof JsonObject element)) {
                throw new IOException("Failed to read configuration.json: expected an object");
            }
            object = element;
        }
    
        for (var config : values()) {
            var value = config.get();
            var element = object.getAsJsonPrimitive(config.name());
            if (value instanceof Boolean) {
                ((Configuration<Boolean>) config).value = element.getAsBoolean();
            } else if (value instanceof Integer) {
                ((Configuration<Integer>) config).value = element.getAsInt();
            } else {
                throw new RuntimeException("You dummy");
            }
        }
    }
    
    private final String name;
    private final Serializer<T> serializer;
    private T value;
    private boolean dirty;
    
    private Configuration(String name, T value, Serializer<T> serializer) {
        this.name = name;
        this.serializer = serializer;
        
        var prop = System.getProperty("launcher." + name);
        if (prop != null) {
            value = serializer.deserialize(prop);
        }
        this.value = value;
        
        VALUES.add(this);
    }
    
    public T get() {
        return value;
    }
    
    public String name() {
        return name;
    }
    
    private static <T> Serializer<T> serializer(Function<String, T> deserializer, Function<T, String> serializer) {
        return new Serializer<>() {
            @Override
            public T deserialize(String value) {
                return deserializer.apply(value);
            }
    
            @Override
            public String serialize(T value) {
                return serializer.apply(value);
            }
        };
    }
    
    public void set(T value) {
        if (this.value.equals(value)) {
            return;
        }
        
        this.value = value;
        dirty = true;
    }
    
    private interface Serializer<T> {
        T deserialize(String value);
        String serialize(T value);
    }
}
