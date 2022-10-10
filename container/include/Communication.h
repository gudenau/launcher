#pragma once

#include <pthread.h>

#include "types.h"
#include "Packets.h"

/**
 * A simple IPC implementation.
 */
class Communication {
public:
    /**
     * Creates a new instance of Communication that uses the provided file handles to communicate.
     *
     * @param read The read handle
     * @param write The write handle
     */
    Communication(int read, int write);
    /**
     * Closes the handles used during IPC.
     */
    ~Communication(void);

    /**
     * Reads a packet from the IPC channel.
     * @return The read packet
     */
    Packet* readPacket(void);

    /**
     * Writes a packet to the IPC channel.
     *
     * @param packet The packet to write
     */
    void writePacket(Packet* packet);

private:
    /**
     * The mutex used to ensure that only one thread is reading a packet at once.
     */
    pthread_mutex_t readMutex;
    /**
     * The mutex used to ensure that only one thread is writing a packet at once.
     */
    pthread_mutex_t writeMutex;

    /**
     * The read handle.
     */
    int read;
    /**
     * The write handle.
     */
    int write;
};
