package net.gudenau.dummy;

import net.gudenau.launcher.api.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class Dummy implements Plugin {
    @Override
    public @NotNull UUID id() {
        return UUID.fromString("01bbffe5-981e-41b6-8a38-bf16ba06c1c1");
    }
    
    @Override
    public void init() {
        /*
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
         */
    }
}
