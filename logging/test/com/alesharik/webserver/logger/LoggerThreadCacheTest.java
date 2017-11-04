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
        Thread.sleep(Logger.LoggerThreadCache.CONTAINS_TIME + Logger.LoggerThreadCache.UPDATE_DELAY + 1);
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