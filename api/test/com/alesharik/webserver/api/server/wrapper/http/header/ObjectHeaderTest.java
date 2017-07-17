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

package com.alesharik.webserver.api.server.wrapper.http.header;

import org.junit.Before;
import org.junit.Test;

import java.nio.ByteOrder;

import static org.junit.Assert.assertEquals;

public class ObjectHeaderTest {
    private ObjectHeader<ByteOrder> header;

    @Before
    public void setUp() throws Exception {
        header = new ObjectHeader<>("Sdf", new ObjectHeader.Factory<ByteOrder>() {
            @Override
            public ByteOrder newInstance(String s) {
                return s.equals("big") ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
            }

            @Override
            public String toString(ByteOrder byteOrder) {
                return byteOrder == ByteOrder.BIG_ENDIAN ? "big" : "little";
            }
        });
    }

    @Test
    public void getValueTest() throws Exception {
        assertEquals(ByteOrder.BIG_ENDIAN, header.getValue("Sdf: big"));
        assertEquals(ByteOrder.LITTLE_ENDIAN, header.getValue("Sdf: little"));
    }

    @Test
    public void buildTest() throws Exception {
        assertEquals("Sdf: big", header.build(ByteOrder.BIG_ENDIAN));
        assertEquals("Sdf: little", header.build(ByteOrder.LITTLE_ENDIAN));
    }
}