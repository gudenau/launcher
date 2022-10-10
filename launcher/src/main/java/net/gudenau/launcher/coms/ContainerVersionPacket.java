package net.gudenau.launcher.coms;

import net.gudenau.launcher.api.util.Version;

import java.nio.ByteBuffer;

final class ContainerVersionPacket implements Packet {
    private Version version;
    
    @Override
    public void read(ByteBuffer buffer) {
        version = new Version(buffer.getInt(), buffer.getInt(), buffer.getInt(), buffer.getInt());
    }
    
    @Override
    public ByteBuffer write() {
        return ByteBuffer.allocate(0);
    }
    
    public Version version() {
        return version;
    }
}
