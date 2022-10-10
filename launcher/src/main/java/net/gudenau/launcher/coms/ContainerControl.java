package net.gudenau.launcher.coms;

import net.gudenau.launcher.api.util.Version;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public final class ContainerControl {
    public static Version version() throws IOException {
        var future = Communications.readPacket(ContainerVersionPacket.class);
        Communications.writePacket(new ContainerVersionPacket());
        try {
            return future.get().version();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get container version", e);
        }
    }
    
    private ContainerControl() {
        throw new AssertionError();
    }
}
