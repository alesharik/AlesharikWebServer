package com.alesharik.webserver.api.qt;

import lombok.SneakyThrows;
import net.jcip.annotations.NotThreadSafe;
import one.nio.util.JavaInternals;
import sun.misc.Cleaner;
import sun.misc.Unsafe;

import javax.annotation.Nonnull;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Use OffHeap for work
 */
@NotThreadSafe
public class QDataStream implements DataInput, DataOutput, AutoCloseable {
    private static final int DEFAULT_SIZE = 16;

    private static final Unsafe U = JavaInternals.getUnsafe();

    private QDataStreamStrategy strategy;
    private final AtomicLong address;
    private final AtomicLong size;

    private final AtomicLong readCursor;
    private final AtomicLong writeCursor;

    public QDataStream() {
        this(DEFAULT_SIZE);
    }

    public QDataStream(int capacity, @Nonnull QDataStreamStrategy streamStrategy) {
        address = new AtomicLong(U.allocateMemory(capacity));
        size = new AtomicLong(capacity);
        readCursor = new AtomicLong(0);
        writeCursor = new AtomicLong(0);
        strategy = streamStrategy;
        Cleaner.create(this, () -> U.freeMemory(address.get()));
    }

    public QDataStream(int capacity) {
        this(capacity, FormatStrategy.QT_4_4);
    }

    public QDataStream(byte[] data) {
        this(data.length);
        write(data);
    }

    public long skip(long n) {
        long delta = Math.min(n, (size.get() - readCursor.get()));
        readCursor.addAndGet(delta);
        return delta;
    }

    public int available() {
        return (int) (size.get() - readCursor.get());
    }

    @Override
    public void write(int b) throws IOException {
        writeByte(b);
    }

    @Override
    public void write(byte[] b) {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        U.copyMemory(b, JavaInternals.byteArrayOffset + off, null, allocFor(len), len);
    }

    @Override
    public void writeBoolean(boolean v) {
        U.putByte(allocFor(1), (byte) (v ? 0x1 : 0x0));
    }

    /**
     * qint8
     */
    @Override
    public void writeByte(int v) {
        U.putByte(allocFor(1), (byte) v);
    }

    /**
     * qint8
     */
    public void writeByte(byte b) {
        U.putByte(allocFor(1), b);
    }

    /**
     * quint8
     */
    public void writeUnsignedByte(int b) {
        U.putByte(allocFor(1), (byte) Integer.reverseBytes(b));
    }

    /**
     * qint16
     */
    @Override
    public void writeShort(int v) {
        U.putShort(allocFor(2), (short) v);
    }

    /**
     * qint16
     */
    public void writeShort(short s) {
        U.putShort(allocFor(2), s);
    }

    /**
     * quint16
     */
    public void writeUnsignedShort(short s) {
        U.putShort(allocFor(2), Short.reverseBytes(s));
    }

    @Override
    public void writeChar(int v) throws IOException {
        U.putChar(allocFor(2), (char) v);
    }

    public void writeChar(char c) {
        U.putChar(allocFor(2), c);
    }

    public void writeUnsignedChar(char c) {
        U.putChar(allocFor(2), Character.reverseBytes(c));
    }

    /**
     * qint32
     */
    @Override
    public void writeInt(int v) {
        U.putInt(allocFor(4), v);
    }

    /**
     * quint32
     */
    public void writeUnsignedInt(int v) {
        U.putInt(allocFor(4), Integer.reverseBytes(v));
    }

    /**
     * qint64
     */
    @Override
    public void writeLong(long v) {
        U.putLong(allocFor(8), v);
    }

    /**
     * quint32
     */
    public void writeUnsignedLong(long l) {
        U.putLong(allocFor(8), Long.reverseBytes(l));
    }

    @Override
    public void writeFloat(float v) {
        U.putFloat(allocFor(4), v);
    }

    @Override
    public void writeDouble(double v) {
        U.putDouble(allocFor(8), v);
    }

    @Override
    public void writeBytes(@Nonnull String s) {
        for(char c : s.toCharArray()) {
            writeByte(c);
        }
    }

    @SneakyThrows
    @Override
    public void writeChars(String s) {
        for(byte b : s.getBytes("UTF-16LE")) {
            writeByte(b);
        }
    }

    /**
     * QString
     */
    @SneakyThrows
    @Override
    public void writeUTF(String s) {
        strategy.writeString(U, s, this);
    }

