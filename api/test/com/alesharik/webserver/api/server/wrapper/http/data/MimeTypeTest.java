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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MimeTypeTest {
    private MimeType mimeType;

    @Before
    public void setUp() throws Exception {
        mimeType = new MimeType("test", "test");
    }

    @Test
    public void toMimeTypeTest() throws Exception {
        assertEquals("test/test", mimeType.toMimeType());
    }

    @Test
    public void parseMimeTypeTest() throws Exception {
        MimeType mimeType = MimeType.parseType("test/test");
        assertEquals(this.mimeType, mimeType);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseIncorrectMimeTypeTest() throws Exception {
        MimeType mimeType = MimeType.parseType("test\\test"); // '\' != '/'
        assertEquals(this.mimeType, mimeType);
    }

    @Test
    public void anyTypeTest() throws Exception {
        assertFalse(mimeType.isAnyType());
        assertTrue(new MimeType("*", "*").isAnyType());
        assertFalse(new MimeType("*", "asdf").isAnyType());
        assertFalse(new MimeType("asdf", "*").isAnyType());
    }

    @Test
    public void isAnySubtypeTest() throws Exception {
        assertFalse(mimeType.isAnySubType());
        assertTrue(new MimeType("*", "*").isAnySubType());
        assertFalse(new MimeType("*", "asdf").isAnySubType());
        assertTrue(new MimeType("asdf", "*").isAnySubType());
    }
}