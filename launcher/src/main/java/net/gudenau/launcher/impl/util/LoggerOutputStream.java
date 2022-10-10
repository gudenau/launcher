package net.gudenau.launcher.impl.util;

import net.gudenau.launcher.api.util.LogLevel;
import net.gudenau.launcher.api.util.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.util.Objects;

final class LoggerOutputStream extends OutputStream {
    private final LogLevel level;
    private final Logger logger;
    private final StringBuilder builder = new StringBuilder();
    
    public LoggerOutputStream(LogLevel level, Logger logger) {
        this.level = level;
        this.logger = logger;
    }
    
    @Override
    public void write(int b) {
        synchronized (builder) {
            print((byte) b);
        }
    }
    
    @Override
    public void write(byte @NotNull [] b, int off, int len) {
        Objects.checkFromIndexSize(off, len, b.length);
        
        synchronized (builder) {
            new String(b, off, len).chars().forEachOrdered(this::print);
        }
    }
    
    private void print(int character) {
        if (character == '\n' || character == '\r') {
            logger.log(level, builder);
            builder.setLength(0);
        } else {
            builder.append(Character.toString(character));
        }
    }
}
