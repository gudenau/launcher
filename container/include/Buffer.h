#pragma once

#include <cstdlib>

#include "types.h"

/**
 * A bare-bones buffer that tracks a pointer and it's size.
 *
 * Currently it is a fixed size, this may change if the need arises.
 */
class Buffer {
public:
    /**
     * Creates a new buffer with the provided size.
     *
     * @param initialSize The size of the new buffer
     */
    explicit Buffer(size_t initialSize);
    /**
     * Destroys the buffer and frees the backing memory.
     */
    ~Buffer(void);

    /**
     * Gets the current pointer of this buffer. The current pointer is the raw pointer with the current position added.
     *
     * @return The current pointer
     */
    void* pointer(void);
    /**
     * Gets the pointer of the buffer, ignoring the current position. This pointer will be the same as the pointer
     * returned by the allocation.
     *
     * @return The raw pointer
     */
    void* rawPointer(void);
    /**
     * Gets the size of this buffer in bytes.
     *
     * @return The buffer size
     */
    [[nodiscard]] size_t size(void) const;
    /**
     * Gets the position of this buffer in bytes.
     *
     * @return The buffer position
     */
    [[nodiscard]] size_t position(void) const;
    /**
     * Sets the position of this buffer in bytes.
     *
     * @param position The new position
     * @return The old position
     */
    size_t position(size_t position);
    /**
     * Gets the remaining bytes in this buffer.
     *
     * @return The remaining bytes
     */
    [[nodiscard]] size_t remaining(void) const;

    /**
     * Copies a block of memory into this buffer at the current position and updates the position to point after the
     * copied block of memory.
     *
     * @param data The buffer to copy from
     * @param bytes The amount of data to copy in bytes
     */
    void write(const void* data, size_t bytes);
    /**
     * Copies a block of memory out of this buffer from the current position and updates the position to point after the
     * copied block of memory.
     *
     * @param data The buffer to copy to
     * @param bytes The amount of data to copy in bytes
     */
    void read(void* data, size_t bytes);

    /**
     * Writes an unsigned 8 bit value to the buffer and increments the pointer by 1.
     *
     * @param value The value to write
     */
    void writeU8(u8 value);
    /**
     * Writes an unsigned 16 bit value to the buffer and increments the pointer by 2.
     *
     * @param value The value to write
     */
    void writeU16(u16 value);
    /**
     * Writes an unsigned 32 bit value to the buffer and increments the pointer by 4.
     *
     * @param value The value to write
     */
    void writeU32(u32 value);
    /**
     * Writes an unsigned 64 bit value to the buffer and increments the pointer by 8.
     *
     * @param value The value to write
     */
    void writeU64(u64 value);

    /**
     * Writes a signed 8 bit value to the buffer and increments the pointer by 1.
     *
     * @param value The value to write
     */
    void writeS8(s8 value);
    /**
     * Writes a signed 16 bit value to the buffer and increments the pointer by 2.
     *
     * @param value The value to write
     */
    void writeS16(s16 value);
    /**
     * Writes a signed 32 bit value to the buffer and increments the pointer by 4.
     *
     * @param value The value to write
     */
    void writeS32(s32 value);
    /**
     * Writes a signed 64 bit value to the buffer and increments the pointer by 8.
     *
     * @param value The value to write
     */
    void writeS64(s64 value);

    /**
     * Reads an unsigned 8 bit value from the buffer and increments the pointer by 1.
     *
     * @return The read value
     */
    [[nodiscard]] u8 readU8(void);
    /**
     * Reads an unsigned 16 bit value from the buffer and increments the pointer by 2.
     *
     * @return The read value
     */
    [[nodiscard]] u16 readU16(void);
    /**
     * Reads an unsigned 32 bit value from the buffer and increments the pointer by 4.
     *
     * @return The read value
     */
    [[nodiscard]] u32 readU32(void);
    /**
     * Reads an unsigned 64 bit value from the buffer and increments the pointer by 8.
     *
     * @return The read value
     */
    [[nodiscard]] u64 readU64(void);

    /**
     * Reads a signed 8 bit value from the buffer and increments the pointer by 1.
     *
     * @return The read value
     */
    [[nodiscard]] s8 readS8(void);
    /**
     * Reads a signed 16 bit value from the buffer and increments the pointer by 2.
     *
     * @return The read value
     */
    [[nodiscard]] s16 readS16(void);
    /**
     * Reads a signed 32 bit value from the buffer and increments the pointer by 4.
     *
     * @return The read value
     */
    [[nodiscard]] s32 readS32(void);
    /**
     * Reads a signed 64 bit value from the buffer and increments the pointer by 8.
     *
     * @return The read value
     */
    [[nodiscard]] s64 readS64(void);

private:
    /**
     * The allocated memory of this buffer.
     */
    void* buffer;
    /**
     * The length of this buffer.
     */
    size_t length;
    /**
     * The current position of this buffer.
     */
    size_t offset;
};
