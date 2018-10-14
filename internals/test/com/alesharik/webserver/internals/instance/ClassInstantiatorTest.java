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

package com.alesharik.webserver.internals.instance;

import org.junit.Test;

import static org.junit.Assert.*;

public class ClassInstantiatorTest {
    @Test
    public void instantiateNullConstructor() {
        Class object = (Class) ClassInstantiator.instantiateNullConstructor(Class.class);
        assertNotNull(object);
        assertNull(object.a);
        assertEquals(0, object.b);
        //noinspection SimplifiableJUnitAssertion
        assertEquals(false, object.c);
        assertEquals(0, object.d);
        assertEquals(0, object.q);
        assertEquals(0, object.w, 0.1);
        assertEquals(0, object.f, 0.1);
        assertEquals(0, object.s);
        assertEquals(0, object.l);
    }

    @Test
    public void instantiateSerialization() {
        Class object = (Class) ClassInstantiator.instantiateSerialization(Class.class);
        assertNotNull(object);
        assertEquals("a", object.a);
        assertEquals(1, object.b);
        //noinspection SimplifiableJUnitAssertion
        assertEquals(true, object.c);
        assertEquals(1, object.d);
        assertEquals(1, object.q);
        assertEquals(1, object.w, 0.1);
        assertEquals(1, object.f, 0.1);
        assertEquals(1, object.s);
        assertEquals(1, object.l);
    }

    private static final class Class {
        final String a;
        final int b;
        final boolean c;
        final byte d;
        final char q;
        final double w;
        final float f;
        final short s;
        final long l;

        public Class(String a, int b, boolean c, byte d, char q, double w, float f, short s, long l) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.q = q;
            this.w = w;
            this.f = f;
            this.s = s;
            this.l = l;
        }

        public Class() {
            this.a = "a";
            this.b = 1;
            this.c = true;
            this.d = 1;
            this.q = 1;
            this.w = 1;
            this.f = 1;
            this.s = 1;
            this.l = 1;
        }
    }
}