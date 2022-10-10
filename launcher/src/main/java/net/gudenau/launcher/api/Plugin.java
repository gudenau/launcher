package net.gudenau.launcher.api;

import net.gudenau.launcher.api.util.Identified;
import net.gudenau.launcher.plugin.PluginLoader;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * The core of the plugin system, all plugins are required to implement this interface and register the implementation
 * as a service in the module-info class.
 */
public interface Plugin extends Identified {
    /**
     * Gets a plugin for a provided {@link UUID}, if present.
     *
     * @param id The {@link UUID} of the plugin
     * @return The plugin or empty
     */
    @NotNull
    static Optional<Plugin> get(@NotNull UUID id) {
        Objects.requireNonNull(id, "id can't be null");
        return PluginLoader.getLoadedPlugin(id);
    }
    
    /**
     * Run initialization tasks that the plugin might need to do.
     */
    default void init() {}
}
