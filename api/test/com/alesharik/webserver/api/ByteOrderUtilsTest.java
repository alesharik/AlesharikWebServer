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

package com.alesharik.webserver.api;

import org.junit.Test;

import java.nio.ByteOrder;

import static org.junit.Assert.assertEquals;

public class ByteOrderUtilsTest {
    @Test
    public void formatShortBigEndian() throws Exception {
        short s = 0x1234;
        short out = ByteOrderUtils.format(s, ByteOrder.BIG_ENDIAN);
        assertEquals((byte) out, 0x12);
    }

    @Test
    public void formatShortLittleEndian() throws Exception {
        short s = 0x1234;
        short out = ByteOrderUtils.format(s, ByteOrder.LITTLE_ENDIAN);
        assertEquals((byte) out, 0x34);
    }

    @Test
    public void formatIntBigEndian() throws Exception {
        int i = 0x12345678;
        int out = ByteOrderUtils.format(i, ByteOrder.BIG_ENDIAN);
        assertEquals((byte) out, 0x12);
    }

    @Test
    public void formatIntLittleEndian() throws Exception {
        int i = 0x12345678;
        int out = ByteOrderUtils.format(i, ByteOrder.LITTLE_ENDIAN);
        assertEquals((byte) out, 0x78);
    }

    @Test
    public void formatLongBigEndian() throws Exception {
        long l = 0x1234567812345678L;
        long out = ByteOrderUtils.format(l, ByteOrder.BIG_ENDIAN);
        assertEquals((byte) out, 0x12);
    }

    @Test
    public void formatLongLittleEndian() throws Exception {
        long l = 0x1234567812345678L;
        long out = ByteOrderUtils.format(l, ByteOrder.LITTLE_ENDIAN);
        assertEquals((byte) out, 0x78);
    }

    @Test
    public void formatCharBigEndian() throws Exception {
        char c = 0x1234;
        char out = ByteOrderUtils.format(c, ByteOrder.BIG_ENDIAN);
        assertEquals((byte) out, 0x12);
    }

    @Test
    public void formatCharLittleEndian() throws Exception {
        char c = 0x1234;
        char out = ByteOrderUtils.format(c, ByteOrder.LITTLE_ENDIAN);
        assertEquals((byte) out, 0x34);
    }

    @Test
    public void formatFloatBigEndian() throws Exception {
        float f = 0x12345678;
        float out = ByteOrderUtils.format(f, ByteOrder.BIG_ENDIAN);
        assertEquals((byte) Float.floatToIntBits(out), 0x4D);
    }

    @Test
    public void formatFloatLittleEndian() throws Exception {
        float f = 0x12345678;
        float out = ByteOrderUtils.format(f, ByteOrder.LITTLE_ENDIAN);
        assertEquals((byte) Float.floatToIntBits(out), -0x4C);
    }

    @Test
    public void formatDoubleBigEndian() throws Exception {
        double d = 131176846517314111236247812874623512342112352352353251.34125123232423423423532141234123123523412D;
        double out = ByteOrderUtils.format(d, ByteOrder.BIG_ENDIAN);
        assertEquals((byte) Double.doubleToLongBits(out), 0x4A);
    }

    @Test
    public void formatDoubleLittleEndian() throws Exception {
        double d = 131176846517314111236247812874623512342112352352353251.34125123232423423423532141234123123523412D;
        double out = ByteOrderUtils.format(d, ByteOrder.LITTLE_ENDIAN);
        assertEquals((byte) Double.doubleToLongBits(out), 0x68);
    }
}