package net.gudenau.launcher.impl.profile;

import net.gudenau.launcher.api.util.Identified;
import net.gudenau.launcher.api.util.PathProvider;
import net.gudenau.launcher.impl.util.MiscUtil;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.UUID;

public final class Profile implements Identified, PathProvider {
    private final UUID id;
    private final String name;
    private final Path path;
    
    Profile(UUID id, String name) {
        this.id = id;
        this.name = name;
        this.path = MiscUtil.getPath("profiles", id.toString());
    }
    
    @NotNull
    public String name() {
        return name;
    }
    
    @NotNull
    @Override
    public UUID id() {
        return id;
    }
    
    @NotNull
    @Override
    public Path path() {
        return path;
    }
}
