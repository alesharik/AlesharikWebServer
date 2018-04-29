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

package com.alesharik.webserver.configuration.config.lang.element;

import com.alesharik.webserver.configuration.config.lang.FormatException;
import com.alesharik.webserver.configuration.config.lang.parser.elements.PrimitiveImpl;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurationObjectArrayTest {
    @Test
    public void toIntArray() {
        ConfigurationObjectArray array = mockArr(PrimitiveImpl.parseNotString("a", "1"), PrimitiveImpl.parseNotString("b", "2"));
        int[] arr = array.toIntArray();
        assertEquals(1, arr[0]);
        assertEquals(2, arr[1]);
    }

    @Test(expected = FormatException.class)
    public void toIntArrayIllegal() {
        ConfigurationObjectArray array = mockArr(PrimitiveImpl.parseNotString("a", "1S"));
        array.toIntArray();
        fail();
    }

    @Test
    public void toByteArray() {
        ConfigurationObjectArray array = mockArr(PrimitiveImpl.parseNotString("a", "1B"), PrimitiveImpl.parseNotString("b", "2B"));
        byte[] arr = array.toByteArray();
        assertEquals(1, arr[0]);
        assertEquals(2, arr[1]);
    }

    @Test(expected = FormatException.class)
    public void toByteArrayIllegal() {
        ConfigurationObjectArray array = mockArr(PrimitiveImpl.parseNotString("a", "1"), PrimitiveImpl.parseNotString("b", "2"));
        array.toByteArray();
        fail();
    }

    @Test
    public void toShortArray() {
        ConfigurationObjectArray array = mockArr(PrimitiveImpl.parseNotString("a", "1S"), PrimitiveImpl.parseNotString("b", "2S"));
        short[] arr = array.toShortArray();
        assertEquals(1, arr[0]);
        assertEquals(2, arr[1]);
    }

    @Test(expected = FormatException.class)
    public void toShortArrayIllegal() {
        ConfigurationObjectArray array = mockArr(PrimitiveImpl.parseNotString("a", "1"), PrimitiveImpl.parseNotString("b", "2"));
        array.toShortArray();
        fail();
    }

    @Test
    public void toLongArray() {
        ConfigurationObjectArray array = mockArr(PrimitiveImpl.parseNotString("a", "1L"), PrimitiveImpl.parseNotString("b", "2L"));
        long[] arr = array.toLongArray();
        assertEquals(1, arr[0]);
        assertEquals(2, arr[1]);
    }

    @Test(expected = FormatException.class)
    public void toLongArrayIllegal() {
        ConfigurationObjectArray array = mockArr(PrimitiveImpl.parseNotString("a", "1"), PrimitiveImpl.parseNotString("b", "2"));
        array.toLongArray();
        fail();
    }

    @Test
    public void toFloatArray() {
        ConfigurationObjectArray array = mockArr(PrimitiveImpl.parseNotString("a", "1.1"), PrimitiveImpl.parseNotString("b", "2.1"));
        float[] arr = array.toFloatArray();
        assertEquals(1.1, arr[0], 0.01);
        assertEquals(2.1, arr[1], 0.01);
    }

    @Test(expected = FormatException.class)
    public void toFloatArrayIllegal() {
        ConfigurationObjectArray array = mockArr(PrimitiveImpl.parseNotString("a", "1"), PrimitiveImpl.parseNotString("b", "2"));
        array.toFloatArray();
        fail();
    }

    @Test
    public void toDoubleArray() {
        ConfigurationObjectArray array = mockArr(PrimitiveImpl.parseNotString("a", "1.1D"), PrimitiveImpl.parseNotString("b", "2.1D"));
        double[] arr = array.toDoubleArray();
        assertEquals(1.1, arr[0], 0.01);
        assertEquals(2.1, arr[1], 0.01);
    }

    @Test(expected = FormatException.class)
    public void toDoubleArrayIllegal() {
        ConfigurationObjectArray array = mockArr(PrimitiveImpl.parseNotString("a", "1.1"), PrimitiveImpl.parseNotString("b", "2.1"));
        array.toDoubleArray();
        fail();
    }

    @Test
    public void toCharArray() {
        ConfigurationObjectArray array = mockArr(PrimitiveImpl.wrap("a", 'a'), PrimitiveImpl.wrap("b", 'b'));
        char[] arr = array.toCharArray();
        assertEquals('a', arr[0]);
        assertEquals('b', arr[1]);
    }

    @Test(expected = FormatException.class)
    public void toCharArrayIllegal() {
        ConfigurationObjectArray array = mockArr(PrimitiveImpl.parseNotString("a", "1.1"), PrimitiveImpl.parseNotString("b", "2.1"));
        array.toCharArray();
        fail();
    }

    @Test
    public void toBooleanArray() {
        ConfigurationObjectArray array = mockArr(PrimitiveImpl.parseNotString("a", "true"), PrimitiveImpl.parseNotString("b", "false"));
        boolean[] arr = array.toBooleanArray();
        assertTrue(arr[0]);
        assertFalse(arr[1]);
    }

    @Test(expected = FormatException.class)
    public void toBooleanArrayIllegal() {
        ConfigurationObjectArray array = mockArr(PrimitiveImpl.parseNotString("a", "1.1"), PrimitiveImpl.parseNotString("b", "2.1"));
        array.toBooleanArray();
        fail();
    }

    @Test
    public void toStringArray() {
        ConfigurationObjectArray array = mockArr(PrimitiveImpl.wrap("a", "asd"), PrimitiveImpl.wrap("b", "qwe"));
        String[] arr = array.toStringArray();
        assertEquals("asd", arr[0]);
        assertEquals("qwe", arr[1]);
    }

    @Test(expected = FormatException.class)
    public void toStringArrayIllegal() {
        ConfigurationObjectArray array = mockArr(PrimitiveImpl.parseNotString("a", "1.1"), PrimitiveImpl.parseNotString("b", "2.1"));
        array.toStringArray();
        fail();
    }

    private ConfigurationObjectArray mockArr(ConfigurationPrimitive... primitives) {
        ConfigurationObjectArray mock = mock(ConfigurationObjectArray.class);
        when(mock.get(anyInt())).then(invocation -> primitives[(int) invocation.getArgument(0)]);
        when(mock.size()).thenReturn(primitives.length);

        when(mock.toBooleanArray()).thenCallRealMethod();
        when(mock.toByteArray()).thenCallRealMethod();
        when(mock.toCharArray()).thenCallRealMethod();
        when(mock.toDoubleArray()).thenCallRealMethod();
        when(mock.toFloatArray()).thenCallRealMethod();
        when(mock.toIntArray()).thenCallRealMethod();
        when(mock.toLongArray()).thenCallRealMethod();
        when(mock.toShortArray()).thenCallRealMethod();
        when(mock.toStringArray()).thenCallRealMethod();

        return mock;
    }
}