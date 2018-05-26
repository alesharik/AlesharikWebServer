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

package com.alesharik.webserver.api;

import com.alesharik.webserver.logger.LoggerUncaughtExceptionHandler;
import com.alesharik.webserver.test.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class ThreadFactoriesTest {
    private ThreadGroup threadGroup;

    @Before
    public void setUp() throws Exception {
        threadGroup = new ThreadGroup("test");
    }

    @Test
    public void newThreadGroupThreadFactory() throws Exception {
        Thread thread = ThreadFactories.newThreadFactory(threadGroup).newThread(() -> {
        });
        assertEquals(thread.getThreadGroup(), threadGroup);
    }

    @Test
    public void newThreadFactoryWithThreadGroupAndUncaughtExceptionHandler() throws Exception {
        Thread thread = ThreadFactories.newThreadFactory(threadGroup, LoggerUncaughtExceptionHandler.INSTANCE).newThread(() -> {
        });
        assertEquals(thread.getThreadGroup(), threadGroup);
        assertEquals(thread.getUncaughtExceptionHandler(), LoggerUncaughtExceptionHandler.INSTANCE);
    }

    @Test
    public void newThreadFactoryWithNameSupplier() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        ThreadFactory threadFactory = ThreadFactories.newThreadFactory(() -> "thread" + counter.get());
        for(int i = 0; i < 100; i++) {
            assertEquals("thread" + counter.get(), threadFactory.newThread(() -> {
            }).getName());
            counter.incrementAndGet();
        }
    }

    @Test
    public void newThreadFactoryWithThreadGroupNameSupplier() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        ThreadFactory threadFactory = ThreadFactories.newThreadFactory(threadGroup, () -> "thread" + counter.get());
        for(int i = 0; i < 100; i++) {
            Thread thread = threadFactory.newThread(() -> {
            });
            assertEquals("thread" + counter.get(), thread.getName());
            assertEquals(threadGroup, thread.getThreadGroup());
            counter.incrementAndGet();
        }
    }

    @Test
    public void incrementalSupplier() throws Exception {
        Supplier<String> supplier = ThreadFactories.incrementalSupplier("test");
        for(int i = 0; i < 100; i++) {
            assertEquals("test" + i, supplier.get());
        }
    }

    @Test
    public void testUtilsClass() throws Exception {
        TestUtils.assertUtilityClass(ThreadFactories.class);
    }
}