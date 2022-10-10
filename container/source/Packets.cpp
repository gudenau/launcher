#include "Buffer.h"

#include "Packets.h"

PacketId ContainerVersionPacket::id(void) {
    return PacketId_ContainerVersion;
}
void ContainerVersionPacket::write(Buffer* buffer) {
    buffer->writeS32(VERSION_MAJOR);
    buffer->writeS32(VERSION_MINOR);
    buffer->writeS32(VERSION_PATCH);
    buffer->writeS32(-1);
}
void ContainerVersionPacket::read(Buffer* buffer) {
    (void)buffer;
}
