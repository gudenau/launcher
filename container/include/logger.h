#pragma once

#include <cstdio>
#include <cstdlib>

// A simple logging implementation.

/**
 * Prints a "debug" message to stdout.
 */
#define DEBUG(format, ...) do{ fprintf(stdout, format "\n", ##__VA_ARGS__ ); fflush(stdout); } while(false)
/**
 * Prints a "fatal" message to stderr.
 */
#define FATAL(format, ...) do{ fprintf(stderr, format "\n", ##__VA_ARGS__ ); fflush(stdout); } while(false)
/**
 * Prints a "fatal" message to stderr then aborts.
 */
#define ABORT(format, ...) do{ FATAL(format, ##__VA_ARGS__ ); abort(); } while(false)
/**
 * If result is non-zero prints a "fatal" message to stderr then aborts.
 */
#define ASSERT(result, format, ...) do{ if(result) { ABORT(format, ##__VA_ARGS__ ); } } while(false)
