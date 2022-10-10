package net.gudenau.launcher.api.util;

import net.gudenau.launcher.impl.util.LanguageManagerImpl;
import org.jetbrains.annotations.NotNull;

/**
 * The heart of localization support, translates strings to different languages.
 */
public sealed interface LanguageManager permits LanguageManagerImpl {
    /**
     * Gets the {@link LanguageManager} singleton.
     *
     * @return The {@link LanguageManager} singleton.
     */
    @NotNull
    static LanguageManager get() {
        return LanguageManagerImpl.INSTANCE;
    }
    
    /**
     * Attempts to translate the provided key. If the current language does not have the provided key and it is *not*
     * set to US English; the language manager will attempt to resolve the US English value. If the lookup fails the key
     * will be returned unmodified.
     *
     * @param key The language key to lookup
     * @return The translated string or the key on failure
     */
    @NotNull
    String translate(@NotNull String key);
}
