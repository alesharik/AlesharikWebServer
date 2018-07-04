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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringHeaderTest {
    private StringHeader header;

    @Before
    public void setUp() throws Exception {
        header = new StringHeader("ASd");
    }

    @Test
    public void getValueTest() throws Exception {
        assertEquals("test", header.getValue("ASd: test"));
    }

    @Test
    public void buildTest() throws Exception {
        assertEquals("ASd: test", header.build("test"));
    }
}