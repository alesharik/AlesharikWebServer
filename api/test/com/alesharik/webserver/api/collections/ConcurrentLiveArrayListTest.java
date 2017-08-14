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

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class ConcurrentLiveArrayListTest {
    private static ConcurrentLiveArrayList<Integer> wr;
    private static ConcurrentLiveArrayList<Integer> wrDouble;
    private static ConcurrentLiveArrayList<Integer> forSort;
    private static ConcurrentLiveArrayList<Integer> readOnly;
    private static ConcurrentLiveArrayList<Integer> sameAsReadOnly;
    private static ConcurrentLiveArrayList<Integer> clear;
    private static ConcurrentLiveArrayList<Integer> empty;

    @BeforeClass
    public static void setUp() throws Exception {
        wr = new ConcurrentLiveArrayList<>(2000);
        readOnly = new ConcurrentLiveArrayList<>(2000);
        readOnly.stop();
        sameAsReadOnly = new ConcurrentLiveArrayList<>(2000);
        sameAsReadOnly.stop();
        for(int i = 0; i <= 100; i++) {
            wr.add(i);
            readOnly.add(i);
            sameAsReadOnly.add(i);
        }
        for(int i = 101; i <= 1000; i++) {
            wr.add(0);
        }
        for(int i = 300; i < 350; i++) {
            wr.set(i, i);
        }
        wr.add(306);
        clear = new ConcurrentLiveArrayList<>(2000);
        for(int i = 0; i < 1000; i++) {
            clear.add(i);
        }
        empty = new ConcurrentLiveArrayList<>(2000);

        forSort = new ConcurrentLiveArrayList<>(2000);
        forSort.stop();
        for(int i = 0; i < 100; i++) {
            forSort.add(99 - i);
        }

        wrDouble = new ConcurrentLiveArrayList<>();
        wrDouble.addAll(wr);
    }

    @Test
    public void constructorTest() throws Exception { //Test constructor works
        ConcurrentLiveArrayList<String> list = new ConcurrentLiveArrayList<>(1L);
        list.add("sdf");
        assertTrue(list.contains("sdf"));
    }

    @Test
    public void add() throws Exception {
        assertTrue(wr.add(1000));
    }

    @Test
    public void addWithLifeTime() throws Exception {
        assertTrue(wr.add(1001, 10L));
    }

    @Test
    public void addWithPosition() throws Exception {
        wr.add(401, Integer.valueOf(1002));
    }

    @Test
    public void addWithPositionAndLifeTime() throws Exception {
        wr.add(400, 1003, 10L);
    }

    @Test
    public void set() throws Exception {
        assertTrue(Integer.compare(wr.set(300, 1004), 300) == 0);
    }

    @Test
    public void setWithLifeTime() throws Exception {
        assertTrue(Integer.compare(wr.set(301, 1004, 10L), 301) == 0);
    }

    @Test
    public void remove() throws Exception {
        assertTrue(wr.remove(302) != null);
    }

    @Test
    public void indexOf() throws Exception {
        assertTrue(Integer.compare(readOnly.indexOf(20), 19) == 0);
    }

    @Test
    public void lastIndexOf() throws Exception {
        assertTrue(Integer.compare(readOnly.lastIndexOf(90), 89) == 0);
    }

    @Test
    public void clear() throws Exception {
        clear.clear();
        assertTrue(clear.isEmpty());
    }

    @Test
    public void addAll() throws Exception {
        assertTrue(wr.addAll(Arrays.asList(600, 601, 602)));
    }

    @Test
    public void addAllWithLifeTime() throws Exception {
        assertTrue(wr.addAll(Arrays.asList(600, 601, 602), 10L));
    }

    @Test
    public void addAllWithIndex() throws Exception {
        assertTrue(wr.addAll(0, Arrays.asList(600, 601, 602)));
    }

    @Test
    public void addAllWithIndexAndLifeTime() throws Exception {
        assertTrue(wr.addAll(1, Arrays.asList(600, 601, 602), 10L));
    }

    @Test
    public void iterator() throws Exception {
        assertNotNull(readOnly.iterator());
    }

    @Test
    public void listIterator() throws Exception {
        assertNotNull(readOnly.listIterator());
    }

    @Test
    public void listIteratorWithIndex() throws Exception {
        assertNotNull(readOnly.listIterator(10));
    }

    @Test
    public void timeMap() throws Exception {
        assertNotNull(readOnly.timeMap());
    }

    @Test
    public void subList() throws Exception {
        assertNotNull(readOnly.subList(1, 6));
    }

    @Test
    public void isEmpty() throws Exception {
        assertTrue(empty.isEmpty());
        assertFalse(readOnly.isEmpty());
    }

    @Test
    public void contains() throws Exception {
        assertTrue(readOnly.contains(2));
        assertFalse(readOnly.contains(567));
    }

    @Test
    public void toArray() throws Exception {
        assertNotNull(readOnly.toArray());
    }

    @Test
    public void toArray1() throws Exception {
        assertNotNull(readOnly.toArray(new Object[0]));
    }

    @Test
    public void containsAll() throws Exception {
        assertTrue(readOnly.containsAll(Arrays.asList(1, 2, 3)));
    }

    @Test
    public void removeAll() throws Exception {
        assertTrue(wr.removeAll(Arrays.asList(305, 306, 307)));
        assertFalse(wr.removeAll(Arrays.asList(5412, 45123)));
    }

    @Test
    public void retainAll() throws Exception {
        List<Integer> c = new ArrayList<>();
        c.addAll(wrDouble);
        assertTrue(wrDouble.retainAll(c));
        assertFalse(wrDouble.contains(310));
    }

    @Test
    public void retainAll1() throws Exception {
        List<Integer> c = new ArrayList<>();
        c.addAll(wr);
        c.remove(Integer.valueOf(310));
        assertTrue(wrDouble.retainAll(c, 60_000L));
        assertFalse(wrDouble.contains(310));
    }

    @Test
    public void toStringTest() throws Exception {
        assertNotNull(readOnly.toString());
    }

    @Test
    public void replaceAll() throws Exception {
        wr.replaceAll(integer -> {
            if(integer.compareTo(311) == 0) {
                return 5600;
            } else {
                return integer;
            }
        });
        assertTrue(wr.contains(5600));
    }

    @Test
    public void replaceAll1() throws Exception {
        wrDouble.replaceAll(integer -> {
            if(integer.compareTo(311) == 0) {
                return 3200;
            } else {
                return integer;
            }
        }, aLong -> aLong + 1);
        assertTrue(wrDouble.contains(3200));
    }

    @Test(expected = UnsupportedOperationException.class) //TODO write this
    public void sort() throws Exception {
        forSort.sort(Integer::compareTo);
    }

    @Test
    public void spliterator() throws Exception {
        assertNotNull(readOnly.spliterator());
    }

    @Test
    public void removeIf() throws Exception {
        assertTrue(wr.removeIf(integer -> integer.compareTo(320) == 0));
        assertFalse(wr.contains(320));
        wr.add(320);
        assertFalse(wr.removeIf(integer -> {
            boolean ok = integer.compareTo(320) == 0;
            if(ok) {
                wr.remove(integer);
            }
            return ok;
        }));
    }

    @Test
    public void stream() throws Exception {
        assertNotNull(readOnly.stream());
    }

    @Test
    public void parallelStream() throws Exception {
        assertNotNull(readOnly.parallelStream());
    }

    @Test
    public void forEach() throws Exception {
        AtomicBoolean isOk = new AtomicBoolean(false);
        readOnly.forEach(integer -> isOk.set(integer >= 0));
        assertTrue(isOk.get());
    }

    @Test
    public void forEach1() throws Exception {
        AtomicBoolean isOk = new AtomicBoolean(false);
        readOnly.forEach((integer, aLong) -> isOk.set(integer >= 0 && aLong >= 0));
        assertTrue(isOk.get());
    }

    @Test
    public void cloneTest() throws Exception {
        ConcurrentLiveArrayList<Integer> copy = (ConcurrentLiveArrayList<Integer>) sameAsReadOnly.clone();
        assertTrue(copy.equals(sameAsReadOnly));
    }

    @Test
    public void get() throws Exception {
        assertTrue(readOnly.get(2) == 3);
    }

    @Test
    public void getTime() throws Exception {
        assertTrue(readOnly.getTime(2) > 0);
    }

    @Test
    public void size() throws Exception {
        assertTrue(readOnly.size() > 0);
        assertTrue(empty.size() == 0);
    }

    @Test
    public void equals() throws Exception {
        assertTrue(readOnly.equals(sameAsReadOnly));
        assertFalse(readOnly.equals(wr));
    }

    @Test
    public void start() throws Exception {
        wr.start();
    }

    @Test
    public void stop() throws Exception {
        wr.stop();
    }

    @Test
    public void tick() throws Exception {
        wr.tick();
    }

    @Test
    public void hashCodeTest() throws Exception {
        assertTrue(Integer.compare(readOnly.hashCode(), sameAsReadOnly.hashCode()) == 0);
        assertFalse(Integer.compare(readOnly.hashCode(), wr.hashCode()) == 0);
    }

    @Test
    public void testArrayWorks() throws Exception {
        ConcurrentLiveArrayList<Integer> list = new ConcurrentLiveArrayList<>(1, 10, false);
        for(int i = 0; i < 100; i++) {
            list.add(i, (long) i);
        }
        list.start();
        Thread.sleep(500);
        assertTrue(list.isEmpty());
    }
}