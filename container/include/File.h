#pragma once

#include <cstdlib>

/**
 * A basic file wrapper.
 */
class File {
public:
    /**
     * Opens an existing file for writing.
     *
     * @param path The file to open
     */
    explicit File(const char* path);
    /**
     * Closes the file.
     */
    ~File(void);

    /**
     * Writes a buffer to the opened file.
     *
     * @param buffer The data to write
     * @param length The size of the buffer
     * @return The amount of data written to the file
     */
    size_t write(const void* buffer, size_t length) const;
    /**
     * Writes a printf formatted string to the file. The string must be 4096 characters (or fewer) long, otherwise it
     * will be truncated.
     *
     * @param format The format string
     * @param ... The format arguments
     * @return The amount of data written to the file
     */
    size_t print(const char* format, ...) const;

private:
    /**
     * The file handle.
     */
    int handle;
};
