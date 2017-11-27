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

import com.alesharik.webserver.api.mx.bean.MXBeanManager;
import com.alesharik.webserver.logger.Prefixes;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import sun.misc.Cleaner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This pool use <code>Executors.newScheduledThreadPool()</code> pool => it execute {@link Tickable}s in multiple threads => you can
 * do(but it is not recommended) long operations in {@link Tickable}s or execute a lot of {@link Tickable}s without having a timing problem
 * This class perfectly used in high-load systems and systems where you need speed, concurrency and timing
 */
@Prefixes({"[TickingPool]", "[ExecutorPoolBasedTickingPool]"})
@ThreadSafe
public final class ExecutorPoolBasedTickingPool implements TickingPool {
    private static final AtomicLong COUNTER = new AtomicLong(0);

    private static final int DEFAULT_PARALLELISM = 10;

    private final ConcurrentHashMap<TickableCache, Boolean> tickables;
    private final ScheduledExecutorService executor;

    private final int parallelism;
    private final long id;

    /**
     * Create {@link ExecutorPoolBasedTickingPool} with default parallelism(var <code>DEFAULT_PARALLELISM</code>)
     * and <code>Thread::new</code> {@link ThreadFactory}
     *
     * @throws NullPointerException     if name or threadGroup == null
     * @throws IllegalArgumentException if name is empty
     */
    public ExecutorPoolBasedTickingPool() {
        this(DEFAULT_PARALLELISM);
    }

    /**
     * Create {@link ExecutorPoolBasedTickingPool} with <code>Thread::new</code> {@link ThreadFactory}
     *
     * @param parallelism amount of threads
     * @throws NullPointerException     if name or threadGroup == null
     * @throws IllegalArgumentException if name is empty
     */
    public ExecutorPoolBasedTickingPool(int parallelism) {
        this(parallelism, Thread::new);
    }

    /**
     * Create {@link ExecutorPoolBasedTickingPool}
     *
     * @param parallelism   amount of threads
     * @param threadFactory thread factory
     * @throws NullPointerException     if threadFactory == null
     * @throws IllegalArgumentException if parallelism <= 0
     */
    public ExecutorPoolBasedTickingPool(int parallelism, @Nonnull ThreadFactory threadFactory) {
        if(parallelism <= 0) {
            throw new IllegalArgumentException();
        }

        this.parallelism = parallelism;
        this.tickables = new ConcurrentHashMap<>();
        this.executor = Executors.newScheduledThreadPool(parallelism, threadFactory);

        this.id = COUNTER.incrementAndGet();

        MXBeanManager.registerMXBean(this, TickingPoolMXBean.class, "com.alesharik.webserver.api.ticking:type=ExecutorPoolBasedTickingPool,id=" + this.id);
        Cleaner.create(this, () -> MXBeanManager.unregisterMXBean("com.alesharik.webserver.api.ticking:type=ExecutorPoolBasedTickingPool,id=" + this.id));
    }

