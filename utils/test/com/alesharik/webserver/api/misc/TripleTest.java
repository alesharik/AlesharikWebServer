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

package com.alesharik.webserver.api.misc;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TripleTest {
    private Triple<Integer, String, String> a;
    private Triple<Integer, String, String> same;
    private Triple<Integer, String, String> not;

    private Triple<Integer, String, String> serA;
    private Triple<Integer, String, String> serSame;
    private Triple<Integer, String, String> serNot;

    private Triple.MutableTriple<Integer, String, String> mutA;
    private Triple.MutableTriple<Integer, String, String> mutSame;
    private Triple.MutableTriple<Integer, String, String> mutNot;

    private Triple.MutableTriple<Integer, String, String> mutSerA;
    private Triple.MutableTriple<Integer, String, String> mutSerSame;
    private Triple.MutableTriple<Integer, String, String> mutSerNot;

    @Before
    public void setUp() {
        a = Triple.immutable(1, "asdfdsf", "bsdaf");
        same = a.clone();
        not = Triple.immutable(2, "sdaasdasd", "bsdaf");

        serA = Triple.immutableSerializable(1, "asdfdsf", "bsdaf");
        serSame = serA.clone();
        serNot = Triple.immutableSerializable(2, "sdaasdasd", "bsdaf");

        mutA = Triple.mutable(1, "asdfdsf", "bsdaf");
        mutSame = mutA.clone();
        mutNot = Triple.mutable(2, "sdaasdasd", "bsdaf");

        mutSerA = Triple.mutableSerializable(1, "asdfdsf", "bsdaf");
        mutSerSame = mutSerA.clone();
        mutSerNot = Triple.mutableSerializable(2, "sdaasdasd", "bsdaf");
    }

    @Test
    public void testImmutableEquals() {
        assertEquals(a, same);
        assertFalse(a.equals(not));

        assertEquals(a, serA);
        assertEquals(a, serSame);
        assertFalse(a.equals(serNot));

        assertEquals(serA, serSame);
        assertFalse(serA.equals(serNot));
    }

    @Test
    public void testMutableEquals() {
        assertEquals(mutA, mutSame);
        assertFalse(mutA.equals(mutNot));

        assertEquals(mutA, mutSerA);
        assertEquals(mutA, mutSerSame);
        assertFalse(mutA.equals(mutSerNot));

        assertEquals(mutSerA, mutSerSame);
        assertFalse(mutSerA.equals(mutSerNot));
    }

    @Test
    public void testClone() {
        assertEquals(a, a.clone());
        assertEquals(serA, serA.clone());
        assertEquals(mutA, mutA.clone());
        assertEquals(mutSerA, mutSerA.clone());
    }
}