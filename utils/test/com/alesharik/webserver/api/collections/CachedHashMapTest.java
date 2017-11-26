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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;

public class CachedHashMapTest {
    private CachedHashMap<Integer, Integer> map;

    @Before
    public void setUp() throws Exception {
        map = new CachedHashMap<>();
        map.stop();
    }

    @Test
    public void size() throws Exception {
        assertEquals(0, map.size());
        for(int i = 0; i < 100; i++) {
            map.put(i, i);
            assertEquals(i + 1, map.size());
        }
    }

    @Test
    public void isEmpty() throws Exception {
        assertTrue(map.isEmpty());
        map.put(1, 1);
        assertFalse(map.isEmpty());
        map.remove(1);
        assertTrue(map.isEmpty());
    }

    @Test
    public void containsKey() throws Exception {
        assertFalse(map.containsKey(null));

        assertFalse(map.containsKey(1));

        for(int i = 0; i < 100; i++)
            map.put(i, i);

        for(int i = 0; i < 100; i++)
            assertTrue(map.containsKey(i));
    }

    @Test
    public void containsValue() throws Exception {
        assertFalse(map.containsValue(null));
        assertFalse(map.containsValue(1));

        for(int i = 0; i < 100; i++)
            map.put(i, i);

        for(int i = 0; i < 100; i++)
            assertTrue(map.containsKey(i));

        map.put(999, null);
        assertTrue(map.containsValue(null));
    }

    @Test
    public void get() throws Exception {
        assertNull(map.get(null));

        for(int i = 0; i < 100; i++)
            assertNull(map.get(i));

        for(int i = 0; i < 100; i++)
            assertNull(map.put(i, i));
        for(int i = 0; i < 100; i++)
            assertEquals(i, map.get(i).longValue());
    }

    @Test
    public void period() throws Exception {
        assertEquals(-1, map.getPeriod(1));
        map.put(1, 1);
        assertEquals(CachedHashMap.DEFAULT_LIFE_TIME, map.getPeriod(1));
        assertFalse(map.setPeriod(2, 1));
        assertTrue(map.setPeriod(1, 1));
        assertEquals(1, map.getPeriod(1));
    }

    @Test
    public void liveTime() throws Exception {
        assertEquals(-1, map.getLiveTime(1));
        map.put(1, 1);
        Thread.sleep(10);
        assertEquals(10, map.getLiveTime(1), 2);
        assertFalse(map.resetTime(2));
        assertTrue(map.resetTime(1));
        assertEquals(0, map.getLiveTime(1), 1);
    }

    @Test
    public void put() throws Exception {
        assertNull(map.put(1, 1));
        assertEquals(1, map.put(1, 2).intValue());
    }

    @Test
    public void remove() throws Exception {
        assertNull(map.remove(1));
        map.put(1, 1);
        assertEquals(1, map.remove(1).intValue());
    }

    @Test
    public void putAllFromMap() throws Exception {
        Map<Integer, Integer> mapa = new HashMap<>();
        for(int i = 0; i < 100; i++)
            mapa.put(i, i);

        map.putAll(mapa);
        for(int i = 0; i < 100; i++) {
            assertEquals(i, map.get(i).intValue());
            assertEquals(CachedHashMap.DEFAULT_LIFE_TIME, map.getPeriod(i));
        }
    }

    @Test
    public void putAllFromMapWithPeriod() throws Exception {
        Map<Integer, Integer> mapa = new HashMap<>();
        for(int i = 0; i < 100; i++)
            mapa.put(i, i);

        map.putAll(mapa, 2);
        for(int i = 0; i < 100; i++) {
            assertEquals(i, map.get(i).intValue());
            assertEquals(2, map.getPeriod(i));
        }
    }

    @Test
    public void putAllFromMapWithPeriodSupplier() throws Exception {
        Map<Integer, Integer> mapa = new HashMap<>();
        for(int i = 0; i < 100; i++)
            mapa.put(i, i);

        map.putAll(mapa, (integer, integer2) -> (long) integer);
        for(int i = 0; i < 100; i++) {
            assertEquals(i, map.get(i).intValue());
            assertEquals(i, map.getPeriod(i));
        }
    }

