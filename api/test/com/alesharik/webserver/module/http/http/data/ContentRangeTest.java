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

import com.alesharik.webserver.module.http.http.header.AcceptRangesHeader;
import org.junit.Test;

import static org.junit.Assert.*;

public class ContentRangeTest {
    @Test
    public void testIsGetters() throws Exception {
        assertTrue(new ContentRange(AcceptRangesHeader.RangeType.BYTES, 1, 1, 1).hasSize());
        assertTrue(new ContentRange(AcceptRangesHeader.RangeType.BYTES, 1, 1, 1).hasRange());

        assertFalse(new ContentRange(AcceptRangesHeader.RangeType.BYTES, 1).hasRange());
        assertFalse(new ContentRange(AcceptRangesHeader.RangeType.BYTES, 1, 2).hasSize());
    }

    @Test
    public void testParse() throws Exception {
        ContentRange range1 = ContentRange.fromHeaderString("bytes 1-2/3");
        assertTrue(range1.hasSize());
        assertTrue(range1.hasRange());

        assertEquals(1, range1.getStart());
        assertEquals(2, range1.getEnd());
        assertEquals(3, range1.getSize());

        ContentRange range2 = ContentRange.fromHeaderString("bytes */3");
        assertFalse(range2.hasRange());
        assertTrue(range2.hasSize());

        assertEquals(3, range2.getSize());

        ContentRange range3 = ContentRange.fromHeaderString("bytes 1-2/*");
        assertTrue(range3.hasRange());
        assertFalse(range3.hasSize());

        assertEquals(1, range3.getStart());
        assertEquals(2, range3.getEnd());
    }

    @Test
    public void testBuild() throws Exception {
        assertEquals("bytes 1-2/3", new ContentRange(AcceptRangesHeader.RangeType.BYTES, 1, 2, 3).toHeaderString());
        assertEquals("bytes */3", new ContentRange(AcceptRangesHeader.RangeType.BYTES, 3).toHeaderString());
        assertEquals("bytes 1-2/*", new ContentRange(AcceptRangesHeader.RangeType.BYTES, 1, 2).toHeaderString());
    }
}