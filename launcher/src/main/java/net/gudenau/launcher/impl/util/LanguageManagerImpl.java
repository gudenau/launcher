package net.gudenau.launcher.impl.util;

import com.google.gson.JsonParser;
import net.gudenau.launcher.api.resource.Identifier;
import net.gudenau.launcher.api.resource.ResourceManager;
import net.gudenau.launcher.api.util.LanguageManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class LanguageManagerImpl implements LanguageManager {
    private record LanguagePath(@NotNull String namespace, @NotNull Path path, @NotNull Set<String> languages) {}
    
    public static final LanguageManagerImpl INSTANCE = new LanguageManagerImpl();
    private static final String DEFAULT_LANGUAGE = "en_us";
    private static final Pattern LANG_FILE_PATTERN = Pattern.compile("^[a-z]{2}_[a-z]{2}\\.json$");
    
    private Map<String, Set<LanguagePath>> languages = Map.of();
    private Map<String, String> translations = Map.of();
    private String currentLanguage = DEFAULT_LANGUAGE;
    
    @Override
    @NotNull
    public String translate(@NotNull String key) {
        Objects.requireNonNull(key, "key can't be null");
        
        return translations.getOrDefault(key, key);
    }
    
    public void init() throws IOException {
        var resourceManager = ResourceManager.get();
        
        var namespaces = resourceManager.matchingNamespaces("lang");
        languages = namespaces.stream().map((namespace) -> {
                try {
                    var directory = resourceManager.resolveDirectory(new Identifier(namespace, "lang"));
                    try (var stream = Files.list(directory)) {
                        return new LanguagePath(
                            namespace,
                            directory,
                            stream.filter(Files::isRegularFile)
                                .map((file) -> file.getFileName().toString())
                                .filter((file) -> LANG_FILE_PATTERN.matcher(file).find())
                                .collect(Collectors.toUnmodifiableSet())
                        );
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .<Map<String, Set<LanguagePath>>, Map<String, Set<LanguagePath>>>collect(Collector.of(
                HashMap::new,
                (map, value) -> {
                    value.languages().forEach((language) -> {
                        map.computeIfAbsent(language.substring(0, 5), (lang) -> new HashSet<>()).add(value);
                    });
                }, (a, b) -> {
                    var q = new HashMap<>(a);
                    b.forEach((key, value) -> q.computeIfAbsent(key, (lang) -> new HashSet<>()).addAll(value));
                    return q;
                },
                Collections::unmodifiableMap
            ));
        
        reloadLanguages();
    }
    
    private void reloadLanguages() {
        var translations = readTranslations(DEFAULT_LANGUAGE);
        if (!currentLanguage.equals(DEFAULT_LANGUAGE)) {
            translations.putAll(readTranslations(currentLanguage));
        }
        this.translations = translations;
    }
    
    private Map<String, String> readTranslations(String language) {
        var paths = languages.get(language);
        if (paths == null || paths.isEmpty()) {
            return new HashMap<>();
        }
        
        return paths.stream()
            .map((path) -> {
                try(var reader = Files.newBufferedReader(path.path().resolve(language + ".json"))) {
                    return JsonParser.parseReader(reader).getAsJsonObject().entrySet().stream()
                        .collect(Collectors.toUnmodifiableMap(
                            (entry) -> path.namespace() + '.' + entry.getKey(),
                            (entry) -> entry.getValue().getAsJsonPrimitive().getAsString()
                        ));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collector.of(
                HashMap::new,
                HashMap::putAll,
                (a, b) -> {
                    a.putAll(b);
                    return a;
                }
            ));
    }
}
