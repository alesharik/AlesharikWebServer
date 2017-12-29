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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * This {@link Future} implementation uses {@link #set(Object)} method to complete it
 *
 * @param <V> return argument type
 */
@ThreadSafe
public final class ConcurrentCompletableFuture<V> implements Future<V> {
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);
    private final AtomicBoolean isDone = new AtomicBoolean(false);
    private volatile V value;

    private final List<Thread> waiting = new CopyOnWriteArrayList<>();

    /**
     * @param mayInterruptIfRunning don't used
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if(isCancelled.compareAndSet(false, true)) {
            for(Thread thread : waiting) {
                if(!thread.isInterrupted())
                    LockSupport.unpark(thread);
            }
            waiting.clear();
            return true;
        }
        return false;
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
    @Nullable
    public V get() {
        if(Thread.interrupted())
            return null;
        while(!isDone.get() && !isCancelled.get()) {
            Signaller signaller = new Signaller(0L, 0L, new SoftReference<>(value), isCancelled, isDone);
            waitForSignaller(signaller);
        }
        return value;
    }

    @Override
    @Nullable
    public V get(long timeout, @Nonnull TimeUnit unit) {
        if(Thread.interrupted())
            return null;
        long nanos = unit.toNanos(timeout);
        if(!isDone.get()) {
            long d = System.nanoTime() + nanos;
            Signaller signaller = new Signaller(nanos, d == 0L ? 1L : d, new SoftReference<>(value), isCancelled, isDone);
            waitForSignaller(signaller);
        }
        return value;
    }

    private void waitForSignaller(Signaller signaller) {
        if(signaller.thread != null && !isDone.get() && !isCancelled.get()) {
            try {
                waiting.add(signaller.thread);
                ForkJoinPool.managedBlock(signaller);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Set value and complete future
     */
    public void set(@Nonnull V value) {
        if(isDone.compareAndSet(false, true)) {
            this.value = value;
            for(Thread thread : waiting) {
                if(!thread.isInterrupted())
                    LockSupport.unpark(thread);
            }
            waiting.clear();
        }
    }

    private static final class Signaller implements ForkJoinPool.ManagedBlocker {
        long nanos;
        final long deadline;
        final Reference<?> value;
        final AtomicBoolean isCancelled;
        final AtomicBoolean isDone;
        volatile Thread thread;

        Signaller(long nanos, long deadline, Reference<?> value, AtomicBoolean isCancelled, AtomicBoolean isDone) {
            this.isCancelled = isCancelled;
            this.isDone = isDone;
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
            return isDone.get() || isCancelled.get();
        }
    }
}
