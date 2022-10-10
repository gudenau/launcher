package net.gudenau.launcher.impl.util;

import net.gudenau.launcher.api.util.LogLevel;
import net.gudenau.launcher.api.util.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class LoggerImpl implements Logger {
    private static final PrintStream STREAM = System.out;
    private static final LogLevel LEVEL;
    private static final Map<String, Logger> LOGGERS = new HashMap<>();
    private static final String RESET = "\u001B[0m";
    
    static {
        System.setOut(new PrintStream(new LoggerOutputStream(LogLevel.INFO, forName("stdout")), false, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new LoggerOutputStream(LogLevel.ERROR, forName("stderr")), false, StandardCharsets.UTF_8));
        
        var levelString = Configuration.LOG_LEVEL.get();
        LogLevel level = LogLevel.DEBUG;
        for (var value : LogLevel.values()) {
            if(value.name().equalsIgnoreCase(levelString)) {
                level = value;
                break;
            }
        }
        LEVEL = level;
    }
    
    @NotNull
    public static Logger forName(@NotNull String name) {
        synchronized (LOGGERS) {
            return LOGGERS.computeIfAbsent(name, LoggerImpl::new);
        }
    }
    
    private final String name;
    
    private LoggerImpl(String name) {
        this.name = name;
    }
    
    @Override
    public void log(@NotNull LogLevel level, @Nullable Throwable exception, @NotNull CharSequence message) {
        if (level.ordinal() > LEVEL.ordinal()) {
            return;
        }
        
        var prefix = switch(level) {
            case FATAL -> "\u001B[38;2;255;0;0m[F] ";
            case ERROR -> "\u001B[38;2;220;0;0m[E] ";
            case WARNING -> "\u001B[38;2;200;200;2m[W] ";
            case INFO -> "\u001B[38;2;200;200;200m[I] ";
            case DEBUG -> "\u001B[38;2;150;150;150m[D] ";
        } + '[' + name + "] ";
        
        var formattedMessage = message.toString().lines()
            .collect(Collectors.joining(RESET + '\n' + prefix, prefix, RESET));
        
        String formattedException;
        if (exception == null) {
            formattedException = null;
        } else {
            var writer = new StringWriter();
            exception.printStackTrace(new PrintWriter(writer));
            formattedException = ("Stack trace: " + writer).lines()
                .collect(Collectors.joining(RESET + '\n' + prefix, prefix, RESET));
        }
        
        synchronized (STREAM) {
            STREAM.println(formattedMessage);
            if (formattedException != null) {
                STREAM.println(formattedException);
            }
            STREAM.flush();
        }
        
        if (level == LogLevel.FATAL) {
            System.exit(1);
        }
    }
}
