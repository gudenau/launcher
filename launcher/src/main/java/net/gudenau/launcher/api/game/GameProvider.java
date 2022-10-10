package net.gudenau.launcher.api.game;

import org.jetbrains.annotations.NotNull;
import net.gudenau.launcher.api.util.Identified;

public interface GameProvider extends Identified {
    @NotNull String name();
}
