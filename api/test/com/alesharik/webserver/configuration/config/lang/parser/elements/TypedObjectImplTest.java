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
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationTypedObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TypedObjectImplTest {
    @Test
    public void testParse() {
        TypedObjectImpl typedObject = TypedObjectImpl.parse("test:asd:1");
        assertEquals("test", typedObject.getName());
        assertEquals("asd:1", typedObject.getType());
    }

    @Test
    public void testParseIllegal() {
        assertNull(TypedObjectImpl.parse("asd"));
        assertNull(TypedObjectImpl.parse("asd:"));
        assertNull(TypedObjectImpl.parse(":asd"));
        assertNull(TypedObjectImpl.parse(":"));
    }

    @Test
    public void testParseExtend() {
        TypedObjectImpl typedObject = TypedObjectImpl.parse("test:asd:1", mockMap(testMap()));
        assertEquals("test", typedObject.getName());
        assertEquals("asd:1", typedObject.getType());
        assertEquals("a", typedObject.getElement("a").getName());
        assertEquals("b", typedObject.getElement("b").getName());
        assertEquals("c", typedObject.getElement("c").getName());
    }

    @Test
    public void testParseExtendIllegal() {
        assertNull(TypedObjectImpl.parse("asd", mockMap(testMap())));
        assertNull(TypedObjectImpl.parse("asd:", mockMap(testMap())));
        assertNull(TypedObjectImpl.parse(":asd", mockMap(testMap())));
        assertNull(TypedObjectImpl.parse(":", mockMap(testMap())));
    }

    @Test
    public void testOverride() {
        TypedObjectImpl typedObject = TypedObjectImpl.parse("test:asd", mockMap(testMap()));
        assertEquals("test", typedObject.getName());
        assertEquals("asd", typedObject.getType());
        assertEquals("a", typedObject.getElement("a").getName());
        assertEquals("b", typedObject.getElement("b").getName());
        assertEquals("c", typedObject.getElement("c").getName());

        typedObject.getEntries().put("a", createConfigElement("qwe"));
        assertEquals("qwe", typedObject.getElement("a").getName());
    }

    @Test
    public void testInternalMap() {
        TypedObjectImpl typedObject = TypedObjectImpl.parse("test:asd");
        typedObject.getEntries().put("a", createConfigElement("a"));
        assertTrue(typedObject.hasKey("a"));
        assertFalse(typedObject.hasKey("b"));
        assertEquals(1, typedObject.getSize());
        assertEquals("a", typedObject.getElement("a").getName());
        assertEquals("a", typedObject.getElement("a", ConfigurationElement.class).getName());
        assertEquals(1, typedObject.getNames().size());
        assertTrue(typedObject.getNames().contains("a"));
        assertFalse(typedObject.getNames().contains("b"));
        assertEquals("a", typedObject.getEntries().get("a").getName());
        assertNull(typedObject.getEntries().get("b"));

        assertNull(typedObject.getElement("dssasadsaads"));
        assertNull(typedObject.getElement("asdsasadasdsad", ConfigurationElement.class));
    }

    private static Map<String, ConfigurationElement> testMap() {
        Map<String, ConfigurationElement> map = new HashMap<>();
        map.put("a", createConfigElement("a"));
        map.put("b", createConfigElement("b"));
        map.put("c", createConfigElement("c"));
        return map;
    }

    private static Map<String, ConfigurationElement> testMapWithoutMock() {
        Map<String, ConfigurationElement> map = new HashMap<>();
        map.put("a", () -> "a");
        map.put("b", () -> "b");
        map.put("c", () -> "c");
        return map;
    }

    private static ConfigurationElement createConfigElement(String name) {
        ConfigurationElement element = mock(ConfigurationElement.class);
        when(element.getName()).thenReturn(name);
        return element;
    }

    private static ConfigurationTypedObject mockMap(Map<String, ConfigurationElement> map) {
        ConfigurationTypedObject object = new TypedObjectImpl("asd", "asd");
        object.getEntries().putAll(map);
        return object;
    }

    @Test
    public void equality() {
        TypedObjectImpl typedObject = TypedObjectImpl.parse("test:asd:1", mockMap(testMapWithoutMock()));
        TypedObjectImpl typedObject1 = TypedObjectImpl.parse("test:asd:1", mockMap(testMapWithoutMock()));

        assertEquals(typedObject, typedObject1);
        assertEquals(typedObject.hashCode(), typedObject1.hashCode());

        TypedObjectImpl typedObject2 = TypedObjectImpl.parse("test:asd:1", mockMap(testMapWithoutMock()));
        typedObject2.getEntries().remove(typedObject2.getEntries().keySet().iterator().next());

        assertNotEquals(typedObject, typedObject2);
        assertNotEquals(typedObject1, typedObject2);
        assertNotEquals(typedObject.hashCode(), typedObject2.hashCode());
        assertNotEquals(typedObject1.hashCode(), typedObject2.hashCode());
    }

    @Test
    public void stringify() {
        TypedObjectImpl typedObject = TypedObjectImpl.parse("test:asd:1", mockMap(testMap()));
        assertNotNull(typedObject);
    }
}