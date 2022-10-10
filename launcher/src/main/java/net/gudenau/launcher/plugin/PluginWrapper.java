package net.gudenau.launcher.plugin;

import net.gudenau.launcher.api.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

final class PluginWrapper implements Plugin {
    private final Plugin plugin;
    private final UUID id;
    private volatile boolean loaded = false;
    
    public PluginWrapper(Plugin plugin) {
        this.plugin = plugin;
        this.id = plugin.id();
    }
    
    @Override
    public @NotNull UUID id() {
        return id;
    }
    
    @Override
    public void init() {
        plugin.init();
        
        synchronized (this) {
            loaded = true;
            notifyAll();
        }
    }
    
    public void waitUntilLoaded() {
        if (loaded) {
            return;
        }
        
        synchronized (this) {
            while (loaded) {
                try {
                    wait();
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
