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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class QDataStreamTest {
    private QDataStream stream;

    @Before
    public void setUp() throws Exception {
        stream = new QDataStream();
    }

    @After
    public void tearDown() throws Exception {
        stream.close();
    }

    @Test
    public void testByte() throws Exception {
        stream.writeByte(123);
        stream.writeByte(0x1);
        assertEquals(123, stream.readByte());
        assertEquals(0x1, stream.readByte());
    }

    @Test
    public void testUnsignedByte() throws Exception {
        stream.writeUnsignedByte((short) (Byte.MAX_VALUE + 1));
        assertEquals((short) Byte.MAX_VALUE + 1, stream.readUnsignedByte());
    }

    @Test
    public void testBoolean() throws Exception {
        stream.writeBoolean(true);
        stream.writeBoolean(false);
        assertTrue(stream.readBoolean());
        assertFalse(stream.readBoolean());
    }

    @Test
    public void testShort() throws Exception {
        short s = 200;
        stream.writeShort(s);
        stream.writeShort(Short.MAX_VALUE);
        assertEquals(s, stream.readShort());
        assertEquals(Short.MAX_VALUE, stream.readShort());
    }

    @Test
    public void testUnsignedShort() throws Exception {
        stream.writeUnsignedShort(Short.MAX_VALUE + 1);
        assertEquals(Short.MAX_VALUE + 1, stream.readUnsignedShort());
    }

    @Test
    public void testChar() throws Exception {
        stream.writeChar(123);
        stream.writeChar('c');
        assertEquals(123, stream.readChar());
        assertEquals('c', stream.readChar());
    }

    @Test
    public void testInt() throws Exception {
        stream.writeInt(123456);
        assertEquals(123456, stream.readInt());
    }

    @Test
    public void testUnsignedInt() throws Exception {
        stream.writeUnsignedInt(Integer.MAX_VALUE + 1L);
        assertEquals(Integer.MAX_VALUE + 1L, stream.readUnsignedInt());
    }

    @Test
    public void testLong() throws Exception {
        stream.writeLong(1234567890123412412L);
        assertEquals(1234567890123412412L, stream.readLong());
    }

    @Test
    public void testUnsignedLong() throws Exception {
        stream.writeLong(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, stream.readLong());
    }

    @Test
    public void testFloat() throws Exception {
        stream.writeFloat(1.0F);
        assertEquals(1.0F, stream.readFloat(), 0.000001);
    }

    @Test
    public void testDouble() throws Exception {
        stream.writeDouble(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, stream.readDouble(), 0.000000000000000000000000000000000000000000000000001);
    }

    @Test
    public void writeBytesTest() throws Exception {
        byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        byte[] data2 = new byte[]{30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42};
        byte[] data3 = new byte[]{99, 100, 101};
        byte first = 20;
        int second = 21;
        stream.write(first);
        stream.write(second);
        stream.write(data);
        stream.write(data2, 1, 2);
        stream.write(data3);

        assertEquals(first, stream.readByte());
        assertEquals(second, stream.readByte());

        byte[] readData1 = new byte[data.length];
        stream.readFully(readData1);
        for(int i = 0; i < data.length; i++) {
            assertEquals(data[i], readData1[i]);
        }

        byte[] readData2 = new byte[data2.length];
        readData2[0] = 100;
        stream.readFully(readData2, 1, 2);
        assertEquals(100, readData2[0]);
        for(int i = 1; i < 3; i++) {
            assertEquals(data2[i], readData2[i]);
        }

        byte[] readData3 = new byte[128];
        stream.read(readData3, 0, data3.length);
        for(int i = 0; i < data3.length; i++) {
            assertEquals(data3[i], readData3[i]);
        }
    }

    @Test
    public void testWriteBytes() throws Exception {
        String test = "asdfqwer";
        stream.writeBytes(test);
        for(char c : test.toCharArray()) {
            assertEquals(c, stream.readByte());
        }
    }

    @Test
    public void testWriteChars() throws Exception {
        String test = "qwertyuiop";
        int length = test.getBytes("UTF-16LE").length;
        stream.writeChars(test);

        byte[] data = new byte[length];
        stream.readFully(data);
        String ret = new String(data, "UTF-16LE");
        assertEquals(test, ret);
    }

    @Test
    public void testReadUTF() throws Exception {
        String test = "aasdsdfsasadfasfgdfkjsldjlfdlksdlkfl;sdkjglfsdklkgdfljgk;dakughdfkslasfl;sd;kgjsfmg";
        stream.writeUTF(test);
        assertEquals(test, stream.readUTF());
    }

    @Test
    public void testSkip() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        stream.write(data);
        stream.skip(1);
        stream.skipBytes(1);
        assertEquals(data[2], stream.readByte());
    }

    @Test
    public void testBigArray() throws Exception {
        byte[] arr = new byte[4096 * 4096];
        new Random().nextBytes(arr);
        stream.write(arr);

        for(byte b : arr) {
            assertEquals(b, stream.readByte());
        }
    }
}