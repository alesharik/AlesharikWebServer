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

package com.alesharik.webserver.api.server.wrapper.http;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HttpVersionTest {
    @Test
    public void getValueTest() throws Exception {
        assertEquals(HttpVersion.HTTP_0_9, HttpVersion.getValue("HTTP/0.9"));
        assertEquals(HttpVersion.HTTP_1_0, HttpVersion.getValue("HTTP/1.0"));
        assertEquals(HttpVersion.HTTP_1_1, HttpVersion.getValue("HTTP/1.1"));
        assertEquals(HttpVersion.HTTP_2, HttpVersion.getValue("HTTP/2"));
        assertNull(HttpVersion.getValue("HTTP!"));
    }
}