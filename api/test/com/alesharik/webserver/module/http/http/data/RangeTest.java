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

public class RangeTest {
    @Test
    public void testHasGetters() throws Exception {
        assertTrue(new Range(1, 2, AcceptRangesHeader.RangeType.BYTES).hasEnd());
        assertFalse(new Range(1, AcceptRangesHeader.RangeType.BYTES).hasEnd());
    }

    @Test
    public void parseTest() throws Exception {
        Range range = Range.parse(AcceptRangesHeader.RangeType.BYTES, "1-2");
        assertTrue(range.hasEnd());
        assertEquals(AcceptRangesHeader.RangeType.BYTES, range.getRangeType());
        assertEquals(1, range.getStart());
        assertEquals(2, range.getEnd());

        Range range1 = Range.parse(AcceptRangesHeader.RangeType.BYTES, "1-");
        assertFalse(range1.hasEnd());
        assertEquals(AcceptRangesHeader.RangeType.BYTES, range1.getRangeType());
        assertEquals(1, range1.getStart());
    }

    @Test
    public void buildTest() throws Exception {
        assertEquals("1-2", new Range(1, 2, AcceptRangesHeader.RangeType.BYTES).toHeaderString());
        assertEquals("2-", new Range(2, AcceptRangesHeader.RangeType.BYTES).toHeaderString());
    }
}