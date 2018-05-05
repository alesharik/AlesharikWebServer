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

package com.alesharik.webserver.configuration.config.lang.parser.elements;

import com.alesharik.webserver.configuration.config.lang.element.ConfigurationPrimitive;
import org.junit.Test;

import static org.junit.Assert.*;

public class PrimitiveImplTest {
    @Test
    public void wrapString() {
        ConfigurationPrimitive wrap = PrimitiveImpl.wrap("test", "asd");
        ConfigurationPrimitive.String string = ((ConfigurationPrimitive.String) wrap);
        assertEquals("test", string.getName());
        assertEquals("asd", string.value());
    }

    @Test
    public void wrapChar() {
        ConfigurationPrimitive wrap = PrimitiveImpl.wrap("test", 'c');
        ConfigurationPrimitive.Char c = (ConfigurationPrimitive.Char) wrap;
        assertEquals("test", c.getName());
        assertEquals('c', c.value());
    }

    @Test
    public void parseInt() {
        ConfigurationPrimitive.Int i = (ConfigurationPrimitive.Int) PrimitiveImpl.parseNotString("test", "111111");
        assertEquals(111111, i.value());
//        ConfigurationPrimitive.Int i1 = (ConfigurationPrimitive.Int) PrimitiveImpl.parseNotString("test", "0b11");
//        assertEquals(0b11, i1.value()); it doesn't support by java
        ConfigurationPrimitive.Int i2 = (ConfigurationPrimitive.Int) PrimitiveImpl.parseNotString("test", "0x2F");
        assertEquals(0x2F, i2.value());
    }

    @Test
    public void parseIllegalIntNegativeOverflow() {
        assertNull(PrimitiveImpl.parseNotString("name", Long.toString(Integer.MIN_VALUE - 1L)));
    }

    @Test
    public void parseIllegalIntPositiveOverflow() {
        assertNull(PrimitiveImpl.parseNotString("name", Long.toString(Integer.MAX_VALUE + 1L)));
    }

    @Test
    public void parseBoolean() {
        ConfigurationPrimitive.Boolean tr = (ConfigurationPrimitive.Boolean) PrimitiveImpl.parseNotString("test", "true");
        assertTrue(tr.value());
        ConfigurationPrimitive.Boolean fls = (ConfigurationPrimitive.Boolean) PrimitiveImpl.parseNotString("test", "false");
        assertFalse(fls.value());
    }

    @Test
    public void parseByte() {
        ConfigurationPrimitive.Byte b1 = (ConfigurationPrimitive.Byte) PrimitiveImpl.parseNotString("test", "1B");
        assertEquals(1, b1.value());
        ConfigurationPrimitive.Byte b2 = (ConfigurationPrimitive.Byte) PrimitiveImpl.parseNotString("test", "0xFB");
        assertEquals(0xF, b2.value());
//        ConfigurationPrimitive.Byte b3 = (ConfigurationPrimitive.Byte) PrimitiveImpl.parseNotString("trst", "0b11B");
//        assertEquals(0b11, b3.value()); it doesn't support by java
    }

    @Test
    public void parseByteNegativeOverflow() {
        assertNull(PrimitiveImpl.parseNotString("t", Integer.toString(Byte.MAX_VALUE + 1) + "B"));
    }

    @Test
    public void parseBytePositiveOverflow() {
        assertNull(PrimitiveImpl.parseNotString("t", Integer.toString(Byte.MIN_VALUE - 1) + "B"));
    }

    @Test
    public void parseShort() {
        ConfigurationPrimitive.Short s1 = (ConfigurationPrimitive.Short) PrimitiveImpl.parseNotString("t", "2550S");
        assertEquals(2550, s1.value());
        ConfigurationPrimitive.Short s2 = (ConfigurationPrimitive.Short) PrimitiveImpl.parseNotString("t", "0xFFFS");
        assertEquals(0xFFF, s2.value());
//        ConfigurationPrimitive.Short s3 = (ConfigurationPrimitive.Short) PrimitiveImpl.parseNotString("t", "0b111111S");
//        assertEquals(0b111111, s3.value()); it doesn't support by java
    }

    @Test
    public void parseShortNegativeOverflow() {
        assertNull(PrimitiveImpl.parseNotString("t", Integer.toString(Short.MIN_VALUE - 1) + "S"));
    }

    @Test
    public void parseShortPositiveOverflow() {
        assertNull(PrimitiveImpl.parseNotString("t", Integer.toString(Short.MAX_VALUE + 1) + "S"));
    }

    @Test
    public void parseLong() {
        ConfigurationPrimitive.Long l1 = (ConfigurationPrimitive.Long) PrimitiveImpl.parseNotString("t", "3000000000L");
        assertEquals(3000000000L, l1.value());
        ConfigurationPrimitive.Long l2 = (ConfigurationPrimitive.Long) PrimitiveImpl.parseNotString("t", "0xCAFEBABEL");
        assertEquals(0xCAFEBABEL, l2.value());
//        ConfigurationPrimitive.Long l3 = (ConfigurationPrimitive.Long) PrimitiveImpl.parseNotString("t", "0b1111111111L");
//        assertEquals(0b1111111111L, l3.value()); it doesn't support by java
    }

    @Test
    public void parseLongPositiveOverflow() {
        assertNull(PrimitiveImpl.parseNotString("t", Long.toString(Long.MAX_VALUE) + "1L"));
    }

    @Test
    public void parseLongNegativeOverflow() {
        assertNull(PrimitiveImpl.parseNotString("t", Long.toString(Long.MIN_VALUE) + "1L"));
    }

    @Test
    public void parseFloat() {
        ConfigurationPrimitive.Float f = (ConfigurationPrimitive.Float) PrimitiveImpl.parseNotString("t", "1.0");
        assertEquals(1.0, f.value(), 0.1);
    }

    @Test
    public void parseDouble() {
        ConfigurationPrimitive.Double d = (ConfigurationPrimitive.Double) PrimitiveImpl.parseNotString("t", "0.12121212121212121D");
        assertEquals(0.12121212121212121, d.value(), 0.000000000000000001);
    }
}