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

import com.alesharik.webserver.exception.ExceptionWithoutStacktrace;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

import static com.alesharik.webserver.api.ticking.ExecutorPoolBasedTickingPool.TickableCache;
import static com.alesharik.webserver.api.ticking.ExecutorPoolBasedTickingPool.TickableCacheManager;

public class ExecutorTaskTest {
    private Tickable tickable;
    private ExecutorPoolBasedTickingPool.ExecutorTask executorTask;
    private ExecutorPoolBasedTickingPool.ExecutorTask exception;
    private ConcurrentHashMap<TickableCache, Boolean> map = new ConcurrentHashMap<>();

    @Before
    public void setUp() {
        tickable = () -> {
        };

        map.put(TickableCacheManager.addTickable(tickable), true);
        executorTask = new ExecutorPoolBasedTickingPool.ExecutorTask(tickable, map);

        Tickable thr = () -> {
            throw new ExceptionWithoutStacktrace();
        };
        map.put(TickableCacheManager.addTickable(thr), true);
        exception = new ExecutorPoolBasedTickingPool.ExecutorTask(thr, map);
    }

    @Test
    public void runNormal() {
        executorTask.run();
    }

    @Test
    public void runException() {
        exception.run();
    }

    @Test(expected = RuntimeException.class)
    public void stop() {
        TickableCache tickableCache = TickableCacheManager.forTickable(tickable);
        if(tickableCache != null) {
            map.remove(tickableCache);
            executorTask.run();
        }
    }
}