package com.alesharik.webserver.api.collections;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class CachedArrayListTest {
    private static final int DEFAULT_LIFE_TIME = 60 * 1000;

    private CachedArrayList<String> list;

    @Before
    public void setUp() throws Exception {
        list = new CachedArrayList<>();
        list.stop();
    }

    @Test
    public void add() throws Exception {
        list.add("test");
        assertTrue(list.contains("test"));
        assertEquals(list.getTime(0).longValue(), DEFAULT_LIFE_TIME);
    }

    @Test
    public void addWithTime() throws Exception {
        list.add("test", 100);
        assertTrue(list.contains("test"));
        assertEquals(list.getTime(0).longValue(), 100);
    }

    @Test
    public void addWithIndex() throws Exception {
        list.add("sdfdsf");
        list.add("dsfasdsa");
        list.add("dsfsad");
        list.add("gfdhgfs");

        list.add(2, "test");
        assertEquals(list.get(2), "test");
        assertEquals(list.getTime(2).longValue(), DEFAULT_LIFE_TIME);
    }

    @Test
    public void addWithIndexAndTime() throws Exception {
        list.add("sdfdsf");
        list.add("dsfasdsa");
        list.add("dsfsad");
        list.add("gfdhgfs");

        list.add(2, "test", 100);
        assertEquals(list.get(2), "test");
        assertEquals(list.getTime(2).longValue(), 100);
    }

    @Test
    public void set() throws Exception {
        list.add("test");

        list.set(0, "none");
        assertTrue(list.contains("none"));
        assertFalse(list.contains("test"));
        assertEquals(list.getTime(0).longValue(), DEFAULT_LIFE_TIME);
    }

    @Test
    public void setWithTime() throws Exception {
        list.add("test");

        list.set(0, "none", 100);
        assertTrue(list.contains("none"));
        assertFalse(list.contains("test"));
        assertEquals(list.getTime(0).longValue(), 100);
    }

    @Test
    public void remove() throws Exception {
        list.add("test");
        assertTrue(list.contains("test"));
        assertTrue(list.remove("test"));
        assertFalse(list.contains("test"));
    }

    @Test
    public void removeIndex() throws Exception {
        list.add("test");
        assertTrue(list.contains("test"));
        assertEquals(list.remove(0), "test");
        assertFalse(list.contains("test"));
    }

    @Test
    public void indexOf() throws Exception {
        for(int i = 0; i < 10; i++) {
            for(int j = 0; j < 100; j++) {
                list.add("i = " + j);
            }
        }

        for(int i = 0; i < 100; i++) {
            assertEquals(list.indexOf("i = " + i), i);
        }
        assertEquals(list.indexOf("asd"), -1);
    }

    @Test
    public void lastIndexOf() throws Exception {
        for(int i = 0; i < 10; i++) {
            for(int j = 0; j < 100; j++) {
                list.add("i = " + j);
            }
        }

        for(int i = 0; i < 100; i++) {
            assertEquals(list.lastIndexOf("i = " + i), i + 9 * 100);
        }
        assertEquals(list.indexOf("asd"), -1);
    }

    @Test
    public void clear() throws Exception {
        for(int i = 0; i < 100; i++) {
            list.add("i = " + i);
        }
        assertEquals(list.size(), 100);
        list.clear();
        assertEquals(list.size(), 0);
    }

    @Test
    public void addAll() throws Exception {
        Collection<String> l = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            l.add(String.valueOf(i));
        }
        list.addAll(l);

        for(int i = 0; i < 100; i++) {
            assertTrue(l.contains(String.valueOf(i)));
            assertEquals(list.getTime(i).longValue(), DEFAULT_LIFE_TIME);
        }
    }

    @Test
    public void addAllWithTime() throws Exception {
        Collection<String> l = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            l.add(String.valueOf(i));
        }
        list.addAll(l, 100);

        for(int i = 0; i < 100; i++) {
            assertTrue(l.contains(String.valueOf(i)));
            assertEquals(list.getTime(i).longValue(), 100);
        }
    }


    @Test
    public void addAllWithIndex() throws Exception {
//        list.add("sdfdsf");
//        list.add("dsfasdsa");
//        list.add("dsfsad");
//        list.add("gfdhgfs");
//
//        Collection<String> l = new ArrayList<>();
//        for(int i = 0; i < 100; i++) {
//            l.add(String.valueOf(i));
//        }
//        list.addAll(2, l);
//
//        for(int i = 2; i < 102; i++) {
////            assertTrue(l.contains(String.valueOf(i)));
////            assertEquals(list.getTime(i).longValue(), DEFAULT_LIFE_TIME);
//        }
    }

    @Test
    public void addAllWithIndexAndTime() throws Exception {
//        list.add("sdfdsf");
//        list.add("dsfasdsa");
//        list.add("dsfsad");
//        list.add("gfdhgfs");
//
//        Collection<String> l = new ArrayList<>();
//        for(int i = 0; i < 100; i++) {
//            l.add(String.valueOf(i));
//        }
//        list.addAll(2, l, 100);
//
//        for(int i = 2; i < 102; i++) {
////            assertTrue(l.contains(String.valueOf(i)));
////            assertEquals(list.getTime(i).longValue(), 100);
//        }
    }

    @Test
    public void iterator() throws Exception {
        for(int i = 0; i < 100; i++) {
            list.add("i = " + i);
        }

        Iterator<String> iterator = list.iterator();
        int counter = 0;
        while(iterator.hasNext()) {
            assertEquals(iterator.next(), "i = " + counter);
            counter++;
        }
    }

    @Test
    public void listIterator() throws Exception {
        for(int i = 0; i < 100; i++) {
            list.add("i = " + i);
        }

        ListIterator<String> listIterator = list.listIterator();
        int counter = 0;
        while(listIterator.hasNext()) {
            assertEquals(listIterator.next(), "i = " + counter);
            counter++;
        }
    }

    @Test
    public void listIteratorWithIndex() throws Exception {
        for(int i = 0; i < 100; i++) {
            list.add("i = " + i);
        }

        ListIterator<String> listIterator = list.listIterator(10);
        int counter = 10;
        while(listIterator.hasNext()) {
            assertEquals(listIterator.next(), "i = " + counter);
            counter++;
        }
    }

    @Test
    public void timeMap() throws Exception {
//        for(int i = 0; i < 100; i++) {
//            list.add("i = " + i);
//        }
//
//        Map<Long, String> timeMap = list.timeMap();
//        int counter = 0;
//        for(Map.Entry<Long, String> entry : timeMap.entrySet()) {
//            assertEquals(entry.getKey().longValue(), DEFAULT_LIFE_TIME);
//            assertEquals(entry.getValue(), "i = " + counter);
//            counter++;
//        }
    }

    @Test
    public void subList() throws Exception {
        for(int i = 0; i < 100; i++) {
            list.add("i = " + i);
        }

        List<String> l = list.subList(10, 30);
        for(int i = 10, counter = 0; i < 30; i++, counter++) {
            assertEquals("i = " + i, l.get(counter));
        }
    }

    @Test
    public void isEmpty() throws Exception {
        assertTrue(list.isEmpty());
        list.add("asd");
        assertFalse(list.isEmpty());
    }

    @Test
    public void contains() throws Exception {
        list.add("test");
        assertFalse(list.contains("df"));
        assertTrue(list.contains("test"));
    }

    @Test
    public void containsAll() throws Exception {
        List<String> l = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            l.add(String.valueOf(i));
        }
        list.addAll(l);
        assertTrue(list.containsAll(l));
    }

    @Test
    public void toArray() throws Exception {
        for(int i = 0; i < 100; i++) {
            list.add("i = " + i);
        }

        Object[] arr = list.toArray();
        for(int i = 0; i < arr.length; i++) {
            assertEquals(arr[i], list.get(i));
        }
    }

    @Test
    public void toNormalArray() throws Exception {
        for(int i = 0; i < 100; i++) {
            list.add("i = " + i);
        }

        String[] arr = list.toArray(new String[0]);
        for(int i = 0; i < arr.length; i++) {
            assertEquals(arr[i], list.get(i));
        }
    }

    @Test
    public void removeAll() throws Exception {
        Collection<String> l = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            l.add(String.valueOf(i));
        }
        list.addAll(l);
        assertFalse(list.isEmpty());
        list.removeAll(l);
        assertTrue(list.isEmpty());
    }

    @Test
    public void retainAll() throws Exception {
        Collection<String> l = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            l.add(String.valueOf(i));
        }
        list.addAll(l);
        list.add("sdf");
        list.retainAll(l);
        assertFalse(list.contains("sdf"));
    }

    @Test
    public void retainAllWithTimeUpdate() throws Exception {
        Collection<String> l = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            l.add(String.valueOf(i));
        }
        list.addAll(l);
        list.retainAll(l, 100);
        assertFalse(list.contains("sdf"));
        for(int i = 0; i < 100; i++) {
            assertEquals(list.getTime(i).longValue(), 100);
        }
    }

    @Test
    public void replaceAll() throws Exception {
        Collection<String> l = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            l.add(String.valueOf(i));
        }
        list.addAll(l);
        list.replaceAll(s -> s.concat("asd"));

        for(int i = 0; i < 100; i++) {
            assertEquals(list.get(i), i + "asd");
        }
    }

    @Test
    public void replaceAllWithTime() throws Exception {
        Collection<String> l = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            l.add(String.valueOf(i));
        }
        list.addAll(l);
        list.replaceAll(s -> s.concat("asd"), aLong -> 100L);

        for(int i = 0; i < 100; i++) {
            assertEquals(list.get(i), i + "asd");
            assertEquals(list.getTime(i).longValue(), 100);
        }
    }

    @Test
    public void sort() throws Exception {
//        list.sort(String::compareTo);
    }

    @Test
    public void spliterator() throws Exception {
        Collection<String> l = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            l.add(String.valueOf(i));
        }
        list.addAll(l);
        Spliterator<String> spliterator = list.spliterator();
        for(int i = 0; i < 100; i++) {
            AtomicBoolean ok = new AtomicBoolean(false);
            final int val = i;
            spliterator.tryAdvance(s -> ok.set(s.equals(String.valueOf(val))));
            assertTrue(ok.get());
        }
    }

    @Test
    public void removeIf() throws Exception {
        Collection<String> l = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            l.add(String.valueOf(i));
        }
        list.addAll(l);
        list.removeIf(s -> Integer.valueOf(s) % 2 == 0);
        for(String s : list) {
            assertFalse(Integer.valueOf(s) % 2 == 0);
        }
    }

    @Test
    public void stream() throws Exception {
        Collection<String> l = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            l.add(String.valueOf(i));
        }
        list.addAll(l);
        Stream<String> stream = list.stream();
        List<String> l1 = stream.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        assertTrue(l1.containsAll(list));
    }

    @Test
    public void parallelStream() throws Exception {
        Collection<String> l = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            l.add(String.valueOf(i));
        }
        list.addAll(l);
        Stream<String> stream = list.parallelStream();
        List<String> l1 = stream.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        assertTrue(l1.containsAll(list));
    }

    @Test
    public void forEach() throws Exception {
        Collection<String> l = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            l.add(String.valueOf(i));
        }
        list.addAll(l);

        AtomicInteger counter = new AtomicInteger();
        list.forEach(s -> assertEquals(String.valueOf(counter.getAndIncrement()), s));
    }

    @Test
    public void forEach1() throws Exception {
        Collection<String> l = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            l.add(String.valueOf(i));
        }
        list.addAll(l, 100);

        AtomicInteger counter = new AtomicInteger();
        list.forEach((s, time) -> {
            assertEquals(String.valueOf(counter.getAndIncrement()), s);
            assertEquals(time.longValue(), 100);
        });
    }

    @Test
    public void get() throws Exception {
        list.add("asd");
        assertEquals(list.get(0), "asd");
    }

    @Test
    public void getTime() throws Exception {
        list.add("asd", 100);
        assertEquals(list.getTime(0).longValue(), 100);
    }

    @Test
    public void size() throws Exception {
        assertEquals(list.size(), 0);
        list.add("asd", 100);
        assertEquals(list.size(), 1);
    }

    @Test
    public void equals() throws Exception {
        list.add("asd");
        CachedArrayList<String> l = new CachedArrayList<>();
        l.add("asd");
        assertTrue(list.equals(l));
    }


}