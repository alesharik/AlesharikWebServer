package com.alesharik.webserver.api.ticking;

import com.alesharik.webserver.exceptions.ExceptionWithoutStacktrace;
import com.alesharik.webserver.logger.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import static com.alesharik.webserver.api.ticking.ExecutorPoolBasedTickingPool.TickableCache;
import static com.alesharik.webserver.api.ticking.ExecutorPoolBasedTickingPool.TickableCacheManager;

public class ExecutorTaskTest {
    private Tickable tickable;
    private ExecutorPoolBasedTickingPool.ExecutorTask executorTask;
    private ExecutorPoolBasedTickingPool.ExecutorTask exception;
    private ConcurrentHashMap<TickableCache, Boolean> map = new ConcurrentHashMap<>();

    @BeforeClass
    public static void init() throws Exception {
        Logger.setupLogger(File.createTempFile("asdassdfasd", "sdfgasdsdf"), 10);
    }

    @Before
    public void setUp() throws Exception {
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
    public void runNormal() throws Exception {
        executorTask.run();
    }

    @Test
    public void runException() throws Exception {
        exception.run();
    }

    @Test(expected = RuntimeException.class)
    public void stop() throws Exception {
        TickableCache tickableCache = TickableCacheManager.forTickable(tickable);
        if(tickableCache != null) {
            map.remove(tickableCache);
            executorTask.run();
        }
    }
}