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

import com.alesharik.webserver.logger.Prefixes;
import lombok.AccessLevel;
import lombok.Getter;
import sun.misc.Cleaner;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.alesharik.webserver.api.ticking.ExecutorPoolBasedTickingPool.*;

/**
 * This class use <code>Executors.newSingleThreadScheduledExecutor()</code> to create executor pool => it execute all {@link Tickable}s in one thread
 * => it can execute {@link Tickable} not in time if {@link Tickable} execute long operations or pool have too many {@link Tickable}s
 * It is prefect for use if you need execute small amount of fast tasks or you need a fast and lightweight pool
 */
@Prefixes({"[TickingPool]", "[OneThreadTickingPool]"})
@ThreadSafe
public final class OneThreadTickingPool implements TickingPool {
    private static final AtomicLong COUNTER = new AtomicLong(0);

    private static final String DEFAULT_NAME = "TickingPool";

    private final ConcurrentHashMap<TickableCache, Boolean> tickables;
    private final ScheduledExecutorService executor;

    @Getter(value = AccessLevel.PACKAGE)
    private final Cleaner cleaner;

    private final long id;

    /**
     * Create {@link OneThreadTickingPool} with <code>DEFAULT_NAME</code> thread name which use current group (group of this thread)
     *
     * @throws NullPointerException     if name or threadGroup == null
     * @throws IllegalArgumentException if name is empty
     */
    public OneThreadTickingPool() {
        this(DEFAULT_NAME);
    }

    /**
     * Create {@link OneThreadTickingPool} which use current group (group of this thread)
     *
     * @param name name of the thread
     * @throws NullPointerException     if name or threadGroup == null
     * @throws IllegalArgumentException if name is empty
     */
    public OneThreadTickingPool(@Nonnull String name) {
        this(name, Thread.currentThread().getThreadGroup());
    }

    /**
     * Create {@link OneThreadTickingPool}
     *
     * @param name        name of the thread
     * @param threadGroup thread group of the thread
     * @throws NullPointerException     if name or threadGroup == null
     * @throws IllegalArgumentException if name is empty
     */
    public OneThreadTickingPool(@Nonnull String name, @Nonnull ThreadGroup threadGroup) {
        if(name.isEmpty())
            throw new IllegalArgumentException("Name can't be empty!");

        tickables = new ConcurrentHashMap<>();
        executor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(threadGroup, r, name));
        id = COUNTER.incrementAndGet();

        Management.registerMXBean(this, TickingPoolMXBean.class, "com.alesharik.webserver.api.ticking:type=OneThreadTickingPool,id=" + id);
        cleaner = Cleaner.create(this, () -> Management.unregisterMXBean("com.alesharik.webserver.api.ticking:type=OneThreadTickingPool,id=" + this.id));
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
        if(!(o instanceof OneThreadTickingPool)) return false;

        OneThreadTickingPool that = (OneThreadTickingPool) o;

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
        return "OneThreadTickingPool{" +
                "tickables=" + tickables +
                ", executor=" + executor +
                '}';
    }

    @Override
    public int getThreadCount() {
        return 1;
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
     * The id used for find needed {@link TickingPool} in mx beans
     */
    public long getId() {
        return id;
    }
}
