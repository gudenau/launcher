#include <cstdlib>
#include <cstring>

#include "types.h"
#include "logger.h"

#include "Buffer.h"

Buffer::Buffer(size_t initialSize) {
    this->length = initialSize;
    this->offset = 0;
    this->buffer = calloc(1, initialSize);
    ASSERT(!this->buffer, "Failed to allocate Buffer::buffer");
}
Buffer::~Buffer(void) {
    free(this->buffer);
}

void* Buffer::pointer(void) {
    // size_t should always be the same size as void*.
    return (void*)(((size_t)this->buffer) + this->offset);
}
void* Buffer::rawPointer(void) {
    return this->buffer;
}
size_t Buffer::size(void) const {
    return this->length;
}
size_t Buffer::position(void) const {
    return this->offset;
}
size_t Buffer::position(size_t position) {
    auto original = this->offset;
    ASSERT(position <= this->length, "Buffer::position out of bounds");
    this->offset = position;
    return original;
}
size_t Buffer::remaining(void) const {
    return this->length - this->offset;
}

void Buffer::write(const void* data, size_t bytes) {
    ASSERT(this->offset + bytes > this->length, "Buffer::write overflow");
    memcpy(pointer(), data, bytes);
    this->offset += bytes;
}
void Buffer::read(void* data, size_t bytes) {
    ASSERT(this->offset + bytes > this->length, "Buffer::read overflow");
    memcpy(data, pointer(), bytes);
    this->offset += bytes;
}

void Buffer::writeU8(u8 value) {
    write(&value, sizeof(value));
}
void Buffer::writeU16(u16 value) {
    write(&value, sizeof(value));
}
void Buffer::writeU32(u32 value) {
    write(&value, sizeof(value));
}
void Buffer::writeU64(u64 value) {
    write(&value, sizeof(value));
}

void Buffer::writeS8(s8 value) {
    write(&value, sizeof(value));
}
void Buffer::writeS16(s16 value) {
    write(&value, sizeof(value));
}
void Buffer::writeS32(s32 value) {
    write(&value, sizeof(value));
}
void Buffer::writeS64(s64 value) {
    write(&value, sizeof(value));
}

u8 Buffer::readU8(void) {
    u8 value;
    read(&value, sizeof(value));
    return value;
}
u16 Buffer::readU16(void) {
    u16 value;
    read(&value, sizeof(value));
    return value;
}
u32 Buffer::readU32(void) {
    u32 value;
    read(&value, sizeof(value));
    return value;
}
u64 Buffer::readU64(void) {
    u64 value;
    read(&value, sizeof(value));
    return value;
}

s8 Buffer::readS8(void) {
    s8 value;
    read(&value, sizeof(value));
    return value;
}
s16 Buffer::readS16(void) {
    s16 value;
    read(&value, sizeof(value));
    return value;
}
s32 Buffer::readS32(void) {
    s32 value;
    read(&value, sizeof(value));
    return value;
}
s64 Buffer::readS64(void) {
    s64 value;
    read(&value, sizeof(value));
    return value;
}
