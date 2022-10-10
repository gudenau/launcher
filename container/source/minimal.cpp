#include <cstdlib>
#include <functional>

#include "logger.h"

// Basic "minimal C++" stuff.

void* operator new(size_t size) {
    auto result = calloc(1, size);
    ASSERT(!result, "Failed to allocate %ld bytes for new", size);
    return result;
}

void* operator new[](size_t size) {
    auto result = calloc(1, size);
    ASSERT(!result, "Failed to allocate %ld bytes for new[]", size);
    return result;
}

void operator delete(void* pointer) {
    if(pointer) {
        free(pointer);
    }
}

void operator delete[](void* pointer) {
    if(pointer) {
        free(pointer);
    }
}

void std::__throw_bad_function_call(void) {
    ABORT("Bad std::function call");
}

extern "C" void __cxa_pure_virtual(void) {
    ABORT("Pure virtual call");
}
