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

package com.alesharik.webserver.module.http.http.data;

import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import static org.junit.Assert.*;

public class ContentTypeTest {
    @Test
    public void parseBasic() {
        ContentType parse = ContentType.parse("text/simple");
        assertNull(parse.getCharset());
        assertFalse(parse.hasBoundary());
        assertNull(parse.getBoundary());
        assertEquals(new MimeType("text", "simple"), parse.getType());
    }

    @Test
    public void parseCharset() {
        ContentType type = ContentType.parse("text/simple; charset=UTF-8");
        assertEquals(Charset.forName("UTF-8"), type.getCharset());
        assertFalse(type.hasBoundary());
        assertNull(type.getBoundary());
        assertEquals(new MimeType("text", "simple"), type.getType());
    }

    @Test(expected = UnsupportedCharsetException.class)
    public void parseCharsetIllegal() {
        ContentType.parse("text/simple; charset=WAT");
        fail();
    }

    @Test
    public void parseBoundary() {
        ContentType parse = ContentType.parse("multipart/form-data; boundary=test");
        assertNull(parse.getCharset());
        assertTrue(parse.hasBoundary());
        assertEquals("test", parse.getBoundary());
        assertEquals(new MimeType("multipart", "form-data"), parse.getType());
    }

    @Test
    public void toStringBasic() {
        ContentType parse = ContentType.parse("text/simple");
        assertEquals("text/simple", parse.toHeaderString());
    }

    @Test
    public void toStringCharset() {
        ContentType type = ContentType.parse("text/simple; charset=UTF-8");
        assertEquals("text/simple; charset=UTF-8", type.toHeaderString());
    }

    @Test
    public void toStringBoundary() {
        ContentType type = ContentType.parse("multipart/form-data; boundary=test");
        assertEquals("multipart/form-data; boundary=test", type.toHeaderString());
    }
}