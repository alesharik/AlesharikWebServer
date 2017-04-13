package com.alesharik.webserver.api;

import com.alesharik.webserver.TestUtils;
import com.alesharik.webserver.logger.LoggerUncaughtExceptionHandler;
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