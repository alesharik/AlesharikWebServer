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

package com.alesharik.webserver.api.utils.crypto.crc;

import com.alesharik.webserver.api.ByteOrderUtils;

import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.zip.CRC32;

public final class CRC32MessageDigest extends MessageDigest {
    static {
        new CRC32Provider();//Ensure Provider is registered
    }

    private final CRC32 crc32;

    /**
     * Creates a message digest with the specified algorithm name.
     */
    public CRC32MessageDigest() {
        super("CRC32");
        crc32 = new CRC32();
    }

    @Override
    protected void engineUpdate(byte input) {
        crc32.update(input);
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
        crc32.update(input, offset, len);
    }

    @Override
    protected byte[] engineDigest() {
        long l = ByteOrderUtils.format(crc32.getValue(), ByteOrder.BIG_ENDIAN);
        byte[] bytes = new byte[8];
        bytes[7] = (byte) ((l & 0xFF00000000000000L) >> 56);
        bytes[6] = (byte) ((l & 0x00FF000000000000L) >> 48);
        bytes[5] = (byte) ((l & 0x0000FF0000000000L) >> 40);
        bytes[4] = (byte) ((l & 0x000000FF00000000L) >> 32);
        bytes[3] = (byte) ((l & 0x00000000FF000000L) >> 24);
        bytes[2] = (byte) ((l & 0x0000000000FF0000L) >> 16);
        bytes[1] = (byte) ((l & 0x000000000000FF00L) >> 8);
        bytes[0] = (byte) ((l & 0x00000000000000FFL));
        return bytes;
    }

    /**
     * Return CRC32 digest
     */
    public long getDigest() {
        return crc32.getValue();
    }

    @Override
    protected void engineReset() {
        crc32.reset();
    }
}
