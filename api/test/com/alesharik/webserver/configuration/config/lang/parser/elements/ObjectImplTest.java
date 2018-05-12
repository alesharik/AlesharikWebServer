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
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObject;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ObjectImplTest {
    @Test
    public void testInternalMap() {
        ConfigurationObject o = new ObjectImpl("test");
        o.getEntries().put("a", createConfigElement("a"));
        assertTrue(o.hasKey("a"));
        assertFalse(o.hasKey("b"));
        assertEquals(1, o.getSize());
        assertEquals("a", o.getElement("a").getName());
        assertEquals("a", o.getElement("a", ConfigurationElement.class).getName());
        assertEquals(1, o.getNames().size());
        assertTrue(o.getNames().contains("a"));
        assertFalse(o.getNames().contains("b"));
        assertEquals("a", o.getEntries().get("a").getName());
        assertNull(o.getEntries().get("b"));

        assertNull(o.getElement("dssasadsaads"));
        assertNull(o.getElement("asdsasadasdsad", ConfigurationElement.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testName() {
        ConfigurationObject o = new ObjectImpl("test");
        assertEquals("test", o.getName());

        new ObjectImpl(null);
        fail();
    }

    private static ConfigurationElement createConfigElement(@SuppressWarnings("SameParameterValue") String name) {
        ConfigurationElement element = mock(ConfigurationElement.class);
        when(element.getName()).thenReturn(name);
        return element;
    }

    @Test
    public void equality() {
        ConfigurationElement element = createConfigElement("a");
        ObjectImpl object = new ObjectImpl("a");
        object.append(element);
        ObjectImpl object1 = new ObjectImpl("a");
        object1.append(element);

        assertEquals(object, object1);
        assertEquals(object.hashCode(), object1.hashCode());

        ObjectImpl object2 = new ObjectImpl("a");
        assertNotEquals(object, object2);
        assertNotEquals(object1, object2);
        assertNotEquals(object.hashCode(), object2.hashCode());
        assertNotEquals(object1.hashCode(), object2.hashCode());

        ObjectImpl object3 = new ObjectImpl("b");
        assertNotEquals(object, object3);
        assertNotEquals(object1, object3);
        assertNotEquals(object.hashCode(), object3.hashCode());
        assertNotEquals(object1.hashCode(), object3.hashCode());
    }

    @Test
    public void stringify() {
        ObjectImpl object = new ObjectImpl("");

        assertNotNull(object.toString());

        ObjectImpl object1 = new ObjectImpl("a");
        object1.append(createConfigElement("a"));
        object1.append(createConfigElement("b"));

        assertNotNull(object.toString());
    }
}