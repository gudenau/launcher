#include <cstdlib>
#include <cstring>
#include <cerrno>

#include <fcntl.h>
#include <unistd.h>

#include "logger.h"

#include "File.h"

File::File(const char* path) {
    auto result = open(path, O_WRONLY);
    if(result == -1) {
        FATAL("Failed to open %s: %s", path, strerror(errno));
        abort();
    }
    this->handle = result;
}

File::~File(void) {
    if(close(this->handle)) {
        FATAL("Failed to close File: %s", strerror(errno));
        abort();
    }
}

size_t File::write(const void* buffer, size_t length) const {
    auto writeHandle = this->handle;

    size_t remaining = length;
    ssize_t written;
    while(remaining) {
        written = ::write(writeHandle, buffer, remaining);
        ASSERT(written <= 0, "Failed to write buffer: %s", strerror(errno));
        remaining -= written;
        buffer = (void*)(((size_t)buffer) + written);
    }

    return length;
}

size_t File::print(const char* format, ...) const {
    char buffer[4096 + 1];
    memset(buffer, 0, sizeof(buffer));
    va_list args;
    va_start(args, format);
    vsnprintf(buffer, sizeof(buffer) - 1, format, args);
    va_end(args);

    return write(buffer, strlen(buffer));
}
