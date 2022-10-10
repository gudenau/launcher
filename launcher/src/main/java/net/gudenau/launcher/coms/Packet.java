package net.gudenau.launcher.coms;

import java.nio.ByteBuffer;

interface Packet {
    void read(ByteBuffer buffer);
    ByteBuffer write();
}
