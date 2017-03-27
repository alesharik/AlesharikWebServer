package com.alesharik.webserver.api.ticking;

import org.junit.Ignore;
import org.junit.Test;

import java.lang.ref.WeakReference;

import static com.alesharik.webserver.api.ticking.ExecutorPoolBasedTickingPool.TickableCache;
import static com.alesharik.webserver.api.ticking.ExecutorPoolBasedTickingPool.TickableCacheManager;
import static org.junit.Assert.*;

public class TickableCacheManagerTest {

    @Test
    public void simpleTest() throws Exception {
        Tickable tickable = () -> {
        };
        TickableCacheManager.addTickable(tickable);
        assertNotNull(TickableCacheManager.forTickable(tickable));
    }

    @Test
    public void notFoundTest() throws Exception {
        assertNull(TickableCacheManager.forTickable(() -> {
        }));
    }

    @Test
    public void addExistsTickable() throws Exception {
        Tickable tickable = () -> {
        };
        TickableCache tickableCache = TickableCacheManager.addTickable(tickable);
        TickableCache tickableCache1 = TickableCacheManager.addTickable(tickable);
        assertSame(tickableCache, tickableCache1);
    }

    @Test
    @Ignore("unstable")
    public void tryEnqueueVariable() throws Exception {
        Tickable tickable = () -> {
        };
        WeakReference<TickableCache> weakReference = new WeakReference<>(TickableCacheManager.addTickable(tickable));

        System.gc();

        System.gc();//2 is better than 1

        assertNull(weakReference.get());
        assertTrue(weakReference.isEnqueued());
    }
}