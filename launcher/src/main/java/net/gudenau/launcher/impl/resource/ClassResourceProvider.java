package net.gudenau.launcher.impl.resource;

import net.gudenau.launcher.api.resource.Identifier;
import net.gudenau.launcher.api.resource.ResourceProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Map;
import java.util.Set;

import static net.gudenau.launcher.impl.util.MiscUtil.className;

public class ClassResourceProvider implements ResourceProvider {
    private final String namespace;
    private final FileSystem fileSystem;
    private final Path root;
    
    public ClassResourceProvider(@NotNull String namespace, Class<?> type) {
        this.namespace = namespace;
        
        Path classPath;
        try {
            classPath = Paths.get(type.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to get path from protection domain of class " + className(type), e);
        }
    
        if (Files.isRegularFile(classPath)) {
            try {
                fileSystem = FileSystems.newFileSystem(classPath, Map.of(
                    "defaultPermissions", Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.GROUP_READ, PosixFilePermission.OTHERS_READ)
                ));
            } catch (IOException e) {
                throw new RuntimeException("Failed to open jar of class " + className(type), e);
            }
            
            root = fileSystem.getRootDirectories().iterator().next().resolve("res");
        } else {
            throw new UnsupportedOperationException("Flat namespaces are not yet supported");
        }
    }
    
    @Override
    public @NotNull String namespace() {
        return namespace;
    }
    
    @Override
    public @NotNull Path getPath(@NotNull Identifier identifier) {
        return root.resolve(identifier.namespace()).resolve(identifier.path());
    }
}
