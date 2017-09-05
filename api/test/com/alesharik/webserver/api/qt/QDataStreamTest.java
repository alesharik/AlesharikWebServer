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
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.nio.ByteOrder;
import java.util.Random;

import static org.junit.Assert.*;

@RunWith(Theories.class)
public class QDataStreamTest {
    @DataPoints
    public static ByteOrder theories[] = new ByteOrder[]{ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN};

    private QDataStream stream;

    @Before
    public void setUp() throws Exception {
        stream = new QDataStream();
    }

    @After
    public void tearDown() throws Exception {
        stream.close();
    }

    @Theory
    public void testByte(ByteOrder byteOrder) throws Exception {
        stream.setOrder(byteOrder);
        stream.writeByte(123);
        stream.writeByte(0x1);
        assertEquals(123, stream.readByte());
        assertEquals(0x1, stream.readByte());
    }

    @Theory
    public void testUnsignedByte(ByteOrder byteOrder) throws Exception {
        stream.setOrder(byteOrder);
        stream.writeUnsignedByte((short) (Byte.MAX_VALUE + 1));
        assertEquals((short) Byte.MAX_VALUE + 1, stream.readUnsignedByte());
    }

    @Theory
    public void testBoolean(ByteOrder byteOrder) throws Exception {
        stream.setOrder(byteOrder);
        stream.writeBoolean(true);
        stream.writeBoolean(false);
        assertTrue(stream.readBoolean());
        assertFalse(stream.readBoolean());
    }

    @Theory
    public void testShort(ByteOrder byteOrder) throws Exception {
        stream.setOrder(byteOrder);
        short s = 200;
        stream.writeShort(s);
        stream.writeShort(Short.MAX_VALUE);
        assertEquals(s, stream.readShort());
        assertEquals(Short.MAX_VALUE, stream.readShort());
    }

    @Theory
    public void testUnsignedShort(ByteOrder byteOrder) throws Exception {
        stream.setOrder(byteOrder);
        stream.writeUnsignedShort(Short.MAX_VALUE + 1);
        assertEquals(Short.MAX_VALUE + 1, stream.readUnsignedShort());
    }

    @Theory
    public void testChar(ByteOrder byteOrder) throws Exception {
        stream.setOrder(byteOrder);
        stream.writeChar(123);
        stream.writeChar('c');
        assertEquals(123, stream.readChar());
        assertEquals('c', stream.readChar());
    }

    @Theory
    public void testInt(ByteOrder byteOrder) throws Exception {
        stream.setOrder(byteOrder);
        stream.writeInt(123456);
        assertEquals(123456, stream.readInt());
    }

    @Theory
    public void testUnsignedInt(ByteOrder byteOrder) throws Exception {
        stream.setOrder(byteOrder);
        stream.writeUnsignedInt(Integer.MAX_VALUE + 1L);
        assertEquals(Integer.MAX_VALUE + 1L, stream.readUnsignedInt());
    }

    @Theory
    public void testLong(ByteOrder byteOrder) throws Exception {
        stream.setOrder(byteOrder);
        stream.writeLong(1234567890123412412L);
        assertEquals(1234567890123412412L, stream.readLong());
    }

    @Theory
    public void testUnsignedLong(ByteOrder byteOrder) throws Exception {
        stream.setOrder(byteOrder);
        stream.writeLong(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, stream.readLong());
    }

    @Theory
    public void testFloat(ByteOrder byteOrder) throws Exception {
        stream.setOrder(byteOrder);
        stream.writeFloat(1.0F);
        assertEquals(1.0F, stream.readFloat(), 0.000001);
    }

    @Theory
    public void testDouble(ByteOrder byteOrder) throws Exception {
        stream.setOrder(byteOrder);
        stream.writeDouble(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, stream.readDouble(), 0.000000000000000000000000000000000000000000000000001);
    }

    @Theory
    public void writeBytesTest(ByteOrder byteOrder) throws Exception {
        stream.setOrder(byteOrder);
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

    @Theory
    public void testWriteBytes(ByteOrder byteOrder) throws Exception {
        stream.setOrder(byteOrder);
        String test = "asdfqwer";
        stream.writeBytes(test);
        for(char c : test.toCharArray()) {
            assertEquals(c, stream.readByte());
        }
    }

    @Theory
    public void testWriteChars(ByteOrder byteOrder) throws Exception {
        stream.setOrder(byteOrder);
        String test = "qwertyuiop";
        int length = test.getBytes("UTF-16LE").length;
        stream.writeChars(test);

        byte[] data = new byte[length];
        stream.readFully(data);
        String ret = new String(data, "UTF-16LE");
        assertEquals(test, ret);
    }

    @Theory
    public void testReadUTF(ByteOrder byteOrder) throws Exception {
        stream.setOrder(byteOrder);
        String test = "aasdsdfsasadfasfgdfkjsldjlfdlksdlkfl;sdkjglfsdklkgdfljgk;dakughdfkslasfl;sd;kgjsfmg";
        stream.writeUTF(test);
        assertEquals(test, stream.readUTF());
    }

    @Theory
    public void testReadALotOfUTF(ByteOrder byteOrder) throws Exception {
        stream.setOrder(byteOrder);
        String test = "aasdsdfsasadfasfgdfkjsldjlfdlksdlkfl;sdkjglfsdklkgdfljgk;dakughdfkslasfl;sd;kgjsfmg";
        String text1 = "sdfdsfadfsdsfdfs";
        stream.writeUTF(test);
        stream.writeUTF(text1);
        assertEquals(test, stream.readUTF());
        assertEquals(text1, stream.readUTF());
    }

    @Theory
    public void testSkip(ByteOrder byteOrder) throws Exception {
        stream.setOrder(byteOrder);
        byte[] data = new byte[]{1, 2, 3};
        stream.write(data);
        stream.skip(1);
        stream.skipBytes(1);
        assertEquals(data[2], stream.readByte());
    }

    @Theory
    public void testBigArray(ByteOrder byteOrder) throws Exception {
        stream.setOrder(byteOrder);
        byte[] arr = new byte[4096 * 4096];
        new Random().nextBytes(arr);
        stream.write(arr);

        for(byte b : arr) {
            assertEquals(b, stream.readByte());
        }
    }

    @Theory
    public void test102UInt(ByteOrder order) {
        stream.setOrder(order);
        stream.writeUnsignedInt(102);
        String s = "{\"id\":\"d871dbd1-ebf2-422b-be4b-1f4cf4d1d8c5\",\"name\":\"test\",\"login\":\"test\",\"connected\":true,\"ip\":\"127.0.0.1\",\"controlPort\":4000,\"data\":{}}";
        stream.writeUTF(s);

        assertEquals(102, stream.readUnsignedInt());
        assertEquals(s, stream.readUTF());
    }
}