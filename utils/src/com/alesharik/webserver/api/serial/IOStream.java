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

package com.alesharik.webserver.api.serial;

import com.alesharik.webserver.api.cache.object.CachedObjectFactory;
import com.alesharik.webserver.api.cache.object.NoCachedObjectFactory;
import com.alesharik.webserver.api.cache.object.Recyclable;
import com.alesharik.webserver.api.cache.object.SmartCachedObjectFactory;
import com.alesharik.webserver.exception.error.UnexpectedBehaviorError;
import com.alesharik.webserver.internals.MemoryReserveUtils;
import com.alesharik.webserver.internals.UnsafeAccess;
import org.jetbrains.annotations.NotNull;
import sun.misc.Cleaner;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UTFDataFormatException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@SuppressWarnings("unused")
interface IOStream extends Recyclable {
    @Override
    default void recycle() {
        reset();
    }

    void write(byte[] data);

    default void writeByteArray(byte[] data) {
        write(data.length);
        write(data);
    }

    void write(int i);

    void write(Integer i);

    void write(long l);

    void write(Long l);

    void write(short s);

    void write(Short s);

    void write(byte b);

    void write(Byte b);

    void write(float f);

    void write(Float f);

    void write(double d);

    void write(Double d);

    void write(char c);

    void write(Character c);

    void write(boolean b);

    void write(Boolean b);

    byte[] read(int size);

    default byte[] readByteArray() {
        int count = readint();
        return read(count);
    }

    int readint();

    Integer readInteger();

    long readlong();

    Long readLong();

    short readshort();

    Short readShort();

    byte readbyte();

    Byte readByte();

    float readfloat();

    Float readFloat();

    double readdouble();

    Double readDouble();

    char readchar();

    Character readCharacter();

    boolean readboolean();

    Boolean readBoolean();

    byte[] toByteArray();

    void reset();

    void reset(byte[] array);

    int length();

    void resetRead();

    default ObjectOutputImpl objectOutput(Serializer serializer, Object current) {
        try {
            return new ObjectOutputImpl(this, serializer, current);
        } catch (IOException e) {
            throw new UnexpectedBehaviorError(e);
        }
    }

    default ObjectInputStream objectInput(Serializer serializer, Object current) {
        try {
            return new ObjectInputImpl(this, serializer, current);
        } catch (IOException e) {
            throw new UnexpectedBehaviorError(e);
        }
    }

    final class ObjectOutputImpl extends ObjectOutputStream implements ObjectOutput {
        private final IOStream stream;
        private final Serializer serializer;
        private final Object object;

        public ObjectOutputImpl(IOStream stream, Serializer serializer, Object object) throws IOException {
            this.stream = stream;
            this.serializer = serializer;
            this.object = object;
        }

        @Override
        protected final void writeObjectOverride(Object obj) {
            if(!(obj instanceof Serializable))
                throw new IllegalArgumentException("Object is not serializable!");
            byte[] serialize = Serial.serialize((Serializable) obj);
            stream.writeByteArray(serialize);
        }

        @Override
        public void writeUnshared(Object obj) {
            writeObjectOverride(obj);
        }

        @Override
        public void defaultWriteObject() {
            byte[] bytes = serializer.serializeDefault(object);
            write(bytes);
        }

        @Override
        public PutField putFields() {
            throw new UnsupportedOperationException("Can't implement due to serialization specifics!");
        }

        @Override
        public void writeFields() {
            //FIXME
            throw new UnsupportedOperationException("Can't implement due to serialization specifics!");
        }

        @Override
        public void reset() {
            stream.reset();
        }

        @Override
        public void write(int b) {
            stream.write((byte) b);
        }

        @Override
        public void write(byte[] b) {
            stream.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            byte[] arr = new byte[len];
            System.arraycopy(b, off, arr, 0, len);
            stream.write(arr);
        }

        @Override
        public void writeBoolean(boolean v) {
            stream.write(v);
        }

        @Override
        public void writeByte(int v) {
            stream.write((byte) v);
        }

        @Override
        public void writeShort(int v) {
            stream.write(v);
        }

