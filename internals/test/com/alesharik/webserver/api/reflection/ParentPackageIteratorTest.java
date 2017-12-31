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

package com.alesharik.webserver.api.reflection;

import com.alesharik.webserver.api.reflection.c.A1;
import com.alesharik.webserver.api.reflection.c.a.A2;
import com.alesharik.webserver.api.reflection.c.a.a.A3;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ParentPackageIteratorTest {
    @Test
    public void forPackage() {
        Iterator<Package> iterator = ParentPackageIterator.forPackage("com.alesharik.webserver.api.reflection.c.a.a");
        List<Integer> order = new ArrayList<>();
        while(iterator.hasNext()) {
            Package next = iterator.next();
            if(next.getName().equals("com.alesharik.webserver.api.reflection.c") && next.isAnnotationPresent(A1.class))
                order.add(1);
            if(next.getName().equals("com.alesharik.webserver.api.reflection.c.a") && next.isAnnotationPresent(A2.class))
                order.add(2);
            if(next.getName().equals("com.alesharik.webserver.api.reflection.c.a.a") && next.isAnnotationPresent(A3.class))
                order.add(3);
        }
        assertEquals(3, order.size());
        assertArrayEquals(new Integer[]{1, 2, 3}, order.toArray(new Integer[0]));
    }


    @Test
    public void forPackageReverse() {
        Iterator<Package> iterator = ParentPackageIterator.forPackageReverse("com.alesharik.webserver.api.reflection.c.a.a");
        List<Integer> order = new ArrayList<>();
        while(iterator.hasNext()) {
            Package next = iterator.next();
            if(next.getName().equals("com.alesharik.webserver.api.reflection.c") && next.isAnnotationPresent(A1.class))
                order.add(1);
            if(next.getName().equals("com.alesharik.webserver.api.reflection.c.a") && next.isAnnotationPresent(A2.class))
                order.add(2);
            if(next.getName().equals("com.alesharik.webserver.api.reflection.c.a.a") && next.isAnnotationPresent(A3.class))
                order.add(3);
        }
        assertEquals(3, order.size());
        assertArrayEquals(new Integer[]{3, 2, 1}, order.toArray(new Integer[0]));
    }
}