package net.gudenau.launcher.impl.util;

import sun.misc.Unsafe;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class UnsafeHelper {
    private static final Unsafe UNSAFE = findUnsafe();
    private static final long ACCESSIBLE_OFFSET = findAccessibleOffset();
    
    public static <T extends AccessibleObject> T setAccessible(T object, boolean accessible) {
        Objects.requireNonNull(object, "object can't be null");
        putBoolean(object, ACCESSIBLE_OFFSET, accessible);
        return object;
    }
    
    public static boolean getBoolean(Object instance, long offset) {
        return UNSAFE.getBoolean(instance, offset);
    }
    
    public static void putBoolean(Object instance, long offset, boolean value) {
        UNSAFE.putBoolean(instance, offset, value);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T allocateInstance(Class<T> type) {
        try {
            return (T) UNSAFE.allocateInstance(type);
        } catch (InstantiationException e) {
            throw new RuntimeException("Failed to allocate instance of " + MiscUtil.className(type), e);
        }
    }
    
    private static Unsafe findUnsafe() {
        Set<Throwable> exceptions = new HashSet<>();
        
        for (var field : Unsafe.class.getDeclaredFields()) {
            if(field.getType() == Unsafe.class && Modifier.isStatic(field.getModifiers())) {
                try {
                    field.setAccessible(true);
                    if(field.get(null) instanceof Unsafe unsafe) {
                        return unsafe;
                    }
                } catch (Throwable e) {
                    exceptions.add(e);
                }
            }
        }
        
        var exception = new RuntimeException("Failed to find Unsafe");
        exceptions.forEach(exception::addSuppressed);
        throw exception;
    }
    
    private static long findAccessibleOffset() {
        var object = allocateInstance(AccessibleObject.class);
        for(long offset = 4; offset < 64; offset++) {
            object.setAccessible(false);
            if(getBoolean(object, offset)) {
                continue;
            }
            
            object.setAccessible(true);
            if(getBoolean(object, offset)) {
                return offset;
            }
        }
        
        throw new RuntimeException("Failed to find offset of AccessibleObject.override");
    }
    
    private UnsafeHelper() {
        throw new AssertionError();
    }
}
