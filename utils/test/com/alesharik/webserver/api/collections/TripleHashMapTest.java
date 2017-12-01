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

package com.alesharik.webserver.api.collections;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TripleHashMapTest {
    private TripleHashMap<Integer, Integer, Integer> map;
    private TripleHashMap<Integer, Integer, Integer> toClear;
    private TripleHashMap<Integer, Integer, Integer> readOnly;
    private TripleHashMap<Integer, Integer, Integer> empty;
    private TripleHashMap<Integer, Integer, Integer> replaceAll;

    private Map<Integer, Integer> toPut;
    private TripleHashMap<Integer, Integer, Integer> toPut1;

    @Before
    public void setUp() {
        map = new TripleHashMap<>();
        for(int i = 1; i <= 13; i++) {
            map.put(i, i, i);
        }

        readOnly = new TripleHashMap<>();
        for(int i = 1; i <= 10; i++) {
            readOnly.put(i, i, i);
        }
        toClear = new TripleHashMap<>();
        for(int i = 0; i < 1000; i++) {
            toClear.put(i, i + 1, i + 2);
        }
        empty = new TripleHashMap<>();

        toPut = new HashMap<>();
        for(int i = 150; i < 200; i++) {
            toPut.put(i, i);
        }

        toPut1 = new TripleHashMap<>();
        for(int i = 250; i < 300; i++) {
            toPut1.put(i, i, i);
        }

        replaceAll = new TripleHashMap<>();
        for(int i = 0; i < 100; i++) {
            replaceAll.put(i, i, i);
        }
    }

    @Test
    public void size() {
        assertTrue(readOnly.size() == 10);
    }

    @Test
    public void isEmpty() {
        assertFalse(readOnly.isEmpty());
        assertTrue(empty.isEmpty());
    }

    @Test
    public void get() {
        assertTrue(readOnly.get(1) == 1);
        assertTrue(readOnly.get(1000) == null);
    }

    @Test
    public void getAddition() {
        assertTrue(readOnly.getAddition(1) == 1);
        assertTrue(readOnly.getAddition(1000) == null);
    }

    @Test
    public void containsKey() {
        assertTrue(readOnly.containsKey(1));
        assertFalse(readOnly.containsKey(100));
    }

    @Test
    public void containsValue() {
        assertTrue(readOnly.containsValue(1));
        assertFalse(readOnly.containsValue(1000));
    }

    @Test
    public void containsAddition() {
        assertTrue(readOnly.containsAddition(2));
        assertFalse(readOnly.containsAddition(1000));
    }

    @Test
    public void put() {
        map.put(100, 100, 100);
        assertTrue(map.containsKey(100) && map.get(100) == 100 && map.getAddition(100) == 100);
    }

    @Test
    public void putAll() {
        map.putAll(toPut);
        assertTrue(map.containsKey(175));
    }

    @Test
    public void putAll1() {
        map.putAll(toPut1);
        assertTrue(map.containsKey(275));
    }

    @Test
    public void remove() {
        map.remove(9);
        assertFalse(map.containsKey(9));
    }

    @Test
    public void clear() {
        toClear.clear();
        assertTrue(toClear.isEmpty());
    }

    @Test
    public void keySet() {
        assertTrue(readOnly.keySet().contains(1));
    }

    @Test
    public void values() {
        assertTrue(readOnly.values().contains(1));
    }

    @Test
    public void additions() {
        assertTrue(readOnly.additions().contains(1));
    }

    @Test
    public void entrySet() {
        assertTrue(!readOnly.entrySet().isEmpty());
    }

    @Test
    public void getOrDefault() {
        assertTrue(readOnly.getOrDefault(1, 100) == 1);
        assertTrue(readOnly.getOrDefault(100, 90) == 90);
    }

    @Test
    public void putIfAbsent() {
        map.putIfAbsent(1, 100, 100);
        assertFalse(map.get(1) == 100);
        map.putIfAbsent(900, 110, 100);
        assertTrue(map.get(900) == 110);
    }

    @Test
    public void remove1() {
        map.remove(8, 10);
        assertTrue(map.containsKey(8));
        map.remove(8, 8);
        assertFalse(map.containsKey(8));
    }

    @Test
    public void replace() {
        map.replace(7, 10, 99);
        assertTrue(map.get(7) == 7);
        map.replace(7, 7, 99);
        assertTrue(map.get(7) == 99);
    }

    @Test
    public void replace1() {
        map.replace(6, 6, 99, 24);
        assertTrue(map.get(6) == 99 && map.getAddition(6) == 24);
    }

    @Test
    public void computeIfAbsent() {
        assertFalse(map.containsKey(999));
        map.computeIfAbsent(5, integer -> 100, integer -> 100);
        assertTrue(map.get(5) == 5);
        map.computeIfAbsent(999, integer -> 100, integer -> 100);
        assertTrue(map.get(999) == 100 && map.getAddition(999) == 100);
    }

    @Test
    public void computeIfPresent() {
        map.computeIfPresent(700, (integer, integer2, integer3) -> 100, (integer, integer2, integer3) -> 100);
        assertFalse(map.containsKey(700));
        map.computeIfPresent(11, (integer, integer2, integer3) -> 99, (integer, integer2, integer3) -> 99);
        assertTrue(map.get(11) == 99 && map.getAddition(11) == 99);
    }

    @Test
    public void compute() {
        map.compute(601, (integer, integer2, integer3) -> 99, (integer, integer2, integer3) -> 99);
        assertTrue(map.get(601) == 99 && map.getAddition(601) == 99);
        map.compute(12, (integer, integer2, integer3) -> 99, (integer, integer2, integer3) -> 99);
        assertTrue(map.get(12) == 99 && map.getAddition(12) == 99);
    }

    @Test
    public void merge() {
        map.merge(602, 99, 99, (integer, integer2, integer3) -> 110, (integer, integer2, integer3) -> 110);
        assertTrue(map.get(602) == 99 && map.getAddition(602) == 99);
        map.merge(13, 110, 110, (integer, integer2, integer3) -> 99, (integer, integer2, integer3) -> 99);
        assertTrue(map.get(13) == 99 && map.getAddition(13) == 99);
    }

    @Test
    public void forEach() {
        AtomicInteger integer = new AtomicInteger(0);
        readOnly.forEach((integer1, integer2, integer3) -> integer.incrementAndGet());
        assertTrue(integer.get() == 10);
    }

    @Test
    public void replaceAll() {
        replaceAll.replaceAll((integer, integer2, integer3) -> 99, (integer, integer2, integer3) -> 100);
        for(int i = 0; i < 100; i++) {
            assertTrue(replaceAll.get(i) == 99 && replaceAll.getAddition(i) == 100);
        }
    }
}