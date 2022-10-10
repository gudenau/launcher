package net.gudenau.launcher.plugin;

import net.gudenau.launcher.api.Plugin;
import net.gudenau.launcher.api.account.AccountProvider;
import net.gudenau.launcher.api.resource.ResourceManager;
import net.gudenau.launcher.api.resource.ResourceProvider;
import net.gudenau.launcher.api.util.Identified;
import net.gudenau.launcher.impl.util.MiscUtil;
import net.gudenau.launcher.impl.util.ThreadUtil;
import net.gudenau.launcher.ui.LoadingScreen;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PluginLoader {
    private static ModuleLayer layer;
    private static Map<UUID, PluginWrapper> plugins = Map.of();
    
    public static void init() throws IOException {
        if (layer != null) {
            throw new IllegalStateException("Plugin loader already initialized");
        }
        
        layer = createLayer();
    
        var manager = ResourceManager.get();
        var plugins = new HashSet<PluginWrapper>();
        for (var plugin : ServiceLoader.load(layer, Plugin.class)) {
            var type = plugin.getClass();
            manager.registerProvider(ResourceProvider.of(type.getModule().getName(), type));
            plugins.add(new PluginWrapper(plugin));
        }
        PluginLoader.plugins = plugins.stream().collect(Collectors.toUnmodifiableMap(
            Identified::id,
            Function.identity()
        ));
    
        LoadingScreen.setMaxProgress(plugins.size() + 1);
        LoadingScreen.incrementProgress();
    
        ThreadUtil.waitFor(plugins.stream()
            .<Runnable>map((plugin) -> () -> {
                try {
                    plugin.init();
                } finally {
                    LoadingScreen.incrementProgress();
                }
            })
            .collect(Collectors.toUnmodifiableSet())
        );
    }
    
    public static Optional<Plugin> getLoadedPlugin(UUID id) {
        var plugin = plugins.get(id);
        if (plugin == null) {
            return Optional.empty();
        }
        plugin.waitUntilLoaded();
        return Optional.of(plugin);
    }
    
    private static ModuleLayer createLayer() throws IOException {
        var path = MiscUtil.createDirectory("plugins");
    
        Set<Path> pluginJars;
        try (var stream = Files.list(path)) {
            pluginJars = stream.filter(Files::isRegularFile)
                .filter((file) -> file.getFileName().toString().endsWith(".jar"))
                .collect(Collectors.toUnmodifiableSet());
        }
    
        var pluginNames = new HashSet<String>();
        var iterator = pluginJars.iterator();
        while (iterator.hasNext()) {
            var jar = iterator.next();
            try (var fs = FileSystems.newFileSystem(jar)) {
                var moduleClass = fs.getPath("module-info.class");
                if (!Files.isRegularFile(moduleClass)) {
                    iterator.remove();
                    continue;
                }
            
                try (var stream = Files.newInputStream(moduleClass)) {
                    var node = new ClassNode();
                    new ClassReader(stream).accept(node, 0);
                    var moduleNode = node.module;
                    if (moduleNode == null) {
                        iterator.remove();
                        continue;
                    }
                
                    if(moduleNode.requires.stream().noneMatch((dependency) ->
                        dependency.module.equals("launcher")
                    )) {
                        iterator.remove();
                        continue;
                    }
                
                    pluginNames.add(moduleNode.name);
                }
            }
        }
    
        var finder = ModuleFinder.of(pluginJars.toArray(Path[]::new));
        var parent = PluginLoader.class.getModule().getLayer();
        var cf = parent.configuration().resolve(finder, ModuleFinder.of(), pluginNames);
        var scl = ClassLoader.getSystemClassLoader();
        return parent.defineModulesWithManyLoaders(cf, scl);
    }
    
    public static <T> Stream<T> services(Class<T> service) {
        return ServiceLoader.load(layer, service).stream()
            .map(ServiceLoader.Provider::get);
    }
}