        @Override
        public void writeChar(int v) {
            stream.write(v);
        }

        @Override
        public void writeInt(int v) {
            stream.write(v);
        }

        @Override
        public void writeLong(long v) {
            stream.write(v);
        }

        @Override
        public void writeFloat(float v) {
            stream.write(v);
        }

        @Override
        public void writeDouble(double v) {
            stream.write(v);
        }

        @Override
        public void writeBytes(@Nonnull String s) {
            for(char c : s.toCharArray())
                writeByte(c);
        }

        @Override
        public void writeChars(@Nonnull String s) {
            for(char c : s.toCharArray())
                writeChar(c);
        }

        @Override
        public void writeUTF(@Nonnull String s) throws IOException {
            if(s.length() > Short.MAX_VALUE)
                throw new UTFDataFormatException();
            writeShort(s.length());
            write(s.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    }

    final class ObjectInputImpl extends ObjectInputStream implements ObjectInput {
        private final IOStream stream;
        private final Serializer serializer;
        private final Object current;

        public ObjectInputImpl(IOStream stream, Serializer serializer, Object current) throws IOException {
            this.stream = stream;
            this.serializer = serializer;
            this.current = current;
        }

        @Override
        public void mark(int readlimit) {
        }

        @Override
        public synchronized void reset() {
            stream.resetRead();
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        protected Object readObjectOverride() {
            return Serial.deserialize(stream.readByteArray());
        }

        @Override
        public Object readUnshared() {
            return readObjectOverride();
        }

        @Override
        public void defaultReadObject() {
            serializer.deserializeDefaultIntoObject(stream, current);
        }

        @Override
        public GetField readFields() {
            //FIXME
            throw new UnsupportedOperationException("Due to serialization specifics");
        }

        @Override
        public void registerValidation(ObjectInputValidation obj, int prio) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read() {
            return stream.readbyte();
        }

        @Override
        public int read(byte[] b) {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) {
            int read = Math.min(len, stream.length());
            byte[] bytes = stream.read(read);
            System.arraycopy(bytes, 0, b, off, read);
            return read;
        }

        @Override
        public long skip(long n) {
            if(n > Integer.MAX_VALUE)
                throw new IllegalArgumentException("This stream doesn't support length > Integer.MAX_VALUE");
            return skipBytes((int) n);
        }

        @Override
        public int available() {
            return stream.length();
        }

        @Override
        public void close() {
        }

        @Override
        public void readFully(@Nonnull byte[] b) {
            byte[] read = stream.read(b.length);
            System.arraycopy(read, 0, b, 0, b.length);
        }

        @Override
        public void readFully(@Nonnull byte[] b, int off, int len) {
            byte[] read = stream.read(len);
            System.arraycopy(read, 0, b, off, len);
        }

        @Override
        public int skipBytes(int n) {
            int len = stream.length();
            int skip = Math.min(len, n);
            stream.read(skip);
            return skip;
        }

        @Override
        public boolean readBoolean() {
            return stream.readboolean();
        }

        @Override
        public byte readByte() {
            return stream.readbyte();
        }

        @Override
        public int readUnsignedByte() {
            return stream.readbyte();
        }

        @Override
        public short readShort() {
            return stream.readshort();
        }

        @Override
        public int readUnsignedShort() {
            return stream.readshort();
        }

        @Override
        public char readChar() {
            return stream.readchar();
        }

        @Override
        public int readInt() {
            return stream.readint();
        }

        @Override
        public long readLong() {
            return stream.readlong();
        }

        @Override
        public float readFloat() {
            return stream.readfloat();
        }

        @Override
        public double readDouble() {
            return stream.readdouble();
        }

        @SuppressWarnings("deprecation")
        @Override
        public String readLine() {
            StringBuilder stringBuilder = new StringBuilder();
            char c;
            while((c = readChar()) != '\n')
                stringBuilder.append(c);
            return stringBuilder.toString();
        }

        @NotNull
        @Override
        public String readUTF() {
            short s = readShort();
            byte[] dat = stream.read(s);
            return new String(dat, StandardCharsets.UTF_8);
        }
    }

    class Factory {
        private static final CachedObjectFactory<IOStream> streams;

        static {
            String property = System.getProperty("api.serial.IOStream.factory");
            if(property == null || property.equals("smart"))
                //noinspection DuplicateExpressions
                streams = new SmartCachedObjectFactory<>(() -> UnsafeAccess.INSTANCE != null ? new Unsafe() : new Safe());
            else
                //noinspection DuplicateExpressions
                streams = new NoCachedObjectFactory<>(() -> UnsafeAccess.INSTANCE != null ? new Unsafe() : new Safe());
        }

        public static IOStream create() {
            IOStream ioStream = streams.getInstance();
            ioStream.reset();
            return ioStream;
        }

        public static IOStream create(byte[] buf) {
            IOStream ioStream = streams.getInstance();
            ioStream.reset(buf);
            return ioStream;
        }

        public static void recycle(IOStream stream) {
            streams.putInstance(stream);
        }

        private static class Safe implements IOStream {
            private byte[] buffer = new byte[16];
            private int writePos = -1;
            private int pos = -1;

            @Override
            public void write(byte[] data) {
                ensureCapacity(data.length);
                System.arraycopy(data, 0, buffer, writePos, data.length);
                writePos += data.length;
            }

            @Override
            public void write(int i) {
                ensureCapacity(4);
                buffer[writePos++] = (byte) (i >> 24);
                buffer[writePos++] = (byte) (i >> 16);
                buffer[writePos++] = (byte) (i >> 8);
                buffer[writePos++] = (byte) (i);
            }

            @Override
            public void write(Integer i) {
                ensureCapacity(4);
                buffer[writePos++] = (byte) (i >> 24);
                buffer[writePos++] = (byte) (i >> 16);
                buffer[writePos++] = (byte) (i >> 8);
                buffer[writePos++] = i.byteValue();
            }

            @Override
            public void write(long l) {
                ensureCapacity(8);
                buffer[writePos++] = (byte) (l >> 56);
                buffer[writePos++] = (byte) (l >> 48);
                buffer[writePos++] = (byte) (l >> 40);
                buffer[writePos++] = (byte) (l >> 32);
                buffer[writePos++] = (byte) (l >> 24);
                buffer[writePos++] = (byte) (l >> 16);
                buffer[writePos++] = (byte) (l >> 8);
                buffer[writePos++] = (byte) (l);
            }

            @Override
            public void write(Long l) {
                ensureCapacity(8);
                buffer[writePos++] = (byte) (l >> 56);
                buffer[writePos++] = (byte) (l >> 48);
                buffer[writePos++] = (byte) (l >> 40);
                buffer[writePos++] = (byte) (l >> 32);
                buffer[writePos++] = (byte) (l >> 24);
                buffer[writePos++] = (byte) (l >> 16);
                buffer[writePos++] = (byte) (l >> 8);
                buffer[writePos++] = l.byteValue();
            }

            @Override
            public void write(short s) {
                ensureCapacity(2);
                buffer[writePos++] = (byte) (s >> 8);
                buffer[writePos++] = (byte) s;
            }

            @Override
            public void write(Short s) {
                ensureCapacity(2);
                buffer[writePos++] = (byte) (s >> 8);
                buffer[writePos++] = s.byteValue();
            }

            @Override
            public void write(byte b) {
                ensureCapacity(1);
                buffer[writePos++] = b;
            }

            @Override
            public void write(Byte b) {
                ensureCapacity(1);
                buffer[writePos++] = b;
            }

            @Override
            public void write(float f) {
                write(Float.floatToRawIntBits(f));
            }

            @Override
            public void write(Float f) {
                write(Float.floatToRawIntBits(f));
            }

            @Override
            public void write(double d) {
                write(Double.doubleToRawLongBits(d));
            }

            @Override
            public void write(Double d) {
                write(Double.doubleToRawLongBits(d));
            }

            @Override
            public void write(char c) {
                ensureCapacity(2);
                buffer[writePos++] = (byte) (c >> 8);
                buffer[writePos++] = (byte) (c);
            }

            @Override
            public void write(Character c) {
                ensureCapacity(2);
                buffer[writePos++] = (byte) (c >> 8);
                buffer[writePos++] = (byte) ((char) c);
            }

            @Override
            public void write(boolean b) {
                ensureCapacity(1);
                buffer[writePos++] = (byte) (b ? 1 : 0);
            }

            @Override
            public void write(Boolean b) {
                ensureCapacity(1);
                buffer[writePos++] = (byte) (b ? 1 : 0);
            }

            @Override
            public byte[] read(int size) {
                checkRead(size);
                byte[] bytes = new byte[size];
                System.arraycopy(buffer, pos, bytes, 0, size);
                pos += size;
                return bytes;
            }

            @Override
            public int readint() {
                checkRead(4);
                return buffer[pos++] << 24
                        | (buffer[pos++] & 0xFF) << 16
                        | (buffer[pos++] & 0xFF) << 8
                        | (buffer[pos++] & 0xFF);
            }

            @Override
            public Integer readInteger() {
                checkRead(4);
                return buffer[pos++] << 24
                        | (buffer[pos++] & 0xFF) << 16
                        | (buffer[pos++] & 0xFF) << 8
                        | (buffer[pos++] & 0xFF);
            }

            @Override
            public long readlong() {
                checkRead(8);
                return ((long) buffer[pos++] << 56)
                        | ((long) (buffer[pos++] & 0xFF) << 48)
                        | ((long) (buffer[pos++] & 0xFF) << 40)
                        | ((long) (buffer[pos++] & 0xFF) << 32)
                        | ((long) (buffer[pos++] & 0xFF) << 24)
                        | ((long) (buffer[pos++] & 0xFF) << 16)
                        | ((long) (buffer[pos++] & 0xFF) << 8)
                        | ((long) (buffer[pos++] & 0xFF));
            }

            @Override
            public Long readLong() {
                checkRead(8);
                return ((long) buffer[pos++] << 56)
                        | ((long) (buffer[pos++] & 0xFF) << 48)
                        | ((long) (buffer[pos++] & 0xFF) << 40)
                        | ((long) (buffer[pos++] & 0xFF) << 32)
                        | ((long) (buffer[pos++] & 0xFF) << 24)
                        | ((long) (buffer[pos++] & 0xFF) << 16)
                        | ((long) (buffer[pos++] & 0xFF) << 8)
                        | ((long) (buffer[pos++] & 0xFF));
            }

            @Override
            public short readshort() {
                checkRead(2);
                return (short) ((buffer[pos++] << 8)
                        | (buffer[pos++] & 0xFF));
            }

            @Override
            public Short readShort() {
                checkRead(2);
                return (short) ((buffer[pos++] << 8)
                        | (buffer[pos++] & 0xFF));
            }

            @Override
            public byte readbyte() {
                checkRead(1);
                return buffer[pos++];
            }

            @Override
            public Byte readByte() {
                checkRead(1);
                return buffer[pos++];
            }

            @Override
            public float readfloat() {
                return Float.intBitsToFloat(readint());
            }

            @Override
            public Float readFloat() {
                return Float.intBitsToFloat(readint());
            }

            @Override
            public double readdouble() {
                return Double.longBitsToDouble(readlong());
            }

            @Override
            public Double readDouble() {
                return Double.longBitsToDouble(readlong());
            }

            @Override
            public char readchar() {
                checkRead(2);
                return (char) ((buffer[pos++] << 8)
                        | (buffer[pos++] & 0xFF));
            }

            @Override
            public Character readCharacter() {
                checkRead(2);
                return (char) ((buffer[pos++] << 8)
                        | (buffer[pos++] & 0xFF));
            }

            @Override
            public boolean readboolean() {
                checkRead(1);
                return buffer[pos++] == 1;
            }

            @Override
            public Boolean readBoolean() {
                checkRead(1);
                return buffer[pos++] == 1;
            }

            @Override
            public void reset() {
                writePos = -1;
                pos = -1;
                if(buffer.length > 256) //Do not clear small buffers
                    buffer = new byte[16];
            }

            @Override
            public void reset(byte[] array) {
                pos = -1;
                writePos = -1;
                buffer = array;
            }

            @Override
            public int length() {
                return buffer.length - pos;
            }

            @Override
            public void resetRead() {
                pos = -1;
            }

            @Override
            public byte[] toByteArray() {
                return Arrays.copyOf(buffer, writePos + 1);
            }

            private void checkRead(int size) {
                if(buffer.length - pos - size < 0)
                    throw new IndexOutOfBoundsException("Cannot read " + size + " bytes!");
            }

            private void ensureCapacity(int cap) {
                int off = buffer.length - writePos - 1 - cap;
                if(off >= 0)
                    return;
                off *= -1;
                if(off < 16)
                    off = 16;
                byte[] dat = new byte[writePos + 1 + off];
                System.arraycopy(buffer, 0, dat, 0, writePos + 1);
                buffer = dat;
            }
        }

        private static class Unsafe implements IOStream {
            private static final UnsafeAccess U = UnsafeAccess.INSTANCE;

            private long pointer;
            private int pointerSize;
            private int size;
            private int pos;

            public Unsafe() {
                size = 0;
                pos = 0;
                MemoryReserveUtils.reserveMemory(16);
                pointer = U.allocateMemory(16);
                pointerSize = 16;
                Cleaner.create(this, () -> {
                    MemoryReserveUtils.unreserveMemory(pointerSize);
                    U.freeMemory(pointer);
                });
            }

            @Override
            public void write(byte[] data) {
                ensureCapacity(data.length);
                U.copyMemory(data, UnsafeAccess.BYTE_ARRAY_BASE_OFFSET, null, pointer + size, data.length);
                size += data.length;
            }

            @Override
            public void write(int i) {
                ensureCapacity(4);
                U.putInt(pointer + size, i);
                size += 4;
            }

            @Override
            public void write(Integer i) {
                ensureCapacity(4);
                U.putInt(pointer + size, i);
                size += 4;
            }

            @Override
            public void write(long l) {
                ensureCapacity(8);
                U.putLong(pointer + size, l);
                size += 8;
            }

            @Override
            public void write(Long l) {
                ensureCapacity(8);
                U.putLong(pointer + size, l);
                size += 8;
            }

            @Override
            public void write(short s) {
                ensureCapacity(2);
                U.putShort(pointer + size, s);
                size += 2;
            }

            @Override
            public void write(Short s) {
                ensureCapacity(2);
                U.putShort(pointer + size, s);
                size += 2;
            }

            @Override
            public void write(byte b) {
                ensureCapacity(1);
                U.putByte(pointer + size, b);
                size++;
            }

            @Override
            public void write(Byte b) {
                ensureCapacity(1);
                U.putByte(pointer + size, b);
                size++;
            }

            @Override
            public void write(float f) {
                ensureCapacity(4);
                U.putFloat(pointer + size, f);
                size += 4;
            }

            @Override
            public void write(Float f) {
                ensureCapacity(4);
                U.putFloat(pointer + size, f);
                size += 4;
            }

            @Override
            public void write(double d) {
                ensureCapacity(8);
                U.putDouble(pointer + size, d);
                size += 8;
            }

            @Override
            public void write(Double d) {
                ensureCapacity(8);
                U.putDouble(pointer + size, d);
                size += 8;
            }

            @Override
            public void write(char c) {
                ensureCapacity(2);
                U.putChar(pointer + size, c);
                size += 2;
            }

            @Override
            public void write(Character c) {
                ensureCapacity(2);
                U.putChar(pointer + size, c);
                size += 2;
            }

            @Override
            public void write(boolean b) {
                ensureCapacity(1);
                U.putBoolean(pointer + size, b);
                size += 1;
            }

            @Override
            public void write(Boolean b) {
                ensureCapacity(1);
                U.putBoolean(pointer + size, b);
                size += 1;
            }

            @Override
            public byte[] read(int size) {
                checkRead(size);
                byte[] b = new byte[size];
                U.copyMemory(null, pointer + pos, b, UnsafeAccess.BYTE_ARRAY_BASE_OFFSET, size);
                pos += b.length;
                return b;
            }

            @Override
            public int readint() {
                checkRead(4);
                pos += 4;
                return U.getInt(pointer + pos - 4);
            }

            @Override
            public Integer readInteger() {
                checkRead(4);
                pos += 4;
                return U.getInt(pointer + pos - 4);
            }

            @Override
            public long readlong() {
                checkRead(8);
                pos += 8;
                return U.getLong(pointer + pos - 8);
            }

            @Override
            public Long readLong() {
                checkRead(8);
                pos += 8;
                return U.getLong(pointer + pos - 8);
            }

            @Override
            public short readshort() {
                checkRead(2);
                pos += 2;
                return U.getShort(pointer + pos - 2);
            }

            @Override
            public Short readShort() {
                checkRead(2);
                pos += 2;
                return U.getShort(pointer + pos - 2);
            }

            @Override
            public byte readbyte() {
                checkRead(1);
                pos++;
                return U.getByte(pointer + pos - 1);
            }

            @Override
            public Byte readByte() {
                checkRead(1);
                pos++;
                return U.getByte(pointer + pos - 1);
            }

            @Override
            public float readfloat() {
                checkRead(4);
                pos += 4;
                return U.getFloat(pointer + pos - 4);
            }

            @Override
            public Float readFloat() {
                checkRead(4);
                pos += 4;
                return U.getFloat(pointer + pos - 4);
            }

            @Override
            public double readdouble() {
                checkRead(8);
                pos += 8;
                return U.getDouble(pointer + pos - 8);
            }

            @Override
            public Double readDouble() {
                checkRead(8);
                pos += 8;
                return U.getDouble(pointer + pos - 8);
            }

            @Override
            public char readchar() {
                checkRead(2);
                pos += 2;
                return U.getChar(pointer + pos - 2);
            }

            @Override
            public Character readCharacter() {
                checkRead(2);
                pos += 2;
                return U.getChar(pointer + pos - 2);
            }

            @Override
            public boolean readboolean() {
                checkRead(1);
                pos += 1;
                return U.getBoolean(pointer + pos - 1);
            }

            @Override
            public Boolean readBoolean() {
                checkRead(1);
                pos += 1;
                return U.getBoolean(pointer + pos - 1);
            }

            @Override
            public byte[] toByteArray() {
                byte[] b = new byte[size];
                U.copyMemory(null, pointer, b, UnsafeAccess.BYTE_ARRAY_BASE_OFFSET, size);
                return b;
            }

            @Override
            public void reset() {
                size = 0;
                pos = 0;
                MemoryReserveUtils.unreserveMemory(pointerSize);
                U.freeMemory(pointer);
                MemoryReserveUtils.reserveMemory(16);
                pointer = U.allocateMemory(16);
                pointerSize = 16;
            }

            @Override
            public void reset(byte[] array) {
                size = 0;
                pos = 0;
                MemoryReserveUtils.unreserveMemory(pointerSize);
                U.freeMemory(pointer);
                MemoryReserveUtils.reserveMemory(array.length);
                pointer = U.allocateMemory(array.length);
                pointerSize = array.length;
                U.copyMemory(array, UnsafeAccess.BYTE_ARRAY_BASE_OFFSET, null, pointer, array.length);
            }

            @Override
            public int length() {
                return pointerSize - pos;
            }

            @Override
            public void resetRead() {
                pos = 0;
            }

            private void ensureCapacity(int cap) {
                int s = pointerSize - size - cap;
                if(s < 0) {
                    s *= -1;
                    if(s < 16)
                        s = 16;
                    MemoryReserveUtils.reserveMemory(s);
                    pointer = U.reallocateMemory(pointer, s + pointerSize);
                    pointerSize += s;
                }
            }

            private void checkRead(int size) {
                if(pointerSize - pos - size < 0)
                    throw new IndexOutOfBoundsException("Cannot read " + size + " bytes!");
            }
        }
    }
}
