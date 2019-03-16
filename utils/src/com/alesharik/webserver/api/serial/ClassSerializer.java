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

final class ClassSerializer implements Serializer {
    @Override
    public String getName() {
        return "com/alesharik/webserver/api/serial/ClassSerializer";
    }

    @Override
    public double getVersion() {
        return -1;
    }

    @Override
    public byte[] serialize(Object o) {
        return serializeDefault(o);
    }

    @Override
    public byte[] serializeDefault(Object o) {
        long id = Serial.getConversionMap().getOrCreateConversionFor((Class<?>) o);
        return new byte[]{
                (byte) (id >>> 56),
                (byte) (id >>> 48),
                (byte) (id >>> 40),
                (byte) (id >>> 32),
                (byte) (id >>> 24),
                (byte) (id >>> 16),
                (byte) (id >>> 8),
                (byte) id
        };
    }

    @Override
    public Object deserialize(byte[] data) {
        return deserializeDefault(data);
    }

    @Override
    public Object deserializeDefault(byte[] data) {
        if(data.length != 8)
            throw new DataOverflowException("Expected 8 bytes, got " + data.length + "!");
        long id = ((data[7] & 0xFFL)) +
                ((data[6] & 0xFFL) << 8) +
                ((data[5] & 0xFFL) << 16) +
                ((data[4] & 0xFFL) << 24) +
                ((data[3] & 0xFFL) << 32) +
                ((data[2] & 0xFFL) << 40) +
                ((data[1] & 0xFFL) << 48) +
                (((long) data[0]) << 56);
        Class<?> conversion = Serial.getConversionMap().resolveConversion(id);
        if(conversion == null)
            throw new SerializationMappingNotFoundException(id);
        return conversion;

    }

    @Override
    public void deserializeDefaultIntoObject(IOStream data, Object o) {
        throw new UnsupportedOperationException("Cannot interact with Class object!");
    }
}
