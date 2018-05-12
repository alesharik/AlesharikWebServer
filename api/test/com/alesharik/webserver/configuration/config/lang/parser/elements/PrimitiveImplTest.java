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
    public void parseFloatWithComma() {
        ConfigurationPrimitive.Float f = (ConfigurationPrimitive.Float) PrimitiveImpl.parseNotString("t", "1,0");
        assertEquals(1.0, f.value(), 0.1);
    }

    @Test
    public void parseDouble() {
        ConfigurationPrimitive.Double d = (ConfigurationPrimitive.Double) PrimitiveImpl.parseNotString("t", "0.12121212121212121D");
        assertEquals(0.12121212121212121, d.value(), 0.000000000000000001);
    }

    @Test
    public void equality() {
        assertEquals(PrimitiveImpl.parseNotString("a", "true"), PrimitiveImpl.parseNotString("a", "true"));
        assertNotEquals(PrimitiveImpl.parseNotString("a", "true"), PrimitiveImpl.parseNotString("a", "false"));

        assertEquals(PrimitiveImpl.parseNotString("a", "111"), PrimitiveImpl.parseNotString("a", "111"));
        assertNotEquals(PrimitiveImpl.parseNotString("a", "111"), PrimitiveImpl.parseNotString("a", "1111"));

        assertEquals(PrimitiveImpl.parseNotString("a", "1B"), PrimitiveImpl.parseNotString("a", "0x01B"));
        assertNotEquals(PrimitiveImpl.parseNotString("a", "1B"), PrimitiveImpl.parseNotString("a", "0x11B"));

        assertEquals(PrimitiveImpl.parseNotString("a", "12S"), PrimitiveImpl.parseNotString("a", "12S"));
        assertNotEquals(PrimitiveImpl.parseNotString("a", "12S"), PrimitiveImpl.parseNotString("a", "13S"));

        assertEquals(PrimitiveImpl.parseNotString("a", "1221212121L"), PrimitiveImpl.parseNotString("a", "1221212121L"));
        assertNotEquals(PrimitiveImpl.parseNotString("a", "12L"), PrimitiveImpl.parseNotString("a", "122L"));

        assertEquals(PrimitiveImpl.parseNotString("a", "1.0"), PrimitiveImpl.parseNotString("a", "1,0"));
        assertNotEquals(PrimitiveImpl.parseNotString("a", "11.0"), PrimitiveImpl.parseNotString("a", "1,0"));

        assertEquals(PrimitiveImpl.parseNotString("a", "12.2D"), PrimitiveImpl.parseNotString("a", "12.2D"));
        assertNotEquals(PrimitiveImpl.parseNotString("a", "12.22D"), PrimitiveImpl.parseNotString("a", "12.2D"));

        assertNotEquals(PrimitiveImpl.parseNotString("a", "true"), PrimitiveImpl.parseNotString("a", "1111"));

        assertNotEquals(PrimitiveImpl.parseNotString("a", "true"), PrimitiveImpl.parseNotString("b", "true"));

        assertEquals(PrimitiveImpl.wrap("a", 'c'), PrimitiveImpl.wrap("a", 'c'));
        assertNotEquals(PrimitiveImpl.wrap("a", 'c'), PrimitiveImpl.wrap("a", 'b'));

        assertEquals(PrimitiveImpl.wrap("a", "aa"), PrimitiveImpl.wrap("a", "aa"));
        assertNotEquals(PrimitiveImpl.wrap("a", "cc"), PrimitiveImpl.wrap("a", "b"));


        assertEquals(PrimitiveImpl.parseNotString("a", "true").hashCode(), PrimitiveImpl.parseNotString("a", "true").hashCode());
        assertNotEquals(PrimitiveImpl.parseNotString("a", "true").hashCode(), PrimitiveImpl.parseNotString("a", "false").hashCode());

        assertEquals(PrimitiveImpl.parseNotString("a", "111").hashCode(), PrimitiveImpl.parseNotString("a", "111").hashCode());
        assertNotEquals(PrimitiveImpl.parseNotString("a", "111").hashCode(), PrimitiveImpl.parseNotString("a", "1111").hashCode());

        assertEquals(PrimitiveImpl.parseNotString("a", "1B").hashCode(), PrimitiveImpl.parseNotString("a", "0x01B").hashCode());
        assertNotEquals(PrimitiveImpl.parseNotString("a", "1B").hashCode(), PrimitiveImpl.parseNotString("a", "0x11B").hashCode());

        assertEquals(PrimitiveImpl.parseNotString("a", "12S").hashCode(), PrimitiveImpl.parseNotString("a", "12S").hashCode());
        assertNotEquals(PrimitiveImpl.parseNotString("a", "12S").hashCode(), PrimitiveImpl.parseNotString("a", "13S").hashCode());

        assertEquals(PrimitiveImpl.parseNotString("a", "1221212121L").hashCode(), PrimitiveImpl.parseNotString("a", "1221212121L").hashCode());
        assertNotEquals(PrimitiveImpl.parseNotString("a", "12L").hashCode(), PrimitiveImpl.parseNotString("a", "122L").hashCode());

        assertEquals(PrimitiveImpl.parseNotString("a", "1.0").hashCode(), PrimitiveImpl.parseNotString("a", "1,0").hashCode());
        assertNotEquals(PrimitiveImpl.parseNotString("a", "11.0").hashCode(), PrimitiveImpl.parseNotString("a", "1,0").hashCode());

        assertEquals(PrimitiveImpl.parseNotString("a", "12.2D").hashCode(), PrimitiveImpl.parseNotString("a", "12.2D").hashCode());
        assertNotEquals(PrimitiveImpl.parseNotString("a", "12.22D").hashCode(), PrimitiveImpl.parseNotString("a", "12.2D").hashCode());

        assertNotEquals(PrimitiveImpl.parseNotString("a", "true").hashCode(), PrimitiveImpl.parseNotString("a", "1111").hashCode());

        assertNotEquals(PrimitiveImpl.parseNotString("a", "true").hashCode(), PrimitiveImpl.parseNotString("b", "true").hashCode());

        assertEquals(PrimitiveImpl.wrap("a", 'c').hashCode(), PrimitiveImpl.wrap("a", 'c').hashCode());
        assertNotEquals(PrimitiveImpl.wrap("a", 'c').hashCode(), PrimitiveImpl.wrap("a", 'b').hashCode());

        assertEquals(PrimitiveImpl.wrap("a", "aa").hashCode(), PrimitiveImpl.wrap("a", "aa").hashCode());
        assertNotEquals(PrimitiveImpl.wrap("a", "cc").hashCode(), PrimitiveImpl.wrap("a", "b").hashCode());
    }

    @Test
    public void stringify() {
        assertTrue(PrimitiveImpl.parseNotString("a", "true").toString().toLowerCase().contains("boolean"));
        assertTrue(PrimitiveImpl.parseNotString("a", "1212").toString().toLowerCase().contains("int"));
        assertTrue(PrimitiveImpl.parseNotString("a", "12S").toString().toLowerCase().contains("short"));
        assertTrue(PrimitiveImpl.parseNotString("a", "12B").toString().toLowerCase().contains("byte"));
        assertTrue(PrimitiveImpl.parseNotString("a", "12L").toString().toLowerCase().contains("long"));
        assertTrue(PrimitiveImpl.parseNotString("a", "12.0").toString().toLowerCase().contains("float"));
        assertTrue(PrimitiveImpl.parseNotString("a", "12.0D").toString().toLowerCase().contains("double"));
        assertTrue(PrimitiveImpl.wrap("a", 'c').toString().toLowerCase().contains("char"));
        assertTrue(PrimitiveImpl.wrap("a", "as").toString().toLowerCase().contains("string"));
    }
}