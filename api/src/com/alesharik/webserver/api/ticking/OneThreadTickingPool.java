package com.alesharik.webserver.api.ticking;

import com.alesharik.webserver.api.Utils;
import com.alesharik.webserver.logger.Prefix;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class use <code>Executors.newSingleThreadScheduledExecutor()</code> to create executor pool => it execute all {@link Tickable}s in one thread
 * => it can execute {@link Tickable} not in time if {@link Tickable} execute long operations or pool have too many {@link Tickable}s
 * It is prefect for use if you need execute small amount of fast tasks or you need a fast and lightweight pool
 */
@Prefix("[TickingPool]")
public final class OneThreadTickingPool implements TickingPool {
    private static final String DEFAULT_NAME = "TickingPool";

    private final ConcurrentHashMap<Tickable, Boolean> tickables;
    private final ScheduledExecutorService executor;

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
    public OneThreadTickingPool(String name) {
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
    public OneThreadTickingPool(String name, ThreadGroup threadGroup) {
        Objects.requireNonNull(threadGroup);
        Utils.requireNotNullOrEmpty(name);

        tickables = new ConcurrentHashMap<>();
        executor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(threadGroup, r, name));
    }

    @Override
    public void startTicking(Tickable tickable, long periodInMs) {
        Objects.requireNonNull(tickable);
        if(periodInMs <= 0) {
            throw new IllegalArgumentException();
        }

        tickables.put(tickable, true);

        executor.scheduleAtFixedRate(new ExecutorPoolBasedTickingPool.ExecutorTask(tickable, tickables), 0, periodInMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stopTicking(Tickable tickable) {
        Objects.requireNonNull(tickable);

        tickables.remove(tickable);
    }

    @Override
    public void pauseTickable(Tickable tickable) {
        Objects.requireNonNull(tickable);

        tickables.replace(tickable, true, false);
    }

    @Override
    public void resumeTickable(Tickable tickable) {
        Objects.requireNonNull(tickable);

        tickables.replace(tickable, false, true);
    }

    @Override
    public boolean isRunning(Tickable tickable) {
        Objects.requireNonNull(tickable);

        return tickables.get(tickable);
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
}
