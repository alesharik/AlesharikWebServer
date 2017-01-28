package com.alesharik.webserver.api.memory;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class StructTest {
    private static Struct simpleStruct;

    private long simpleStructAddr;

    @BeforeClass
    public static void setUp() throws Exception {
        simpleStruct = new Struct.Builder()
                .addShort("s")
                .addInteger("i")
                .addLong("l")
                .addChar("c")
                .addBoolean("z")
                .addByte("b")
                .addDouble("d")
                .addFloat("f")
                .build();
    }

    @Before
    public void setup() {
        simpleStructAddr = simpleStruct.allocate();
    }

    @After
    public void tearDown() throws Exception {
        simpleStruct.free(simpleStructAddr);
    }

    @Test
    public void allocateAndFree() throws Exception {
        long addr = simpleStruct.allocate();
        assertTrue(addr != 0);
        simpleStruct.free(addr);
    }

    @Test
    public void getSize() throws Exception {
        assertTrue(simpleStruct.getSize() == 33);
    }

    @Test
    public void setAndGetLong() throws Exception {
        simpleStruct.setLong(simpleStructAddr, "l", 1001L);
        assertTrue(Long.compare(simpleStruct.getLong(simpleStructAddr, "l"), 1001L) == 0);
    }

    @Test
    public void setAndGetBoolean() throws Exception {
        simpleStruct.setBoolean(simpleStructAddr, "z", true);
        assertTrue(simpleStruct.getBoolean(simpleStructAddr, "z"));
    }

    @Test
    public void setAndGetByte() throws Exception {
        simpleStruct.setByte(simpleStructAddr, "b", (byte) 0x11);
        assertTrue(simpleStruct.getByte(simpleStructAddr, "b") == ((byte) 0x11));
    }

    @Test
    public void setAndGetShort() throws Exception {
        simpleStruct.setShort(simpleStructAddr, "s", (short) 12);
        assertTrue(simpleStruct.getShort(simpleStructAddr, "s") == ((short) 12));
    }

    @Test
    public void setAndGetChar() throws Exception {
        simpleStruct.setChar(simpleStructAddr, "c", 'd');
        assertTrue(simpleStruct.getChar(simpleStructAddr, "c") == 'd');
    }

    @Test
    public void setAndGetDouble() throws Exception {
        simpleStruct.setDouble(simpleStructAddr, "d", 1234213.12432142134123D);
        assertTrue(Double.compare(simpleStruct.getDouble(simpleStructAddr, "d"), 1234213.12432142134123D) == 0);
    }

    @Test
    public void setAndGetFloat() throws Exception {
        simpleStruct.setFloat(simpleStructAddr, "f", 123.324F);
        assertTrue(Float.compare(simpleStruct.getFloat(simpleStructAddr, "f"), 123.324F) == 0);
    }


    @Test
    public void setStruct() throws Exception {

    }

    @Test
    public void getStructAddress() throws Exception {

    }

    @Test
    public void setArray() throws Exception {

    }

    @Test
    public void getArrayAddress() throws Exception {

    }

    @Test
    public void setPointer() throws Exception {

    }

    @Test
    public void getPointer() throws Exception {

    }

}