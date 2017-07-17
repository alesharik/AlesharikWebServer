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

package com.alesharik.webserver.api.server.wrapper.http.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class WeightMimeTypeTest {
    @Test
    public void toMimeTypeTest() throws Exception {
        assertEquals("test/test;q=0.5", new WeightMimeType("test", "test", 0.5F).toMimeType());
        assertEquals("asd/asd", new WeightMimeType("asd", "asd", 1).toMimeType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseIllegalTypeTest() throws Exception {
        WeightMimeType weightMimeType = WeightMimeType.parseType("sdasd\\asdsad");
        assertNull(weightMimeType);
    }

    @Test
    public void parseTypeTest() throws Exception {
        WeightMimeType weightMimeType = WeightMimeType.parseType("test/test;q=0.25");
        assertEquals(new WeightMimeType("test", "test", 0.25F), weightMimeType);

        WeightMimeType weightMimeType1 = WeightMimeType.parseType("test/test");
        assertEquals(new WeightMimeType("test", "test", 1), weightMimeType1);
    }

    @Test
    public void parseIllegalTypeNullUnsafeTest() throws Exception {
        WeightMimeType weightMimeType = WeightMimeType.parseTypeNullUnsafe("sdasd\\asdsad");
        assertNull(weightMimeType);
    }

    @Test
    public void parseTypeNullUnsafeTest() throws Exception {
        WeightMimeType weightMimeType = WeightMimeType.parseTypeNullUnsafe("test/test;q=0.25");
        assertEquals(new WeightMimeType("test", "test", 0.25F), weightMimeType);

        WeightMimeType weightMimeType1 = WeightMimeType.parseTypeNullUnsafe("test/test");
        assertEquals(new WeightMimeType("test", "test", 1), weightMimeType1);
    }
}