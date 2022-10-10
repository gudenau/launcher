package net.gudenau.launcher.impl.util;

import net.gudenau.launcher.api.util.functional.ExceptionalRunnable;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A simple method to ensure that files are saved at a regular interval to combat data losses.
 */
public final class AutoSaver {
    /**
     * The {@link Timer} instance.
     */
    private static final Timer TIMER;
    /**
     * All registered callbacks when we need to save files.
     */
    private static final Set<ExceptionalRunnable<? extends IOException>> SAVERS = new HashSet<>();
    
    static {
        // Save on exit too.
        Runtime.getRuntime().addShutdownHook(new Thread(AutoSaver::save, "autosave"));
        TIMER = new Timer("AutoSave", true);
        var delay = TimeUnit.MINUTES.toMillis(1);
        TIMER.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                save();
            }
        }, delay, delay);
    }
    
    /**
     * Registers a callback to save files on.
     *
     * @param task The callback
     */
    public static void registerSaver(ExceptionalRunnable<? extends IOException> task) {
        synchronized (SAVERS) {
            SAVERS.add(task);
        }
    }
    
    /**
     * Calls all registered callbacks so they can save files.
     */
    private static void save() {
        synchronized (SAVERS) {
            Set<IOException> exceptions = new HashSet<>();
            ThreadUtil.waitFor(SAVERS.stream().<Runnable>map((task) -> () -> {
                try {
                    task.runExceptionally();
                } catch (IOException e) {
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                }
            }).collect(Collectors.toUnmodifiableSet()));
            
            if (!exceptions.isEmpty()) {
                var exception = new RuntimeException("Failed to save");
                exceptions.forEach(exception::addSuppressed);
                throw exception;
            }
        }
    }
}