    /**
     * Clear memory
     */
    @Override
    public void close() {
        U.freeMemory(address.get());
        address.set(0);
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) {
        long delta = Math.min(len, (size.get() - readCursor.get()));
        U.copyMemory(null, address.get() + readCursor.get(), b, JavaInternals.byteArrayOffset + (long) off, delta);
        readCursor.addAndGet(delta);
    }

    public long read(byte[] b, int off, int len) {
        long delta = Math.min(len, (size.get() - readCursor.get()));
        U.copyMemory(null, address.get() + readCursor.get(), b, JavaInternals.byteArrayOffset + (long) off, delta);
        readCursor.addAndGet(delta);
        return delta;
    }

    @Override
    public int skipBytes(int n) {
        long delta = Math.min((size.get() - readCursor.get()), n);
        readCursor.addAndGet(delta);
        return (int) delta;
    }

    @Override
    public boolean readBoolean() {
        return U.getByte(read(1)) == 0x1;
    }

    @Override
    public byte readByte() {
        return U.getByte(read(1));
    }

    @Override
    public int readUnsignedByte() {
        return Integer.reverseBytes(U.getByte(read(1)));
    }

    @Override
    public short readShort() throws IOException {
        return U.getShort(read(2));
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return Short.reverseBytes(U.getShort(read(2)));
    }

    public char readUnsignedChar() {
        return Character.reverseBytes(U.getChar(read(2)));
    }

    @Override
    public char readChar() throws IOException {
        return U.getChar(read(2));
    }

    public int readUnsignedInt() {
        return Integer.reverseBytes(U.getInt(read(4)));
    }

    @Override
    public int readInt() {
        return U.getInt(read(4));
    }

    public long readUnsignedLong() {
        return Long.reverseBytes(U.getLong(read(8)));
    }

    @Override
    public long readLong() {
        return U.getLong(read(8));
    }

    @Override
    public float readFloat() {
        return U.getFloat(read(4));
    }

    @Override
    public double readDouble() {
        return U.getDouble(read(8));
    }

    @Override
    public String readLine() {
        return readUTF();
    }

    @Override
    public String readUTF() {
        return strategy.readString(U, this);
    }

    public void writeObject(QSerializable o) {
        strategy.writeObject(U, o, this);
    }

    /**
     * @throws RuntimeException with {@link InstantiationException}
     */
    @SuppressWarnings("unchecked")
    public <T extends Object & Serializable> T readObject(Class<?> clazz) {
        try {
            return strategy.readObject(U, clazz, this);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private long read(long count) {
        long addr = address.get() + readCursor.getAndAdd(count);
        if(readCursor.get() > size.get())
            throw new IllegalStateException("Read end");
        return addr;
    }

    private long allocFor(int count) {
        allocateRequired(count);
        long addr = address.get() + writeCursor.getAndAdd(count);
        if(writeCursor.get() > size.get())
            throw new IllegalStateException("Write end");
        return addr;
    }

    /**
     * Allocate only needed space
     */
    private void allocateRequired(long count) {
        long required = (size.get() - writeCursor.get() - count) * -1;
        if(required > 0) {
            allocate(size.get() + required);
        }
    }

    private void allocate(long count) {
        address.set(U.reallocateMemory(address.get(), count));
        size.set(count);
    }

    public enum FormatStrategy implements QDataStreamStrategy {
        QT_4_4 {
            @Override
            public String readString(Unsafe unsafe, QDataStream dataStream) {
                int size = dataStream.readUnsignedInt();
                byte[] data = new byte[size * 2];
                for(int i = 0; i < size * 2; i++) {
                    data[i] = dataStream.readByte();
                }
                return new String(data, Charset.forName("UTF-16LE"));
            }

            @SneakyThrows
            @Override
            public void writeString(Unsafe unsafe, String s, QDataStream dataStream) {
                dataStream.writeUnsignedInt(s.length());
                dataStream.write(s.getBytes("UTF-16LE"));
            }

            @Override
            public void writeObject(Unsafe unsafe, QSerializable serializable, QDataStream stream) {
                serializable.write(stream);
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T extends QSerializable> T readObject(Unsafe unsafe, Class<?> clazz, QDataStream stream) throws InstantiationException {
                T instance;
                try {
                    Constructor<?> emptyConstructor = clazz.getDeclaredConstructor();
                    emptyConstructor.setAccessible(true);
                    instance = (T) emptyConstructor.newInstance();
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    instance = (T) unsafe.allocateInstance(clazz);
                }
                instance.read(stream);
                return instance;
            }
        }
    }
}
