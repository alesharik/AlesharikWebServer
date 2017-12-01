package com.alesharik.webserver.api.collections;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class CachedArrayListTest {

    private final CachedArrayList<String> list = new CachedArrayList<>();

    @Test
    public void testAddNull() {
        assertFalse(list.add(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addNegativeLifeTime() {
        list.add("dsa", -1);
        fail();
    }

    @Test
    public void addElement() {
        assertTrue(list.add("asd", 100));
        assertTrue(list.contains("asd"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testCapacityCheckWithNegativeValue() {
        list.checkCapacityAdd(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testNegativeCapacity() {
        list.checkCapacity(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testBigCapacity() {
        list.checkCapacity(1000);
    }

    @Test
    public void insertElement() {
        list.add("asd");
        list.add("sdf");
        list.add(1, "dfg");

        assertEquals("asd", list.get(0));
        assertEquals("dfg", list.get(1));
        assertEquals("sdf", list.get(2));
    }

    @Test
    public void setElement() {
        list.add("asd");
        list.add("sdf");
        list.add("dfg");

        assertEquals("asd", list.get(0));
        assertEquals("sdf", list.get(1));
        assertEquals("dfg", list.get(2));

        list.set(1, "qwerty");

        assertEquals("asd", list.get(0));
        assertEquals("qwerty", list.get(1));
        assertEquals("dfg", list.get(2));
    }

    @Test
    public void setPeriod() {
        list.add("asd");
        list.add("sdf");
        list.add("dfg");

        long p1 = list.getLastTime(1) + list.getLiveTime(1);
        assertEquals(CachedArrayList.DEFAULT_LIFE_TIME_PERIOD, p1);

        list.setPeriod(1, 1234);

        long p2 = list.getLastTime(1) + list.getLiveTime(1);
        assertEquals(1234, p2);
    }

    @Test
    public void setObjectPeriod() {
        list.add("asd");
        list.add("sdf");
        list.add("dfg");

        long p1 = list.getLastTime("sdf") + list.getLiveTime("sdf");
        assertEquals(CachedArrayList.DEFAULT_LIFE_TIME_PERIOD, p1);

        assertTrue(list.setPeriod("sdf", 1234));

        long p2 = list.getLastTime("sdf") + list.getLiveTime("sdf");
        assertEquals(1234, p2);
    }

    @Test
    public void setPeriodToNotExistingObject() {
        list.add("as");
        list.add("asd");

        assertFalse(list.setPeriod("asdsadasdsad", 12));
        assertEquals(-1, list.getLastTime("asdsadasdsad"));
        assertEquals(-1, list.getLiveTime("asdsadasdsad"));
    }

    @Test
    public void resetObjectTime() throws Exception {
        list.add("as");
        list.add("asd");
        list.start();

        Thread.sleep(5);
        assertEquals(5, list.getLiveTime("asd"), 1);
        list.resetTime("asd");

        assertEquals(0, list.getLiveTime("asd"));
    }

    @Test
    public void resetTime() throws Exception {
        list.add("as");
        list.add("asd");
        list.start();

        Thread.sleep(5);
        assertEquals(5, list.getLiveTime(1), 1);
        list.resetTime(1);

        assertEquals(0, list.getLiveTime(1), 1);
    }

    @Test
    public void remove() {
        list.add("as");
        list.add("dasfsda");
        list.add("asdsdaasdasd");

        list.remove(1);

        assertFalse(list.contains("dasfsda"));
        assertTrue(list.contains("as"));
        assertTrue(list.contains("asdsdaasdasd"));
    }

    @Test
    public void indexOf() {
        list.add("as");
        list.add("dasfsda");
        list.add("asdsdaasdasd");
        list.add("asdsdaasdasd");
        list.add("asdsdaasdasd");
        list.add("asdsdaasdasd");
        list.add("asdsdaasdasd");

        assertEquals(2, list.indexOf("asdsdaasdasd"));
    }

    @Test
    public void lastIndexOf() {
        list.add("as");
        list.add("dasfsda");
        list.add("asdsdaasdasd");
        list.add("asdsdaasdasd");
        list.add("asdsdaasdasd");
        list.add("asdsdaasdasd");
        list.add("asdsdaasdasd");

        assertEquals(6, list.lastIndexOf("asdsdaasdasd"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void clear() {
        assertTrue(list.isEmpty());

        list.add("as");
        list.add("dasfsda");
        list.add("asdsdaasdasd");
        list.add("asdsdaasdasd");
        list.add("asdsdaasdasd");
        list.add("asdsdaasdasd");
        list.add("asdsdaasdasd");

        assertFalse(list.isEmpty());

        list.clear();

        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        //noinspection ResultOfMethodCallIgnored
        list.get(0);
        fail();
    }

    @Test
    public void addAllWithIndex() {
        List<String> a = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            a.add("a" + i);
        }

        List<String> b = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            b.add("b" + i);
        }

        list.addAll(a);
        list.addAll(50, b);
        for(int i = 0; i < 50; i++) {
            assertEquals(a.get(i), list.get(i));
        }

        for(int i = 0; i < 100; i++) {
            assertEquals(b.get(i), list.get(i + 50));
        }
    }

    @Test
    public void iterator() {
        List<String> a = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            a.add("a" + ((i == 0) ? "c" : i));
        }

        list.addAll(a);

        Iterator<String> iterator = list.iterator();
        //noinspection Java8CollectionRemoveIf
        while(iterator.hasNext()) {
            String next = iterator.next();
            if(!next.contains("c"))
                iterator.remove();
        }

        assertEquals(1, list.size());
        assertTrue(list.contains("ac"));
        for(int i = 1; i < 100; i++) {
            assertFalse(list.contains("a" + i));
        }
    }

    @Test
    public void listIterator() {
        List<String> a = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            a.add("a" + ((i == 0) ? "c" : i));
        }

        list.addAll(a);

        Iterator<String> iterator = list.listIterator();
        //noinspection Java8CollectionRemoveIf,WhileLoopReplaceableByForEach
        while(iterator.hasNext()) {
            String next = iterator.next();
            if(!next.contains("c"))
                list.remove(next);
        }

        assertEquals(1, list.size());
        assertTrue(list.contains("ac"));
        for(int i = 1; i < 100; i++) {
            assertFalse(list.contains("a" + i));
        }
    }

    @Test
    public void listIteratorWithIndex() {
        List<String> a = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            a.add("a" + ((i < 5) ? "c" + i : i));
        }

        list.addAll(a);

        Iterator<String> iterator = list.listIterator(5);
        //noinspection Java8CollectionRemoveIf
        while(iterator.hasNext()) {
            String next = iterator.next();
            if(!next.contains("c"))
                list.remove(next);
        }

        assertEquals(5, list.size());
        assertTrue(list.contains("ac0"));
        assertTrue(list.contains("ac1"));
        assertTrue(list.contains("ac2"));
        assertTrue(list.contains("ac3"));
        assertTrue(list.contains("ac4"));
        for(int i = 5; i < 100; i++) {
            assertFalse(list.contains("a" + i));
        }
    }

    @Test
    public void subList() {
        List<String> a = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            a.add("a" + i);
        }

        list.addAll(a);

        List<String> sub = list.subList(10, 50);
        for(int i = 10; i < 50; i++) {
            assertEquals("a" + i, sub.get(i - 10));
        }
    }

    @Test
    public void isEmpty() {
        assertTrue(list.isEmpty());
        list.add("asd");
        assertFalse(list.isEmpty());
        list.remove("asd");
        assertTrue(list.isEmpty());
    }

    @Test
    public void contains() {
        list.add("test");
        list.add("test");
        list.add("asd");
        list.add("gf");

        assertTrue(list.contains("test"));
        assertTrue(list.contains("gf"));

        assertFalse(list.contains("asdfasdfasd"));
    }

    @Test
    public void toObjectArray() {
        List<String> a = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            a.add("a" + i);
        }

        list.addAll(a);

        Object[] arr = list.toArray();

        for(int i = 0; i < 100; i++) {
            assertEquals(a.get(i), arr[i]);
        }
    }

    @Test
    public void toArray() {
        List<String> a = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            a.add("a" + i);
        }

        list.addAll(a);

        assertNull(list.toArray(null));
        String[] arr = list.toArray(new String[0]);

        for(int i = 0; i < 100; i++) {
            assertEquals(a.get(i), arr[i]);
        }
    }

    @Test
    public void removeObject() {
        List<String> a = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            a.add("a" + i);
        }

        list.addAll(a);

        assertTrue(list.contains("a0"));
        assertTrue(list.remove("a0"));
        assertFalse(list.contains("a0"));
        assertFalse(list.remove("a0"));
    }

    @Test
    public void containsAll() {
        List<String> a = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            a.add("a" + i);
        }

        list.addAll(a);

        assertTrue(list.containsAll(a));

        a.add("asd");
        assertFalse(list.containsAll(a));
    }

    @Test
    public void removeAll() {
        List<String> a = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            a.add("a" + i);
        }

        list.addAll(a);

        assertFalse(list.isEmpty());
        a.remove("a0");
        assertTrue(list.removeAll(a));

        assertEquals(1, list.size());
        assertTrue(list.contains("a0"));
    }

    @Test
    public void retainAll() {
        List<String> a = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            a.add("a" + i);
        }

        list.addAll(a);

        assertFalse(list.isEmpty());
        a.remove("a0");

        assertTrue(list.retainAll(a));
        assertFalse(list.contains("a0"));
        assertTrue(list.containsAll(a));
    }

    @Test
    public void replaceAll() {
        List<String> a = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            a.add("a" + i);
        }

        list.addAll(a);

        assertFalse(list.isEmpty());

        list.replaceAll(s -> "b" + s);
        for(String s : a)
            assertTrue(list.contains("b" + s));
    }

    @Test
    public void replaceAllWithTime() {
        List<String> a = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            a.add("a" + i);
        }

        list.addAll(a);

        assertFalse(list.isEmpty());

        list.replaceAll(s -> "b" + s, aLong -> -1L);
        list.update();

        assertTrue(list.isEmpty());
    }

    @Test
    public void sortDefault() {
        ThreadLocalRandom.current().ints(100)
                .mapToObj(operand -> "a" + Integer.toString(operand))
                .forEach(list::add);
        List<String> sample = list.subList(0, 100);
        list.sort(null);

        sample.sort(String::compareTo);
        for(int i = 0; i < sample.size(); i++) {
            assertEquals(sample.get(i), list.get(i));
        }
    }

    @Test
    public void sortWithComparator() {
        ThreadLocalRandom.current().ints(100)
                .mapToObj(operand -> "a" + Integer.toString(operand))
                .forEach(list::add);
        List<String> sample = list.subList(0, 100);
        list.sort((o1, o2) -> -o1.compareTo(o2));

        sample.sort((o1, o2) -> -o1.compareTo(o2));
        for(int i = 0; i < sample.size(); i++) {
            assertEquals(sample.get(i), list.get(i));
        }
    }

    @Test
    public void spliterator() {
        ThreadLocalRandom.current().ints(100)
                .mapToObj(operand -> "a" + Integer.toString(operand))
                .forEach(list::add);
        list.spliterator().forEachRemaining(s -> {
            assertTrue(list.contains(s));
            list.remove(s);
        });
    }

    @Test
    public void removeIf() {
        ThreadLocalRandom.current().ints(100)
                .mapToObj(operand -> "a" + Integer.toString(operand))
                .forEach(list::add);
        assertFalse(list.isEmpty());
        assertTrue(list.removeIf(s -> true));
        assertTrue(list.isEmpty());
    }

    @Test
    public void parallelStream() {
        ThreadLocalRandom.current().ints(100)
                .mapToObj(operand -> "a" + Integer.toString(operand))
                .forEach(list::add);
        list.parallelStream().forEach(list::remove);
    }

    @Test
    public void forEach() {
        ThreadLocalRandom.current().ints(100)
                .mapToObj(operand -> "a" + Integer.toString(operand))
                .forEach(list::add);
        List<String> a = new ArrayList<>();
        //noinspection UseBulkOperation
        list.forEach((Consumer<String>) a::add);
        assertEquals(a.size(), list.size());
        assertTrue(list.containsAll(a));
    }

    @Test
    public void forEachWithTime() {
        list.stop();

        ThreadLocalRandom.current().ints(100)
                .mapToObj(operand -> "a" + Integer.toString(operand))
                .forEach(list::add);
        List<String> a = new ArrayList<>();
        //noinspection UseBulkOperation
        list.forEach((s, aLong) -> a.add(s + ":" + aLong));
        assertEquals(a.size(), list.size());
        for(int i = 0; i < list.size(); i++) {
            assertTrue(a.get(i).startsWith(list.get(i)));
        }
    }

    @Test
    public void cloneTest() {
        ThreadLocalRandom.current().ints(100)
                .mapToObj(operand -> "a" + Integer.toString(operand))
                .forEach(list::add);
        List<String> clone = list.clone();
        assertEquals(clone, list);
        String t1 = clone.get(0);
        list.set(0, "asdfsdaassadsasasadsad");
        assertEquals(t1, clone.get(0));
    }

    @Test
    public void isRunning() {
        assertTrue(list.isRunning());
        list.stop();
        assertFalse(list.isRunning());
        list.start();
        assertTrue(list.isRunning());
    }

    @Test
    public void testLogic() throws Exception {
        for(int i = 0; i < 100; i++) {
            list.add(Integer.toString(i));
        }
        list.add("a", 1);
        for(int i = 0; i < 100; i++) {
            list.add(Integer.toString(i));
        }

        Thread.sleep(2);
        assertFalse(list.contains("a"));

        list.stop();
        list.add("a", 1);
        Thread.sleep(10);
        assertTrue(list.contains("a"));
        list.start();
        Thread.sleep(2);
        assertFalse(list.contains("a"));
    }
}