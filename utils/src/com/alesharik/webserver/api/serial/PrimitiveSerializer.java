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

abstract class PrimitiveSerializer implements Serializer {

    @Override
    public double getVersion() {
        return -1;
    }

    @Override
    public byte[] serialize(Object o) {
        return serializeDefault(o);
    }

    @Override
    public Object deserialize(byte[] data) {
        return deserializeDefault(data);
    }

    @Override
    public void deserializeDefaultIntoObject(IOStream data, Object o) {
        throw new UnsupportedOperationException("Cannot deserialize fields into primitive wrapper type!");
    }

    static final class IntSerializer extends PrimitiveSerializer {

        @Override
        public String getName() {
            return "com/alesharik/webserver/api/serial/IntPrimitiveSerializer";
        }

        @Override
        public byte[] serializeDefault(Object o) {
            int val = (Integer) o;
            return new byte[]{
                    (byte) (val >>> 24),
                    (byte) (val >>> 16),
                    (byte) (val >>> 8),
                    (byte) val
            };
        }

        @Override
        public Object deserializeDefault(byte[] data) {
            if(data.length != 4)
                throw new IllegalArgumentException("Expected 4 bytes, got " + data.length + "!");
            return ((data[3] & 0xFF)) +
                    ((data[2] & 0xFF) << 8) +
                    ((data[1] & 0xFF) << 16) +
                    ((data[0]) << 24);
        }
    }

    static final class BooleanSerializer extends PrimitiveSerializer {

        @Override
        public String getName() {
            return "com/alesharik/webserver/api/serial/BooleanPrimitiveSerializer";
        }

        @Override
        public byte[] serializeDefault(Object o) {
            boolean val = (Boolean) o;
            return new byte[]{
                    (byte) (val ? 1 : 0)
            };
        }

        @Override
        public Object deserializeDefault(byte[] data) {
            if(data.length != 1)
                throw new IllegalArgumentException("Expected 1 byte, got " + data.length + "!");
            return data[0] != 0;
        }
    }

    static final class CharSerializer extends PrimitiveSerializer {

        @Override
        public String getName() {
            return "com/alesharik/webserver/api/serial/CharPrimitiveSerializer";
        }

        @Override
        public byte[] serializeDefault(Object o) {
            int val = (Character) o;
            return new byte[]{
                    (byte) (val >>> 8),
                    (byte) val
            };
        }

        @Override
        public Object deserializeDefault(byte[] data) {
            if(data.length != 2)
                throw new IllegalArgumentException("Expected 2 bytes, got " + data.length + "!");
            return (char) ((data[1] & 0xFF) +
                    (data[0] << 8));
        }
    }

    static final class ShortSerializer extends PrimitiveSerializer {

        @Override
        public String getName() {
            return "com/alesharik/webserver/api/serial/ShortPrimitiveSerializer";
        }

        @Override
        public byte[] serializeDefault(Object o) {
            int val = (Short) o;
            return new byte[]{
                    (byte) (val >>> 8),
                    (byte) val
            };
        }

        @Override
        public Object deserializeDefault(byte[] data) {
            if(data.length != 2)
                throw new IllegalArgumentException("Expected 2 bytes, got " + data.length + "!");
            //noinspection UnnecessaryBoxing
            return Short.valueOf((short) ((data[1] & 0xFF) + (data[0] << 8)));
        }
    }

    static final class FloatSerializer extends PrimitiveSerializer {

        @Override
        public String getName() {
            return "com/alesharik/webserver/api/serial/FloatPrimitiveSerializer";
        }

        @Override
        public byte[] serializeDefault(Object o) {
            int val = Float.floatToIntBits((Float) o);
            return new byte[]{
                    (byte) (val >>> 24),
                    (byte) (val >>> 16),
                    (byte) (val >>> 8),
                    (byte) val
            };
        }

        @Override
        public Object deserializeDefault(byte[] data) {
            if(data.length != 4)
                throw new IllegalArgumentException("Expected 4 bytes, got " + data.length + "!");
            int i = ((data[3] & 0xFF)) +
                    ((data[2] & 0xFF) << 8) +
                    ((data[1] & 0xFF) << 16) +
                    ((data[0]) << 24);
            return Float.intBitsToFloat(i);
        }
    }

    static final class LongSerializer extends PrimitiveSerializer {

        @Override
        public String getName() {
            return "com/alesharik/webserver/api/serial/LongPrimitiveSerializer";
        }

        @Override
        public byte[] serializeDefault(Object o) {
            long val = (Long) o;
            return new byte[]{
                    (byte) (val >>> 56),
                    (byte) (val >>> 48),
                    (byte) (val >>> 40),
                    (byte) (val >>> 32),
                    (byte) (val >>> 24),
                    (byte) (val >>> 16),
                    (byte) (val >>> 8),
                    (byte) val
            };
        }

        @Override
        public Object deserializeDefault(byte[] data) {
            if(data.length != 8)
                throw new IllegalArgumentException("Expected 8 bytes, got " + data.length + "!");
            return ((data[7] & 0xFFL)) +
                    ((data[6] & 0xFFL) << 8) +
                    ((data[5] & 0xFFL) << 16) +
                    ((data[4] & 0xFFL) << 24) +
                    ((data[3] & 0xFFL) << 32) +
                    ((data[2] & 0xFFL) << 40) +
                    ((data[1] & 0xFFL) << 48) +
                    (((long) data[0]) << 56);
        }
    }

    static final class DoubleSerializer extends PrimitiveSerializer {

        @Override
        public String getName() {
            return "com/alesharik/webserver/api/serial/DoublePrimitiveSerializer";
        }

        @Override
        public byte[] serializeDefault(Object o) {
            long val = Double.doubleToLongBits((Double) o);
            return new byte[]{
                    (byte) (val >>> 56),
                    (byte) (val >>> 48),
                    (byte) (val >>> 40),
                    (byte) (val >>> 32),
                    (byte) (val >>> 24),
                    (byte) (val >>> 16),
                    (byte) (val >>> 8),
                    (byte) val
            };
        }

        @Override
        public Object deserializeDefault(byte[] data) {
            if(data.length != 8)
                throw new IllegalArgumentException("Expected 8 bytes, got " + data.length + "!");
            return Double.longBitsToDouble(((data[7] & 0xFFL)) +
                    ((data[6] & 0xFFL) << 8) +
                    ((data[5] & 0xFFL) << 16) +
                    ((data[4] & 0xFFL) << 24) +
                    ((data[3] & 0xFFL) << 32) +
                    ((data[2] & 0xFFL) << 40) +
                    ((data[1] & 0xFFL) << 48) +
                    (((long) data[0]) << 56));
        }
    }
}
