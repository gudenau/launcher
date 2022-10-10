package net.gudenau.launcher.impl.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class LookupHelper {
    private static final MethodHandles.Lookup LOOKUP = createLookup();
    
    @SuppressWarnings("unchecked")
    public static <T> Class<T> findClass(String name) {
        try {
            return (Class<T>) Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find class " + name, e);
        }
    }
    
    public static MethodHandle findStatic(Class<?> owner, String name, MethodType type) {
        try {
            return LOOKUP.findStatic(owner, name, type);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("Failed to find static method " + MiscUtil.className(owner) + "." + name + type, e);
        }
    }
    
    public static MethodHandle findSetter(Class<?> owner, String name, Class<?> type) {
        try {
            return LOOKUP.findSetter(owner, name, type);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to find setter for " + MiscUtil.className(owner) + "." + name, e);
        }
    }
    
    private static MethodHandles.Lookup createLookup() {
        try {
            return UnsafeHelper.setAccessible(
                MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Class.class, int.class),
                true
            ).newInstance(Object.class, null, -1);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create lookup", e);
        }
    }
    
    private LookupHelper() {
        throw new AssertionError();
    }
}
