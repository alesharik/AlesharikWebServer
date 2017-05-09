package com.alesharik.webserver.api;

import com.alesharik.webserver.logger.Logger;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * This class is basic implementation of {@link Future}. Used for create and set future. This class is thread-safe!
 *
 * @param <V>
 */
public class ConcurrentCompletableFuture<V> implements Future<V> {
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);
    private final AtomicBoolean isDone = new AtomicBoolean(false);
    private volatile V value = null;

    public ConcurrentCompletableFuture() {
    }

    /**
     * @param mayInterruptIfRunning don't used
     */
    @Override //TODO rewrite
    public boolean cancel(boolean mayInterruptIfRunning) {
        isCancelled.set(true);
        return true;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled.get();
    }

    @Override
    public boolean isDone() {
        return isDone.get();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        if(Thread.interrupted()) {
            return null;
        }
        while(value == null) {
            Signaller signaller = new Signaller(0L, 0L, new SoftReference<>(value));
            waitForSignaller(signaller);
        }
        return value;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Objects.requireNonNull(unit);
        if(Thread.interrupted()) {
            return null;
        }
        long nanos = unit.toNanos(timeout);
        if(value == null) {
            long d = System.nanoTime() + nanos;
            Signaller signaller = new Signaller(nanos, d == 0L ? 1L : d, new SoftReference<>(value));
            waitForSignaller(signaller);
        }
        return value;
    }

    private void waitForSignaller(Signaller signaller) {
        if(signaller.thread != null && value == null) {
            try {
                ForkJoinPool.managedBlock(signaller);
            } catch (InterruptedException e) {
                Logger.log(e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Set value and complete future
     */
    public void set(V value) {
        Objects.requireNonNull(value);
        this.value = value;
        isDone.set(true);
    }

    private static final class Signaller implements ForkJoinPool.ManagedBlocker {
        long nanos;
        final long deadline;
        final Reference<?> value;
        volatile Thread thread;

        Signaller(long nanos, long deadline, Reference<?> value) {
            this.thread = Thread.currentThread();
            this.nanos = nanos;
            this.deadline = deadline;
            this.value = value;
        }

        public boolean block() {
            if(isReleasable()) {
                return true;
            } else if(deadline == 0L) {
                LockSupport.park(this);
            } else if(nanos > 0L) {
                LockSupport.parkNanos(this, nanos);
            }
            return isReleasable();
        }

        @Override
        public boolean isReleasable() {
            if(deadline != 0L && (nanos <= 0L || (nanos = deadline - System.nanoTime()) <= 0L)) {
                thread = null;
                return true;
            }
            return value.get() != null;
        }
    }
}
