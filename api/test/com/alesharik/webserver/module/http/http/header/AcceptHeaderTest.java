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

package com.alesharik.webserver.module.http.http.header;

import com.alesharik.webserver.module.http.http.data.WeightMimeType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AcceptHeaderTest {
    private AcceptHeader header;

    @Before
    public void setUp() throws Exception {
        header = new AcceptHeader();
    }

    @Test
    public void getValue() throws Exception {
        WeightMimeType[] value = header.getValue("Accept: */*, test/*;q=0.5, test/asd;q=0.1");
        assertEquals(3, value.length);

        assertTrue(value[0].isAnyType());
        assertEquals(1.0F, value[0].getWeight(), 0.000001);

        assertTrue(value[1].isAnySubType());
        assertFalse(value[1].isAnyType());
        assertEquals("test", value[1].getType());
        assertEquals(0.5F, value[1].getWeight(), 0.000001);

        assertFalse(value[2].isAnySubType());
        assertFalse(value[2].isAnyType());
        assertEquals("test", value[2].getType());
        assertEquals("asd", value[2].getSubType());
        assertEquals(0.1F, value[2].getWeight(), 0.000001);
    }

    @Test
    public void build() throws Exception {
        WeightMimeType[] types = new WeightMimeType[]{
                new WeightMimeType("test", "test", 1.0F),
                new WeightMimeType("*", "asd", 0.5F),
                new WeightMimeType("*", "*", 0.1F)
        };
        assertEquals("Accept: test/test, */asd;q=0.5, */*;q=0.1", header.build(types));
    }

}