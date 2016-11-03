package com.alesharik.webserver.api;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressFBWarnings("UC_USELESS_OBJECT")
public class ConcurrentCompletableFutureTest {
    @Test
    public void cancel() throws Exception {
        ConcurrentCompletableFuture<?> future = new ConcurrentCompletableFuture<>();
        assertTrue(future.cancel(true));
    }

    @Test
    public void isCancelled() throws Exception {
        ConcurrentCompletableFuture<?> future = new ConcurrentCompletableFuture<>();
        assertFalse(future.isCancelled());
        future.cancel(true);
        assertTrue(future.isCancelled());
    }

    @Test
    public void set() throws Exception {
        ConcurrentCompletableFuture<Object> future = new ConcurrentCompletableFuture<>();
        future.set(new Object());
        future.set(null);
    }

    @Test
    public void isDone() throws Exception {
        ConcurrentCompletableFuture<Object> future = new ConcurrentCompletableFuture<>();
        assertFalse(future.isDone());
        future.set(new Object());
        assertTrue(future.isDone());
    }

    @Test
    public void get() throws Exception {
        ConcurrentCompletableFuture<Object> future = new ConcurrentCompletableFuture<>();
        Object test = new Object();
        future.set(test);
        assertTrue(test.equals(future.get()));
    }

    @Test
    public void concurrentSetAndGet() throws Exception {
        final ConcurrentCompletableFuture<Integer> future = new ConcurrentCompletableFuture<>();
        Thread thread = new Thread(() -> future.set(123));
        thread.start();
        assertTrue(future.get() == 123);
    }

    @Test
    public void timedGet() throws Exception {
        final ConcurrentCompletableFuture<Integer> future = new ConcurrentCompletableFuture<>();
        future.get(100, TimeUnit.MILLISECONDS);
    }
}