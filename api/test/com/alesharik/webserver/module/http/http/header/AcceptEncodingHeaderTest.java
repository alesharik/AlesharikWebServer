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

import com.alesharik.webserver.module.http.http.data.Encoding;
import com.alesharik.webserver.module.http.http.data.WeightEncoding;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class AcceptEncodingHeaderTest {
    private AcceptEncodingHeader header;

    @Before
    public void setUp() throws Exception {
        header = new AcceptEncodingHeader();
    }

    @Test
    public void getValueTest() throws Exception {
        WeightEncoding[] encodings = header.getValue("Accept-Encoding: br, gzip;q=0.8, *;q=0.1, test;q=0.05");

        assertEquals(4, encodings.length);

        assertEquals(Encoding.BR, encodings[0].getEncoding());
        assertEquals(1.0F, encodings[0].getWeight(), 0.00005);

        assertEquals(Encoding.GZIP, encodings[1].getEncoding());
        assertEquals(0.8F, encodings[1].getWeight(), 0.00005);

        assertEquals(Encoding.ALL, encodings[2].getEncoding());
        assertEquals(0.1F, encodings[2].getWeight(), 0.00005);

        assertEquals(Encoding.ALL, encodings[3].getEncoding());
        assertEquals(0.05F, encodings[3].getWeight(), 0.00005);
    }

    @Test
    public void buildTest() throws Exception {
        WeightEncoding[] encodings = new WeightEncoding[]{
                new WeightEncoding(Encoding.BR),
                new WeightEncoding(Encoding.GZIP, 0.8F),
                new WeightEncoding(Encoding.ALL, 0.1F)
        };
        assertEquals("Accept-Encoding: br, gzip;q=0.8, *;q=0.1", header.build(encodings));
    }
}