    @Override
    public void startTicking(@Nonnull Tickable tickable, long periodInMs) {
        if(periodInMs <= 0) {
            throw new IllegalArgumentException();
        }


        ExecutorTask command = new ExecutorTask(tickable, tickables);
        tickables.put(command.getTickable(), true);
        executor.scheduleAtFixedRate(command, 0, periodInMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stopTicking(@Nonnull Tickable tickable) {
        TickableCache key = TickableCacheManager.forTickable(tickable);
        if(key != null)
            tickables.remove(key);
    }

    @Override
    public void pauseTickable(@Nonnull Tickable tickable) {
        TickableCache key = TickableCacheManager.forTickable(tickable);
        if(key != null)
            tickables.replace(key, true, false);
    }

    @Override
    public void resumeTickable(@Nonnull Tickable tickable) {
        TickableCache key = TickableCacheManager.forTickable(tickable);
        if(key != null)
            tickables.replace(key, false, true);
    }

    @Override
    public boolean isRunning(@Nonnull Tickable tickable) {
        TickableCache key = TickableCacheManager.forTickable(tickable);
        if(key != null)
            return tickables.get(key);
        else
            return false;
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public void shutdownNow() {
        executor.shutdownNow();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof ExecutorPoolBasedTickingPool)) return false;

        ExecutorPoolBasedTickingPool that = (ExecutorPoolBasedTickingPool) o;

        if(tickables != null ? !tickables.equals(that.tickables) : that.tickables != null) return false;
        return executor != null ? executor.equals(that.executor) : that.executor == null;
    }

    @Override
    public int hashCode() {
        int result = tickables != null ? tickables.hashCode() : 0;
        result = 31 * result + (executor != null ? executor.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ExecutorPoolBasedTickingPool{" +
                "tickables=" + tickables +
                ", executor=" + executor +
                '}';
    }

    @Override
    public int getThreadCount() {
        return parallelism;
    }

    @Override
    public int getTotalTaskCount() {
        return tickables.size();
    }

    @Override
    public int getRunningTaskCount() {
        return (int) tickables.values().stream()
                .filter(Boolean::booleanValue)
                .count();
    }

    @Override
    public int getPauseTaskCount() {
        return (int) tickables.values().stream()
                .filter(aBoolean -> !aBoolean)
                .count();
    }

    /**
     * The id used for find needed {@link TickingPool} in {@link javax.management.MXBean}
     */
    public long getId() {
        return id;
    }

    /**
     * This task handles {@link Tickable}. It check if pool contains {@link Tickable}(if not - shutdown itself) and if {@link Tickable} is running and tick the {@link Tickable}
     */
    @Immutable
    static final class ExecutorTask implements Runnable {
        @Getter
        private final TickableCache tickable;
        private final ConcurrentHashMap<TickableCache, Boolean> tickables;

        public ExecutorTask(@Nonnull Tickable tickable, @Nonnull ConcurrentHashMap<TickableCache, Boolean> tickables) {
            this.tickable = TickableCacheManager.addTickable(tickable);
            this.tickables = tickables;
        }

        @Override
        public void run() {
            if(tickables.containsKey(tickable)) {
                if(tickables.get(tickable)) {
                    try {
                        tickable.tick();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                throw new RuntimeException("Stop the task!");
            }
        }
    }

    /**
     * This class hold tickable and it's first <code>objectHashCode</code>
     */
    @Immutable
    @Getter
    static final class TickableCache {
        private final Tickable tickable;
        private final int hashCode;

        public TickableCache(@Nonnull Tickable tickable) {
            this.tickable = tickable;
            this.hashCode = tickable.objectHashCode();
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(!(o instanceof TickableCache)) return false;

            TickableCache that = (TickableCache) o;

            return Integer.compare(hashCode, that.hashCode) == 0;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        public void tick() throws Exception {
            tickable.tick();
        }

        @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION") //tickable is always non-null!
        @Nonnull
        public Tickable getTickable() {
            return tickable;
        }
    }

    /**
     * This class manage all {@link TickableCache}s in JVM
     */
    @ThreadSafe
    @UtilityClass
    static final class TickableCacheManager {
        private static final CopyOnWriteArraySet<WeakReference<TickableCache>> cache = new CopyOnWriteArraySet<>();

        /**
         * Get cache for tickable
         *
         * @param tickable tickable
         * @return cache if cache found, null - tickable doesn't have a cache
         */
        @Nullable
        public static TickableCache forTickable(@Nonnull Tickable tickable) {
            for(WeakReference<TickableCache> tickableCache : cache) {
                TickableCache cache = tickableCache.get();
                if(tickableCache.isEnqueued() || cache == null) {
                    TickableCacheManager.cache.remove(tickableCache);
                    continue;
                }

                if(cache.getTickable().objectEquals(tickable)) {
                    return cache;
                }
            }
            return null;
        }

        /**
         * Add new tickable to cache if not exists
         *
         * @param tickable tickable
         * @return new or existing tickable cache
         */
        public static TickableCache addTickable(@Nonnull Tickable tickable) {
            TickableCache tickableCache = forTickable(tickable);
            if(tickableCache == null) {
                tickableCache = new TickableCache(tickable);
                cache.add(new WeakReference<>(tickableCache));
                return tickableCache;
            } else {
                return tickableCache;
            }
        }
    }
}
