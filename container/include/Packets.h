#pragma once

#include "Buffer.h"

/**
 * All current packet IDs. We skip zero just because that's a common error case.
 */
enum PacketId {
    PacketId_ContainerVersion = 1,
};

/**
 * The root packet type, all packets must implement this "interface".
 */
class Packet {
public:
    virtual ~Packet(void) = default;

    /**
     * Gets the id of this packet.
     *
     * @return The packet id
     */
    virtual PacketId id(void) = 0;
    /**
     * Writes this packet to the provided buffer.
     *
     * @param buffer The buffer to write to
     */
    virtual void write(Buffer* buffer) = 0;
    /**
     * Read this packet from the provided buffer.
     *
     * @param buffer The buffer to read from
     */
    virtual void read(Buffer* buffer) = 0;
};

/**
 * The packet used to communicate the current container version to the JVM.
 *
 * The JVM doesn't send any data.
 */
class ContainerVersionPacket : public Packet {
public:
    ContainerVersionPacket(void) = default;
    ~ContainerVersionPacket(void) override = default;

    PacketId id(void) override;
    void write(Buffer* buffer) override;
    void read(Buffer* buffer) override;
};
