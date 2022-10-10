package net.gudenau.launcher.stub;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.module.ModuleFinder;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class Stub {
    private static final Set<Dependency> DEPENDENCIES = Set.of(
        new Dependency(Source.MAVEN_CENTRAL, "org.ow2.asm", "asm", "9.3"),
        new Dependency(Source.MAVEN_CENTRAL, "org.ow2.asm", "asm-tree", "9.3"),
        new Dependency(Source.MAVEN_CENTRAL, "com.google.code.gson", "gson", "2.9.1"),
        new Dependency(Source.MAVEN_CENTRAL, "it.unimi.dsi", "fastutil", "8.5.9")
    );
    
    private static final Path LIBRARY_PATH = Path.of(".", "libraries");
    
    public static void main(String[] args) {
        init(-1, -1);
    }
    
    public static void init(int readPipe, int writePipe) {
        try {
            HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
            
            Set<Path> classPath = new HashSet<>();
            CompletableFuture.allOf(DEPENDENCIES.stream()
                .map((dependency) -> CompletableFuture.supplyAsync(() -> downloadDependency(client, dependency)))
                .map((future) -> future.thenAccept((paths) -> {
                    synchronized (classPath) {
                        classPath.addAll(paths);
                    }
                }))
                .toArray(CompletableFuture[]::new)
            ).get();
            
            //FIXME
            classPath.add(Path.of("/home/gudenau/projects/cpp/GameLauncher/launcher/build/libs/launcher-1.0.0.jar"));
            
            var finder = ModuleFinder.of(classPath.toArray(Path[]::new));
            var parent = ModuleLayer.boot();
            var configuration = parent.configuration().resolve(finder, ModuleFinder.of(), Set.of("launcher"));
            var loader = ClassLoader.getSystemClassLoader();
            var layer = parent.defineModulesWithOneLoader(configuration, loader);
            var launcher = layer.findLoader("launcher").loadClass("net.gudenau.launcher.Launcher");
            // Idea is drunk here.
            //noinspection ConfusingArgumentToVarargsMethod
            MethodHandles.lookup()
                .findStatic(launcher, "main", MethodType.methodType(void.class, String[].class))
                .invokeExact(new String[]{
                    Integer.toString(readPipe),
                    Integer.toString(writePipe)
                });
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    private static Set<Path> downloadDependency(HttpClient client, @NotNull Dependency dependency) {
        var path = LIBRARY_PATH;
        for (var segment : dependency.group().split("\\.")) {
            path = path.resolve(segment);
        }
        path = path.resolve(Path.of(dependency.name(), dependency.version(), dependency.name() + '-' + dependency.version() + ".jar"));
        
        if(Files.exists(path)) {
            return Set.of(path);
        }
        
        var url = dependency.source().url + '/' + dependency.group.replaceAll("\\.", "/") + '/' + dependency.name() + '/' + dependency.version() + '/' + dependency.name() + '-' + dependency.version() + ".jar";
        var request = HttpRequest.newBuilder(URI.create(url))
            .build();
        try {
            Files.createDirectories(path.getParent());
            client.send(request, HttpResponse.BodyHandlers.ofFile(path));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    
        return Set.of(path);
    }
    
    private enum Source {
        MAVEN_CENTRAL("https://repo1.maven.org/maven2"),
        ;
        
        private final String url;
        
        Source(String url) {
            this.url = url;
        }
    }
    
    private record Dependency(
        @NotNull Source source,
        @NotNull String group,
        @NotNull String name,
        @NotNull String version
    ){}
}
