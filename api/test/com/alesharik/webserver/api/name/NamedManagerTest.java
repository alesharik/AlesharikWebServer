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

package com.alesharik.webserver.api.name;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NamedManagerTest {

    @Before
    public void setUp() throws Exception {
        NamedManager.entries.clear();
    }

    @Test
    public void listenNamedClass() throws Exception {
        NamedManager.listenNamedClass(Test1.class);
        assertEquals(1, NamedManager.entries.size());
        assertEquals(Test1.class, NamedManager.entries.keys().nextElement());
        assertEquals("Test", NamedManager.entries.get(Test1.class));
    }

    @Test
    public void getClassForName() throws Exception {
        NamedManager.listenNamedClass(Test1.class);

        assertEquals(Test1.class, NamedManager.getClassForName("Test"));

        assertNull(NamedManager.getClassForName("Wat"));
    }

    @Test
    public void getClassForNameAndType() throws Exception {
        NamedManager.listenNamedClass(Test1.class);

        assertEquals(Test1.class, NamedManager.getClassForNameAndType("Test", Error.class));
        assertNull(NamedManager.getClassForNameAndType("Test", Exception.class));
    }

    @Named("Test")
    private static final class Test1 extends Error {
        private static final long serialVersionUID = 7273125240944974271L;
    }
}