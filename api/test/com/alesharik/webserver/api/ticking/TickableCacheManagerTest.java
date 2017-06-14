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

package com.alesharik.webserver.api.ticking;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

    @SuppressFBWarnings("DM_GC")
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