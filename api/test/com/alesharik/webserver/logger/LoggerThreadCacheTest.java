package com.alesharik.webserver.logger;

import com.alesharik.webserver.api.documentation.test.LongTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@LongTest
public class LoggerThreadCacheTest {
    private Logger.LoggerThreadCache cache = new Logger.LoggerThreadCache();

    @Test
    public void addAndGet() throws Exception {
        cache.add(String.class, "test");
        assertEquals("test", cache.get(String.class));
    }

    @Test
    public void cacheClearTest() throws Exception {
        cache.add(String.class, "test");
        Thread.sleep(Logger.LoggerThreadCache.CONTAINS_TIME + Logger.LoggerThreadCache.UPDATE_DELAY);
        assertNull(cache.get(String.class));
    }

    @Test
    public void cacheUpdateTimeTest() throws Exception {
        cache.add(String.class, "test");
        int max = Logger.LoggerThreadCache.CONTAINS_TIME + Logger.LoggerThreadCache.UPDATE_DELAY;
        int firstDelta = max / 2;
        Thread.sleep(firstDelta);
        assertEquals("test", cache.get(String.class));
        Thread.sleep(max - firstDelta + Logger.LoggerThreadCache.UPDATE_DELAY);
        assertEquals("test", cache.get(String.class));
    }
}