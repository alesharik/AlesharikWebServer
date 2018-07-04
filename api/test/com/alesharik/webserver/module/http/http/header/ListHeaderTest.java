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

import java.nio.ByteOrder;

import static org.junit.Assert.assertEquals;

public class ListHeaderTest {
    private ListHeader<ByteOrder> header;

    @Before
    public void setUp() throws Exception {
        header = new ListHeader<>("ByteOrder", new ListHeader.Factory<ByteOrder>() {
            @Override
            public ByteOrder newInstance(String value) {
                return value.equals("bigEndian") ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
            }

            @Override
            public String toString(ByteOrder byteOrder) {
                return byteOrder == ByteOrder.BIG_ENDIAN ? "bigEndian" : "littleEndian";
            }

            @Override
            public ByteOrder[] newArray(int size) {
                return new ByteOrder[size];
            }
        });
    }

    @Test
    public void getValueTest() throws Exception {
        ByteOrder[] orders = header.getValue("ByteOrder: bigEndian, bigEndian, littleEndian, bigEndian");

        assertEquals(4, orders.length);
        assertEquals(ByteOrder.BIG_ENDIAN, orders[0]);
        assertEquals(ByteOrder.BIG_ENDIAN, orders[1]);
        assertEquals(ByteOrder.LITTLE_ENDIAN, orders[2]);
        assertEquals(ByteOrder.BIG_ENDIAN, orders[3]);
    }

    @Test
    public void buildTest() throws Exception {
        ByteOrder[] orders = new ByteOrder[]{
                ByteOrder.BIG_ENDIAN,
                ByteOrder.LITTLE_ENDIAN,
                ByteOrder.BIG_ENDIAN,
                ByteOrder.LITTLE_ENDIAN
        };

        assertEquals("ByteOrder: bigEndian, littleEndian, bigEndian, littleEndian", header.build(orders));
    }
}