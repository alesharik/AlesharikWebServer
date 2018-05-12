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

import com.alesharik.webserver.configuration.config.lang.element.ConfigurationElement;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class ArrayImplTest {

    public static final ConfigurationElement MOCK = mock(ConfigurationElement.class);

    @Test(expected = IllegalArgumentException.class)
    public void getName() {
        ArrayImpl array = new ArrayImpl("test");
        assertEquals("test", array.getName());

        new ArrayImpl(null);
        fail();
    }

    @Test
    public void size() {
        ArrayImpl array = new ArrayImpl("test");
        assertEquals(0, array.size());
        array.append(mockConfigElement());
        array.append(mockConfigElement());
        array.append(mockConfigElement());
        assertEquals(3, array.size());
    }

    @Test
    public void get() {
        ArrayImpl array = new ArrayImpl("test");
        assertEquals(0, array.size());
        assertNull(array.get(0));

        ConfigurationElement a = mockConfigElement();
        ConfigurationElement b = mockConfigElement();
        ConfigurationElement c = mockConfigElement();
        array.append(a);
        array.append(b);
        array.append(c);

        assertSame(a, array.get(0));
        assertSame(b, array.get(1));
        assertSame(c, array.get(2));
        assertNull(array.get(-1));
        assertNull(array.get(3));
    }

    @Test
    public void getElements() {
        ArrayImpl array = new ArrayImpl("test");
        assertEquals(0, array.getElements().length);


        ConfigurationElement a = mockConfigElement();
        ConfigurationElement b = mockConfigElement();
        ConfigurationElement c = mockConfigElement();
        array.append(a);
        array.append(b);
        array.append(c);

        ConfigurationElement[] arr = array.getElements();
        assertSame(a, arr[0]);
        assertSame(b, arr[1]);
        assertSame(c, arr[2]);
    }

    @Test
    public void iterator() {
        ArrayImpl array = new ArrayImpl("test");
        assertFalse(array.iterator().hasNext());

        ConfigurationElement a = mockConfigElement();
        ConfigurationElement b = mockConfigElement();
        ConfigurationElement c = mockConfigElement();
        array.append(a);
        array.append(b);
        array.append(c);

        Iterator<ConfigurationElement> iterator = array.iterator();
        int i = 0;
        while(iterator.hasNext()) {
            ConfigurationElement next = iterator.next();
            if(i == 0)
                assertSame(a, next);
            else if(i == 1)
                assertSame(b, next);
            else if(i == 2)
                assertSame(c, next);
            else
                fail();
            i++;
        }
    }

    @Test
    public void equality() {
        ArrayImpl array = new ArrayImpl("test");
        assertEquals(0, array.size());
        array.append(mockConfigElement());
        array.append(mockConfigElement());
        array.append(mockConfigElement());

        ArrayImpl array1 = new ArrayImpl("test");
        assertEquals(0, array1.size());
        array1.append(mockConfigElement());
        array1.append(mockConfigElement());
        array1.append(mockConfigElement());

        ArrayImpl array2 = new ArrayImpl("test");
        assertEquals(0, array2.size());
        array2.append(mockConfigElement());
        array2.append(mockConfigElement());

        assertEquals(array, array1);
        assertNotEquals(array, array2);
        assertNotEquals(array1, array2);

        assertEquals(array.hashCode(), array1.hashCode());
        assertNotEquals(array.hashCode(), array2.hashCode());
        assertNotEquals(array1.hashCode(), array2.hashCode());
    }

    @Test
    public void stringify() {
        ArrayImpl array = new ArrayImpl("test");
        assertEquals(0, array.size());

        assertNotNull(array.toString());

        array.append(mockConfigElement());
        array.append(mockConfigElement());
        array.append(mockConfigElement());

        assertNotNull(array.toString());
    }

    private ConfigurationElement mockConfigElement() {
        return MOCK;
    }

}