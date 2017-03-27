package com.alesharik.webserver.api.ticking;

import org.junit.Before;
import org.junit.Test;

import static com.alesharik.webserver.api.ticking.ExecutorPoolBasedTickingPool.TickableCache;
import static org.junit.Assert.*;

public class TickableCacheTest {
    public static final Tickable TICKABLE = () -> {
    };
    public static final Tickable TICKABLE1 = () -> {
    };
    private TickableCache tickableCache;
    private TickableCache equals;
    private TickableCache notEquals;

    @Before
    public void setUp() throws Exception {
        tickableCache = new TickableCache(TICKABLE);
        equals = new TickableCache(TICKABLE);
        notEquals = new TickableCache(TICKABLE1);
    }

    @Test
    public void equalsTest() throws Exception {
        assertTrue(tickableCache.equals(equals));
        assertFalse(tickableCache.equals(notEquals));
    }

    @Test
    public void hashCodeTest() throws Exception {
        assertEquals(tickableCache.hashCode(), equals.getHashCode());
        assertFalse(Integer.compare(tickableCache.hashCode(), notEquals.hashCode()) == 0);
    }

    @Test
    public void hashCodeSaveTest() throws Exception {
        assertEquals(TICKABLE.hashCode(), tickableCache.hashCode());
        assertEquals(TICKABLE.hashCode(), equals.hashCode());
    }
}