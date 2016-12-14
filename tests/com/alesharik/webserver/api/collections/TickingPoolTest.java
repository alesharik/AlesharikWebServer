package com.alesharik.webserver.api.collections;

import org.junit.BeforeClass;
import org.junit.Test;

public class TickingPoolTest {
    private static LiveHashMap<Object, Object> map;
    private static ConcurrentLiveArrayList<Object> list;

    @BeforeClass
    public static void setup() {
        map = new LiveHashMap<>(1000, 16, false);
        list = new ConcurrentLiveArrayList<>(1000, 16, false);
        TickingPool.addHashMap(map, 10001); // For warm up TickingPool
    }

    @Test
    public void addHashMap0() {
        TickingPool.addHashMap(map, 1000);
    }

    @Test
    public void addHashMap1() {
        TickingPool.addHashMap(map, -1000);
    }

    @Test
    public void addHashMap2() {
        TickingPool.addHashMap(map, 0);
    }

    @Test
    public void addArrayList0() throws Exception {
        TickingPool.addArrayList(list, 100011);
    }

    @Test
    public void addArrayList1() throws Exception {
        TickingPool.addArrayList(list, -1000);
    }

    @Test
    public void addArrayList2() throws Exception {
        TickingPool.addArrayList(list, 0);
    }

    @Test
    public void removeHashMap0() throws Exception {
        TickingPool.removeHashMap(map, 1000);
    }

    @Test
    public void removeHashMap1() throws Exception {
        TickingPool.removeHashMap(map, -1000);
    }

    @Test
    public void removeHashMap2() throws Exception {
        TickingPool.removeHashMap(map, 0);
    }

    @Test
    public void removeArrayList0() throws Exception {
        TickingPool.removeArrayList(list, 100011);
    }

    @Test
    public void removeArrayList1() throws Exception {
        TickingPool.removeArrayList(list, -100011);
    }

    @Test
    public void removeArrayList2() throws Exception {
        TickingPool.removeArrayList(list, 0);
    }

    @Test
    public void concurrencyTest() throws InterruptedException {
        Thread thread = new Thread(() -> {
            TickingPool.addArrayList(list, 10002);
            TickingPool.addHashMap(map, 10002);
        });
        thread.start();
        TickingPool.addArrayList(list, 10001);
        TickingPool.addHashMap(map, 10001);
        thread.join();

        Thread thread1 = new Thread(() -> {
            TickingPool.removeHashMap(map, 10001);
            TickingPool.removeHashMap(map, 10001);
        });
        thread1.start();
        TickingPool.removeArrayList(list, 10002);
        TickingPool.removeHashMap(map, 10002);
        thread1.join();
    }
}