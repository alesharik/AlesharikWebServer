/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.alesharik.webserver.api.qt;

import com.alesharik.webserver.api.ByteOrderUtils;
import lombok.Getter;
import lombok.Setter;
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
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Use OffHeap for work
 */
@NotThreadSafe
public class QDataStream implements DataInput, DataOutput, AutoCloseable {
    private static final int DEFAULT_SIZE = 16;
    private static final Unsafe U = JavaInternals.getUnsafe();

    private QDataStreamStrategy strategy;
    @Setter
    @Getter
    private ByteOrder order;

    private long address;
    private long size;

    private long readCursor;
    private long writeCursor;

    public QDataStream() {
        this(DEFAULT_SIZE);
    }

    public QDataStream(int capacity, @Nonnull QDataStreamStrategy streamStrategy) {
        this.address = U.allocateMemory(capacity);
        this.size = capacity;
        this.readCursor = 0;
        this.writeCursor = 0;
        this.strategy = streamStrategy;
        this.order = ByteOrder.nativeOrder();

        Cleaner.create(this, () -> U.freeMemory(address));
    }

    public QDataStream(int capacity) {
        this(capacity, FormatStrategy.QT_4_4);
    }

    public QDataStream(byte[] data) {
        this(data.length);
        write(data);
    }

    public long skip(long n) {
        long delta = Math.min(n, (size - readCursor));
        readCursor += delta;
        return delta;
    }

    public long availableRead() {
        return size - readCursor;
    }

    public long availableWrite() {
        return size - writeCursor;
    }

    @Override
    public void write(int b) {
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

    @Override
    public byte readByte() {
        return U.getByte(read(1));
    }

    /**
     * quint8
     */
    public void writeUnsignedByte(int b) {
        U.putByte(allocFor(1), (byte) (b & 0xFF));
    }

    @Override
    public int readUnsignedByte() {
        return U.getByte(read(1)) & 0xFF;
    }

    @Override
    public void writeBoolean(boolean v) {
        U.putByte(allocFor(1), (byte) (v ? 0x1 : 0x0));
    }

    @Override
    public boolean readBoolean() {
        return U.getByte(read(1)) == 0x1;
    }

    /**
     * qint16
     */
    @Override
    public void writeShort(int v) {
        U.putShort(allocFor(2), ByteOrderUtils.format((short) v, order));
    }

    /**
     * qint16
     */
    public void writeShort(short s) {
        U.putShort(allocFor(2), ByteOrderUtils.format(s, order));
    }

    @Override
    public short readShort() throws IOException {
        return ByteOrderUtils.format(U.getShort(read(2)), order);
    }

    /**
     * quint16
     */
    public void writeUnsignedShort(short s) {
        U.putShort(allocFor(2), ByteOrderUtils.format((short) (s & 0xffff), order));
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return ByteOrderUtils.format((short) (U.getShort(read(2)) & 0xffff), order);
    }

    @Override
    public void writeChar(int v) throws IOException {
        U.putChar(allocFor(2), ByteOrderUtils.format((char) v, order));
    }

    public void writeChar(char c) {
        U.putChar(allocFor(2), ByteOrderUtils.format(c, order));
    }

    @Override
    public char readChar() throws IOException {
        return U.getChar(read(2));
    }

    /**
     * qint32
     */
    @Override
    public void writeInt(int v) {
        U.putInt(allocFor(4), ByteOrderUtils.format(v, order));
    }

    @Override
    public int readInt() {
        return ByteOrderUtils.format(U.getInt(read(4)), order);
    }

    /**
     * quint32
     */
    public void writeUnsignedInt(int v) {
        U.putInt(allocFor(4), ByteOrderUtils.format((int) (v & 0xffffffffL), order));
    }

    public int readUnsignedInt() {
        return ByteOrderUtils.format((int) (U.getInt(read(4)) & 0xffffffffL), order);
    }

    /**
     * qint64
     */
    @Override
    public void writeLong(long v) {
        U.putLong(allocFor(8), ByteOrderUtils.format(v, order));
    }

    @Override
    public long readLong() {
        return ByteOrderUtils.format(U.getLong(read(8)), order);
    }

    /**
     * quint64
     */
    public void writeUnsignedLong(long l) {
        U.putLong(allocFor(8), ByteOrderUtils.format(l, order));
    }

    public long readUnsignedLong() {
        return ByteOrderUtils.format(U.getLong(read(8)), order);
    }


    @Override
    public void writeFloat(float v) {
        U.putFloat(allocFor(4), ByteOrderUtils.format(v, order));
    }

    @Override
    public void writeDouble(double v) {
        U.putDouble(allocFor(8), ByteOrderUtils.format(v, order));
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
        U.freeMemory(address);
        address = 0;
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) {
        long delta = Math.min(len, (size - readCursor));
        U.copyMemory(null, address + readCursor, b, JavaInternals.byteArrayOffset + (long) off, delta);
        readCursor += delta;
    }

    public long read(byte[] b, int off, int len) {
        long delta = Math.min(len, (size - readCursor));
        U.copyMemory(null, address + readCursor, b, JavaInternals.byteArrayOffset + (long) off, delta);
        readCursor += delta;
        return delta;
    }

    public long size() {
        return size;
    }

    @Override
    public int skipBytes(int n) {
        long delta = Math.min((size - readCursor), n);
        readCursor += delta;
        return (int) delta;
    }


    @Override
    public float readFloat() {
        return ByteOrderUtils.format(U.getFloat(read(4)), order);
    }

    @Override
    public double readDouble() {
        return ByteOrderUtils.format(U.getDouble(read(8)), order);
    }

    /**
     * Return same as {@link #readUTF()}
     */
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

    /**
     * Return memory address for read next block
     *
     * @param count how many your block require
     * @return memory address
     * @throws IllegalStateException if this instance doesn't have something for read
     */
    private long read(long count) {
        if(readCursor + count > size)
            throw new IllegalStateException("Read end");
        long addr = address + readCursor;
        readCursor += count;
        return addr;
    }

    /**
     * Return memory address for block to write. Can reallocate memory
     * @param count how many space you need
     * @return memory address
     */
    private long allocFor(int count) {
        allocateRequired(count);
        long address = this.address + writeCursor;
        writeCursor += count;
        return address;
    }

    /**
     * Allocate only needed space
     * @param count how many space you need
     */
    private void allocateRequired(long count) {
        long required = (size - writeCursor - count) * -1;
        if(required > 0) {
            allocate(size + required);
        }
    }

    /**
     * Allocate memory and set new size
     * @param count new memory byte count
     */
    private void allocate(long count) {
        address = U.reallocateMemory(address, count);
        size = count;
    }

    public enum FormatStrategy implements QDataStreamStrategy {
        QT_4_4 {
            @Override
            public String readString(Unsafe unsafe, QDataStream dataStream) {
                int size = dataStream.readUnsignedInt();
                if(size == 0xFFFFFFFF)
                    return "";

                byte[] data = new byte[size];
                for(int i = 0; i < size; i++) {
                    data[i] = dataStream.readByte();
                }
                return new String(data, Charset.forName("UTF-8")).replaceAll("\u0000", "");
            }

            @SneakyThrows
            @Override
            public void writeString(Unsafe unsafe, String s, QDataStream dataStream) {
                dataStream.writeUnsignedInt(s.length() == 0 ? 0xFFFFFFFF : s.length());
                dataStream.write(s.getBytes("UTF-8"));
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
