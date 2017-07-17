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

package com.alesharik.webserver.api.server.wrapper.server.impl.executor;

import com.alesharik.webserver.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintStream;
import java.util.concurrent.ForkJoinTask;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SeparatedExecutorPoolTest {
    private final ThreadGroup threadGroup = new ThreadGroup("test");
    private SeparatedExecutorPool executorPool;

    @Before
    public void setUp() throws Exception {
        executorPool = new SeparatedExecutorPool(10, 10, threadGroup);
    }

    @After
    public void tearDown() throws Exception {
        executorPool.shutdownNow();
    }

    @Test
    public void testMXBeanMethods() throws Exception {
        assertEquals(10, executorPool.getSelectorPoolThreadCount());
        assertEquals(10, executorPool.getWorkerPoolThreadCount());

        assertEquals(0, executorPool.getWorkerPoolAliveThreadCount());
        assertEquals(0, executorPool.getSelectorPoolAliveThreadCount());

        assertEquals(0, executorPool.getSelectorPoolTaskCount());
        assertEquals(0, executorPool.getWorkerPoolTaskCount());


        executorPool.start();
        executorPool.executeSelectorTask(new TestUtils.WaitTask(10 * 1000));
        executorPool.executeWorkerTask(new TestUtils.WaitTask(10 * 1000));

        assertEquals(1, executorPool.getWorkerPoolAliveThreadCount());
        assertEquals(1, executorPool.getSelectorPoolAliveThreadCount());

        for(int i = 0; i < 10; i++) {
            executorPool.executeSelectorTask(new TestUtils.WaitTask(10 * 1000));
            executorPool.executeWorkerTask(new TestUtils.WaitTask(10 * 1000));
        }

        Thread.sleep(10);

        assertTrue(executorPool.getSelectorPoolTaskCount() > 0);
        assertTrue(executorPool.getWorkerPoolTaskCount() > 0);

        assertEquals(threadGroup, executorPool.getThreadGroup());
    }

    @Test
    public void testLifeCycleStable() throws Exception {
        PrintStream mock = mock(PrintStream.class);
        System.setOut(mock);

        assertFalse(executorPool.isRunning());
        executorPool.start();
        assertTrue(executorPool.isRunning());

        Runnable runnable = mock(Runnable.class);
        Object checker = new Object();

        executorPool.executeWorkerTask(runnable);
        executorPool.executeSelectorTask(runnable);

        assertEquals(checker, executorPool.submitSelectorTask(ForkJoinTask.adapt(runnable, checker)).get());
        assertEquals(checker, executorPool.submitWorkerTask(ForkJoinTask.adapt(runnable, checker)).get());

        Thread.sleep(100);

        verify(runnable, times(4)).run();

        executorPool.shutdown();
        assertFalse(executorPool.isRunning());

        verify(mock).println("Starting separated FJP based executor pool (selector: 10 threads, worker: 10 threads)");
        verify(mock).println("Separated executor pool in test thread group successfully started");
        verify(mock).println("Shutdown pool in test thread group");
        verify(mock).println("Shutdown successful of pool in test thread group");
    }

    @Test
    public void testLifeCycleEmergency() throws Exception {
        PrintStream mock = mock(PrintStream.class);
        System.setOut(mock);

        assertFalse(executorPool.isRunning());
        executorPool.start();
        assertTrue(executorPool.isRunning());

        Runnable runnable = mock(Runnable.class);
        Object checker = new Object();

        executorPool.executeWorkerTask(runnable);
        executorPool.executeSelectorTask(runnable);

        assertEquals(checker, executorPool.submitSelectorTask(ForkJoinTask.adapt(runnable, checker)).get());
        assertEquals(checker, executorPool.submitWorkerTask(ForkJoinTask.adapt(runnable, checker)).get());

        Thread.sleep(100);

        verify(runnable, times(4)).run();

        executorPool.shutdownNow();
        assertFalse(executorPool.isRunning());

        verify(mock).println("Starting separated FJP based executor pool (selector: 10 threads, worker: 10 threads)");
        verify(mock).println("Separated executor pool in test thread group successfully started");
        verify(mock).println("Emergency shutdown pool in test thread group");

    }
}