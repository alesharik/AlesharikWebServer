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

import com.alesharik.webserver.module.http.http.data.WeightCharset;
import org.glassfish.grizzly.utils.Charsets;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

public class AcceptCharsetHeaderTest {
    @Test
    public void getValueTest() throws Exception {
        AcceptCharsetHeader header = new AcceptCharsetHeader();
        String testHeader = "Accept-Charset: utf-8, iso-8859-1;q=0.5, *;q=0.1, asd";
        WeightCharset[] charsets = header.getValue(testHeader);

        assertEquals(3, charsets.length);
        assertEquals(new WeightCharset(Charsets.UTF8_CHARSET), charsets[0]);
        assertEquals(new WeightCharset(Charset.forName("ISO-8859-1"), 0.5F), charsets[1]);
        assertEquals(WeightCharset.anyCharset(0.1F), charsets[2]);
    }

    @Test
    public void buildTest() throws Exception {
        AcceptCharsetHeader header = new AcceptCharsetHeader();
        String testHeader = "Accept-Charset: utf-8, iso-8859-1;q=0.5, *;q=0.1";
        WeightCharset[] toBuild = new WeightCharset[]{new WeightCharset(Charsets.UTF8_CHARSET),
                new WeightCharset(Charset.forName("ISO-8859-1"), 0.5F),
                WeightCharset.anyCharset(0.1F)};

        assertEquals(testHeader, header.build(toBuild));
    }
}