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

import java.nio.charset.StandardCharsets;

final class StringSerializer implements Serializer {

    @Override
    public String getName() {
        return "com/alesharik/webserver/api/serial/StringSerializer";
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
        return ((String) o).getBytes(StandardCharsets.UTF_16LE);
    }

    @Override
    public Object deserialize(byte[] data) {
        return deserializeDefault(data);
    }

    @Override
    public Object deserializeDefault(byte[] data) {
        return new String(data, StandardCharsets.UTF_16LE);
    }

    @Override
    public void deserializeDefaultIntoObject(IOStream data, Object o) {
        throw new UnsupportedOperationException("Cannot write into string!");
    }
}
