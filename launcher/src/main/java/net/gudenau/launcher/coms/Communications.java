package net.gudenau.launcher.coms;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.gudenau.launcher.impl.util.LookupHelper;
import net.gudenau.launcher.impl.util.MiscUtil;
import net.gudenau.launcher.impl.util.UnsafeHelper;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Pipe;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public final class Communications {
    private static Pipe.SourceChannel source;
    private static Pipe.SinkChannel sink;
    private static Thread thread;
    
    private static final Int2ObjectMap<Supplier<Packet>> PACKET_FACTORIES = new Int2ObjectOpenHashMap<>();
    private static final Object2IntMap<Class<? extends Packet>> PACKET_IDS = new Object2IntOpenHashMap<>();
    static {
        PACKET_FACTORIES.defaultReturnValue(null);
        PACKET_IDS.defaultReturnValue(-1);
        
        register(1, ContainerVersionPacket::new);
    }
    
    private static void register(int id, Supplier<Packet> factory) {
        PACKET_FACTORIES.put(id, factory);
        PACKET_IDS.put(factory.get().getClass(), id);
    }
    
    private static final class Waiter<T extends Packet> {
        private final Class<T> type;
        private T value;
        
        private Waiter(Class<T> type) {
            this.type = type;
        }
    }
    
    private static final List<Waiter> WAITERS = new LinkedList<>();
    
    private static void packetReader() {
        while(true) {
            try {
                var packet = readPacket();
                var type = packet.getClass();
                synchronized (WAITERS) {
                    for (var waiter : WAITERS) {
                        if(waiter.type == type) {
                            waiter.value = packet;
                            synchronized (waiter) {
                                waiter.notify();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    static <T extends Packet> T waitForPacket(Class<T> type) {
        try {
            return readPacket(type).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to wait for packet", e);
        }
    }
    
    static <T extends Packet> CompletableFuture<T> readPacket(Class<T> type) {
        return CompletableFuture.supplyAsync(() -> {
            var waiter = new Waiter<>(type);
            synchronized (WAITERS) {
                WAITERS.add(waiter);
            }
            synchronized (waiter) {
                if(waiter.value != null) {
                    return waiter.value;
                }
                try {
                    waiter.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return waiter.value;
            }
        });
    }
    
    static void writePacket(Packet packet) throws IOException {
        if(sink == null) {
            throw new IOException("ContainerManager is disabled");
        }
        
        int id = PACKET_IDS.getInt(packet.getClass());
        if(id == -1) {
            throw new IllegalArgumentException("Packet " + MiscUtil.className(packet.getClass()) + " is not registered");
        }
        var packetBuffer = packet.write();
        var buffer = ByteBuffer.allocateDirect(8 + packetBuffer.remaining())
            .order(ByteOrder.nativeOrder());
        buffer.putInt(0, id);
        buffer.putInt(4, packetBuffer.remaining());
        if(packetBuffer.hasRemaining()) {
            buffer.put(8, packetBuffer, 0, packetBuffer.remaining());
        }
        
        synchronized (Communications.class) {
            while(buffer.hasRemaining()) {
                sink.write(buffer);
            }
        }
    }
    
    static Packet readPacket() throws IOException {
        if(sink == null) {
            throw new IOException("ContainerManager is disabled");
        }
        
        int id;
        int size;
        var buffer = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder());
        synchronized (Communications.class) {
            while(buffer.hasRemaining()) {
                source.read(buffer);
            }
            
            id = buffer.getInt(0);
            size = buffer.getInt(4);
            buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
            
            while(buffer.hasRemaining()) {
                source.read(buffer);
            }
            buffer.flip();
        }
        
        var factory = PACKET_FACTORIES.get(id);
        if(factory == null) {
            throw new IOException("Unknown packet ID: " + id);
        }
        var packet = factory.get();
        packet.read(buffer);
        return packet;
    }
    
    public static void disable() {
        synchronized (Communications.class) {
            if(source != null) {
                try {
                    source.close();
                } catch (IOException ignored) {}
                try {
                    sink.close();
                } catch (IOException ignored) {}
                source = null;
                sink = null;
            }
        }
    }
    
    //FIXME This is one awful hack and a half. Find a better way or actually implement stuff
    public static void init(int readPipe, int writePipe) throws IOException {
        try {
            var IOUtil = LookupHelper.findClass("sun.nio.ch.IOUtil");
            var IOUtil$setfdVal = LookupHelper.findStatic(IOUtil, "setfdVal", MethodType.methodType(void.class, FileDescriptor.class, int.class));
        
            var SourceChannelImpl = LookupHelper.findClass("sun.nio.ch.SourceChannelImpl");
            var SourceChannelImpl$fd = LookupHelper.findSetter(SourceChannelImpl, "fd", FileDescriptor.class);
            var SourceChannelImpl$fdVal = LookupHelper.findSetter(SourceChannelImpl, "fdVal", int.class);
            var SourceChannelImpl$readLock = LookupHelper.findSetter(SourceChannelImpl, "readLock", ReentrantLock.class);
            var SourceChannelImpl$stateLock = LookupHelper.findSetter(SourceChannelImpl, "stateLock", Object.class);
        
            var SinkChannelImpl = LookupHelper.findClass("sun.nio.ch.SinkChannelImpl");
            var SinkChannelImpl$fd = LookupHelper.findSetter(SinkChannelImpl, "fd", FileDescriptor.class);
            var SinkChannelImpl$fdVal = LookupHelper.findSetter(SinkChannelImpl, "fdVal", int.class);
            var SinkChannelImpl$writeLock = LookupHelper.findSetter(SinkChannelImpl, "writeLock", ReentrantLock.class);
            var SinkChannelImpl$stateLock = LookupHelper.findSetter(SinkChannelImpl, "stateLock", Object.class);
    
            var AbstractSelectableChannel$keyLock = LookupHelper.findSetter(AbstractSelectableChannel.class, "keyLock", Object.class);
            var AbstractSelectableChannel$regLock = LookupHelper.findSetter(AbstractSelectableChannel.class, "regLock", Object.class);
            
            var AbstractInterruptibleChannel$closeLock = LookupHelper.findSetter(AbstractInterruptibleChannel.class, "closeLock", Object.class);
            
            var sourceFd = new FileDescriptor();
            IOUtil$setfdVal.invokeExact(sourceFd, readPipe);
            var sourceChannel = UnsafeHelper.allocateInstance(SourceChannelImpl);
            SourceChannelImpl$fd.invoke(sourceChannel, sourceFd);
            SourceChannelImpl$fdVal.invoke(sourceChannel, readPipe);
            SourceChannelImpl$readLock.invoke(sourceChannel, new ReentrantLock());
            SourceChannelImpl$stateLock.invoke(sourceChannel, new Object());
            AbstractSelectableChannel$keyLock.invoke(sourceChannel, new Object());
            AbstractSelectableChannel$regLock.invoke(sourceChannel, new Object());
            AbstractInterruptibleChannel$closeLock.invoke(sourceChannel, new Object());
    
            var sinkFd = new FileDescriptor();
            IOUtil$setfdVal.invokeExact(sinkFd, writePipe);
            var sinkChannel = UnsafeHelper.allocateInstance(SinkChannelImpl);
            SinkChannelImpl$fd.invoke(sinkChannel, sinkFd);
            SinkChannelImpl$fdVal.invoke(sinkChannel, writePipe);
            SinkChannelImpl$writeLock.invoke(sinkChannel, new ReentrantLock());
            SinkChannelImpl$stateLock.invoke(sinkChannel, new Object());
            AbstractSelectableChannel$keyLock.invoke(sinkChannel, new Object());
            AbstractSelectableChannel$regLock.invoke(sinkChannel, new Object());
            AbstractInterruptibleChannel$closeLock.invoke(sinkChannel, new Object());
            
            source = (Pipe.SourceChannel) sourceChannel;
            sink = (Pipe.SinkChannel) sinkChannel;
            
            Runtime.getRuntime().addShutdownHook(new Thread(Communications::disable, "IPC Cleanup"));
            
            thread = new Thread(Communications::packetReader, "IPC Reader");
        } catch (Throwable e) {
            throw new RuntimeException("Failed to setup IPC", e);
        }
    }
    
    private Communications() {
        throw new AssertionError();
    }
}