    @Test
    public void putAllFromCachedMap() throws Exception {
        CachedHashMap<Integer, Integer> mapa = new CachedHashMap<>();
        mapa.stop();
        for(int i = 0; i < 100; i++)
            mapa.put(i, i, i);

        map.putAllCached(mapa);
        for(int i = 0; i < 100; i++) {
            assertEquals(i, map.get(i).intValue());
            assertEquals(i, map.getPeriod(i));
        }
    }

    @Test
    public void clear() throws Exception {
        assertTrue(map.isEmpty());

        for(int i = 0; i < 100; i++)
            assertNull(map.put(i, i));
        assertFalse(map.isEmpty());
        map.clear();
        assertTrue(map.isEmpty());
    }

    @Test
    public void keySet() throws Exception {
        Set<Integer> keys = map.keySet();
        assertTrue(keys.isEmpty());
        map.put(1, 1);
        assertFalse(keys.isEmpty());
        assertTrue(keys.contains(1));

        map.put(2, 2);
        map.put(3, 3);

        Object[] arr1 = keys.toArray();
        assertEquals(3, arr1.length);
        for(int i = 0; i < 3; i++)
            assertEquals(i + 1, arr1[i]);

        Integer[] arr2 = keys.toArray(new Integer[0]);
        assertEquals(3, arr2.length);
        for(int i = 0; i < 3; i++)
            assertEquals(i + 1, arr2[i].intValue());

        assertTrue(keys.containsAll(asList(1, 2, 3)));
        assertFalse(keys.containsAll(asList(1, 2, 3, 4)));

        assertTrue(keys.retainAll(asList(2, 3)));

        assertFalse(map.containsKey(1));
        assertTrue(map.containsKey(2));
        assertTrue(map.containsKey(3));

        assertTrue(keys.removeAll(singletonList(2)));
        assertTrue(map.containsKey(3));
        assertFalse(map.containsKey(2));

        keys.clear();
        assertFalse(map.containsKey(3));
        assertTrue(keys.isEmpty());
        assertTrue(map.isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void keySetAdd() throws Exception {
        map.keySet().add(1);
        fail();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void keySetAddAll() throws Exception {
        map.keySet().addAll(asList(1, 2));
        fail();
    }

    @Test
    public void valueCollection() throws Exception {
        Collection<Integer> values = map.values();
        assertTrue(values.isEmpty());
        map.put(1, 1);
        assertFalse(values.isEmpty());
        assertTrue(values.contains(1));

        map.put(2, 2);
        map.put(3, 3);

        Object[] arr1 = values.toArray();
        assertEquals(3, arr1.length);
        for(int i = 0; i < 3; i++)
            assertEquals(i + 1, arr1[i]);

        Integer[] arr2 = values.toArray(new Integer[0]);
        assertEquals(3, arr2.length);
        for(int i = 0; i < 3; i++)
            assertEquals(i + 1, arr2[i].intValue());

        assertTrue(values.containsAll(asList(1, 2, 3)));
        assertFalse(values.containsAll(asList(1, 2, 3, 4)));

        assertTrue(values.retainAll(asList(2, 3)));

        assertFalse(map.containsValue(1));
        assertTrue(map.containsValue(2));
        assertTrue(map.containsValue(3));

        assertTrue(values.removeAll(singletonList(2)));
        assertTrue(map.containsValue(3));
        assertFalse(map.containsValue(2));

        values.clear();
        assertFalse(map.containsValue(3));
        assertTrue(values.isEmpty());
        assertTrue(map.isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void valueCollectionAdd() throws Exception {
        map.values().add(1);
        fail();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void valueCollectionAddAll() throws Exception {
        map.values().addAll(asList(1, 2));
        fail();
    }

    @Test
    public void entrySet() throws Exception {
        Set<Map.Entry<Integer, Integer>> values = map.entrySet();
        assertTrue(values.isEmpty());
        map.put(1, 1);
        assertFalse(values.isEmpty());
        assertTrue(values.contains(of(1, 1)));

        map.put(2, 2);
        map.put(3, 3);

        Object[] arr1 = values.toArray();
        assertEquals(3, arr1.length);
        for(int i = 0; i < 3; i++)
            assertEquals(of(i + 1, i + 1), arr1[i]);

        Map.Entry<Integer, Integer>[] arr2 = values.toArray(new Map.Entry[0]);
        assertEquals(3, arr2.length);
        for(int i = 0; i < 3; i++)
            assertEquals(of(i + 1, i + 1), arr2[i]);

        assertTrue(values.containsAll(asList(of(1, 1), of(2, 2), of(3, 3))));
        assertFalse(values.containsAll(asList(of(1, 1), of(2, 2), of(3, 3), of(4, 4))));

        assertTrue(values.retainAll(asList(of(2, 2), of(3, 3))));

        assertFalse(map.containsValue(1));
        assertTrue(map.containsValue(2));
        assertTrue(map.containsValue(3));

        assertTrue(values.removeAll(singletonList(of(2, 2))));
        assertTrue(map.containsValue(3));
        assertFalse(map.containsValue(2));

        values.clear();
        assertFalse(map.containsValue(3));
        assertTrue(values.isEmpty());
        assertTrue(map.isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void entrySetAdd() throws Exception {
        map.entrySet().add(of(1, 2));
        fail();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void entrySetAddAll() throws Exception {
        map.entrySet().addAll(asList(of(1, 1), of(2, 2)));
        fail();
    }

    @Test
    public void timeDelete() throws Exception {
        map.start();
        map.put(1, 1, 5);
        assertTrue(map.containsKey(1));
        Thread.sleep(5);
        map.update();
        assertFalse(map.containsKey(1));
    }

    private static <K, V> Map.Entry<K, V> of(K key, V value) {
        return new EntryImpl<>(key, value);
    }

    @Test
    public void forEach() throws Exception {
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            map.put(i, i);
            list.add(i);
        }
        map.forEach((integer, integer2) -> list.remove(integer));
        assertTrue(list.isEmpty());
    }

    @Test
    public void forEachWithTime() throws Exception {
        List<Long> list = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            map.put(i, i, i);
            list.add((long) i);
        }
        map.forEach((integer, integer2, time) -> list.remove(time));
        assertTrue(list.isEmpty());
    }

    @Test
    public void replaceAll() throws Exception {
        for(int i = 0; i < 100; i++)
            map.put(i, i);

        map.replaceAll((integer, integer2) -> integer2 + 100);
        for(int i = 0; i < 100; i++)
            assertEquals(i + 100, map.get(i).intValue());
    }

    @Test
    public void replaceAllWithTime() throws Exception {
        for(int i = 0; i < 100; i++)
            map.put(i, i, i);

        map.replaceAll((integer, integer2) -> integer2 + 100, (integer, integer2, aLong) -> aLong + 100);
        for(int i = 0; i < 100; i++) {
            assertEquals(i + 100, map.get(i).intValue());
            assertEquals(i + 100, map.getPeriod(i));
        }
    }

    @Test
    public void putIfAbsent() throws Exception {
        //noinspection UnnecessaryBoxing
        Integer integer = new Integer(234);
        assertNull(map.putIfAbsent(1, integer));
        assertSame(integer, map.putIfAbsent(1, integer));
    }

    @Test
    public void putIfAbsentWithTime() throws Exception {
        //noinspection UnnecessaryBoxing
        Integer integer = new Integer(234);
        assertNull(map.putIfAbsent(1, integer, 1));
        assertSame(integer, map.putIfAbsent(1, integer));
        assertEquals(1, map.getPeriod(1));
    }

    @Test
    public void removeKV() throws Exception {
        assertTrue(map.isEmpty());
        assertFalse(map.remove(1, 1));
        map.put(1, 2);
        assertFalse(map.remove(1, 1));
        assertTrue(map.remove(1, 2));
        assertTrue(map.isEmpty());
    }

    @Test
    public void replaceWithCheck() throws Exception {
        assertTrue(map.isEmpty());
        assertFalse(map.replace(1, 1, 2));
        map.put(1, 2);
        assertFalse(map.replace(1, 1, 2));
        assertTrue(map.replace(1, 2, 10));

        assertEquals(10, map.get(1).intValue());
    }

    @Test
    public void replace() throws Exception {
        assertTrue(map.isEmpty());
        assertNull(map.replace(1, 10));
        map.put(1, 1);
        assertEquals(1, map.replace(1, 10).intValue());
        assertEquals(10, map.get(1).intValue());
    }

    @Test
    public void computeIfAbsent() throws Exception {
        map.put(1, 1);
        assertEquals(1, map.computeIfAbsent(1, integer -> 10).intValue());
        map.remove(1);
        assertEquals(10, map.computeIfAbsent(1, integer -> 10).intValue());
    }

    @Test
    public void computeIfAbsentWithTime() throws Exception {
        map.put(1, 1);
        assertEquals(1, map.computeIfAbsent(1, integer -> 10, (integer, integer2) -> 10L).intValue());
        assertEquals(CachedHashMap.DEFAULT_LIFE_TIME, map.getPeriod(1));
        map.remove(1);
        assertEquals(10, map.computeIfAbsent(1, integer -> 10, (integer, integer2) -> 100L).intValue());
        assertEquals(100, map.getPeriod(1));
    }

    @Test
    public void computeIfPresent() throws Exception {
        assertNull(map.computeIfPresent(1, (integer, integer2) -> 10));
        map.put(1, 1);
        assertEquals(10, map.computeIfPresent(1, (integer, integer2) -> 10).intValue());
        assertEquals(10, map.get(1).intValue());
    }

    @Test
    public void computeIfPresentWithTime() throws Exception {
        assertNull(map.computeIfPresent(1, (integer, integer2) -> 10, (integer, integer2, aLong) -> 10L));
        map.put(1, 1);
        assertEquals(10, map.computeIfPresent(1, (integer, integer2) -> 10, (integer, integer2, aLong) -> 10L).intValue());
        assertEquals(10, map.get(1).intValue());
        assertEquals(10, map.getPeriod(1));
    }

    @Test
    public void compute() throws Exception {
        assertTrue(map.isEmpty());
        assertEquals(10, map.compute(1, (integer, integer2) -> 10).intValue());
        assertEquals(10, map.get(1).intValue());

        assertEquals(20, map.compute(1, (integer, integer2) -> 20).intValue());
        assertEquals(20, map.get(1).intValue());
    }

    @Test
    public void computeWithTime() throws Exception {
        assertTrue(map.isEmpty());
        assertEquals(10, map.compute(1, (integer, integer2) -> 10, (integer, integer2, aLong) -> 100L).intValue());
        assertEquals(10, map.get(1).intValue());
        assertEquals(100, map.getPeriod(1));

        assertEquals(20, map.compute(1, (integer, integer2) -> 20, (integer, integer2, aLong) -> 200L).intValue());
        assertEquals(20, map.get(1).intValue());
        assertEquals(200, map.getPeriod(1));
    }

    @Test
    public void merge() throws Exception {
        assertTrue(map.isEmpty());
        assertEquals(10, map.merge(1, 10, (integer, integer2) -> 20).intValue());
        assertEquals(10, map.get(1).intValue());

        assertEquals(20, map.merge(1, 10, (integer, integer2) -> 20).intValue());
        assertEquals(20, map.get(1).intValue());
    }

    @Test
    public void mergeWithTime() throws Exception {
        assertTrue(map.isEmpty());
        assertEquals(10, map.merge(1, 10, 100L, (integer, integer2) -> 20).intValue());
        assertEquals(10, map.get(1).intValue());
        assertEquals(100, map.getPeriod(1));

        assertEquals(20, map.merge(1, 10, 200, (integer, integer2) -> 20).intValue());
        assertEquals(20, map.get(1).intValue());
        assertEquals(200, map.getPeriod(1));
    }

    @Test
    public void cloneTest() throws Exception {
        for(int i = 0; i < 100; i++)
            map.put(i, i);

        CachedHashMap<Integer, Integer> mapa = map.clone();
        for(int i = 0; i < 100; i++)
            assertEquals(map.get(i), mapa.get(i));

        mapa.clear();
        assertFalse(map.isEmpty());
        assertTrue(mapa.isEmpty());
        for(int i = 0; i < 100; i++)
            assertEquals(i, map.get(i).intValue());
    }

    @AllArgsConstructor
    @Getter
    @ToString
    private static final class EntryImpl<K, V> implements Map.Entry<K, V> {
        private final K key;
        private volatile V value;

        @Override
        public V setValue(V value) {
            V v = this.value;
            this.value = value;
            return v;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(!(o instanceof Map.Entry)) return false;

            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;

            return (getKey() != null ? getKey().equals(entry.getKey()) : entry.getKey() == null) && (getValue() != null ? getValue().equals(entry.getValue()) : entry.getValue() == null);
        }

        @Override
        public int hashCode() {
            int result = getKey() != null ? getKey().hashCode() : 0;
            result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
            return result;
        }
    }
}