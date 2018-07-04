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

import com.alesharik.webserver.module.http.http.data.ETag;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IfETagHeaderTest {
    private IfETagHeader header;

    @Before
    public void setUp() throws Exception {
        header = new IfETagHeader("Tess");
    }

    @Test
    public void buildTest() throws Exception {
        ETag[] tags = new ETag[]{
                new ETag("test", true),
                new ETag("asd", false),
                ETag.ANY_TAG
        };
        assertEquals("Tess: W/\"test\", \"asd\", *", header.build(tags));
    }

    @Test
    public void parseTest() throws Exception {
        ETag[] tags = header.getValue("Tess: W/\"test\", \"asd\", *");
        assertEquals(3, tags.length);

        assertEquals("test", tags[0].getTag());
        assertTrue(tags[0].isWeak());

        assertEquals("asd", tags[1].getTag());
        assertFalse(tags[1].isWeak());

        assertEquals(ETag.ANY_TAG, tags[2]);
    }
}