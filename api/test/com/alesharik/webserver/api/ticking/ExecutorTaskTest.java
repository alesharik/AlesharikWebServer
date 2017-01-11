package com.alesharik.webserver.api.ticking;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

public class ExecutorTaskTest {
    private Tickable tickable;
    private ExecutorPoolBasedTickingPool.ExecutorTask executorTask;
    private ExecutorPoolBasedTickingPool.ExecutorTask exception;
    private ConcurrentHashMap<Tickable, Boolean> map = new ConcurrentHashMap<>();

    @Before
    public void setUp() throws Exception {
        tickable = () -> {
        };

        map.put(tickable, true);
        executorTask = new ExecutorPoolBasedTickingPool.ExecutorTask(tickable, map);

        Tickable thr = () -> {
            throw new Exception();
        };
        map.put(thr, true);
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
        map.remove(tickable);
        executorTask.run();
    }
}