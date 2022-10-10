#include <cstdlib>
#include <cstring>

#include <unistd.h>

#include "logger.h"

#include "Communication.h"
#include "Packets.h"

// The lint is wrong, {read, write}Mutex are initialized.
Communication::Communication(int read, int write) { // NOLINT(cppcoreguidelines-pro-type-member-init)
    ASSERT(pthread_mutex_init(&this->readMutex, nullptr), "Failed to init readMutex Communication::readMutex");
    ASSERT(pthread_mutex_init(&this->writeMutex, nullptr), "Failed to init readMutex Communication::write");
    this->read = read;
    this->write = write;
}

Communication::~Communication(void) {
    ASSERT(pthread_mutex_destroy(&this->writeMutex), "Failed to destroy readMutex Communication::writeMutex");
    ASSERT(pthread_mutex_destroy(&this->readMutex), "Failed to destroy readMutex Communication::readMutex");
    close(read);
    close(write);
}

/**
 * A basic "read fully" implementation.
 *
 * @param handle The file handle to read from
 * @param buffer The buffer to write to
 * @param length The amount of data to transfer
 * @return 0 on success, non-0 on failure
 */
static int doRead(int handle, void* buffer, size_t length) {
    auto remaining = length;
    while(remaining) {
        auto transfered = ::read(handle, buffer, remaining);
        if(transfered < 0) {
            return 1;
        }
        remaining -= transfered;
        buffer = (void*)((size_t)buffer + transfered);
    }
    return 0;
}

/**
 * A basic "write fully" implementation.
 *
 * @param handle The file handle to write to
 * @param buffer The buffer to read from
 * @param length The amount of data to transfer
 * @return 0 on success, non-0 on failure
 */
static int doWrite(int handle, const void* buffer, size_t length) {
    auto remaining = length;
    while(remaining) {
        auto transfered = ::write(handle, buffer, remaining);
        if(transfered < 0) {
            return 1;
        }
        remaining -= transfered;
        buffer = (void*)((size_t)buffer + transfered);
    }
    return 0;
}

/**
 * The packet header, all Java types are signed.
 */
struct PacketHeader {
    int id;
    int length;
};

Packet* Communication::readPacket(void) {
    ASSERT(pthread_mutex_lock(&this->readMutex), "Failed to lock readMutex in Communication::readPacket");

    PacketHeader header = {0, 0};
    ASSERT(doRead(read, &header, sizeof(header)), "Failed to read packet header");
    ASSERT(!(header.length >= 0 && header.length <= 4096), "Header length was too large or too small: %d", header.length);

    auto buffer = new Buffer(header.length);
    ASSERT(doRead(read, buffer->rawPointer(), header.length), "Failed to read packet payload");

    // The packet payload is fully transferred, no need to hold the lock any longer.
    ASSERT(pthread_mutex_unlock(&this->readMutex), "Failed to unlock readMutex in Communication::readPacket");

    Packet* packet;
    switch(header.id) {
        case PacketId_ContainerVersion: {
            packet = new ContainerVersionPacket();
        } break;

        default: ABORT("Unknown packet id %d", header.id);
    }
    packet->read(buffer);
    delete buffer;
    return packet;
}

void Communication::writePacket(Packet* packet) {
    ASSERT(!packet, "packet was null in Communication::writePacket");
    auto buffer = new Buffer(4096);
    packet->write(buffer);
    PacketHeader header = {
        packet->id(),
        (int) buffer->position()
    };

    ASSERT(pthread_mutex_lock(&this->writeMutex), "Failed to lock readMutex in Communication::writePacket");
    ASSERT(doWrite(write, &header, sizeof(PacketHeader)), "Failed to write packet header");
    ASSERT(doWrite(write, buffer->rawPointer(), header.length), "Failed to write packet payload");
    ASSERT(pthread_mutex_lock(&this->writeMutex), "Failed to unlock readMutex in Communication::writePacket");
}
