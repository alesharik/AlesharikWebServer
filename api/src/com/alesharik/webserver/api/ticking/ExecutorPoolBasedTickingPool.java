package com.alesharik.webserver.api.ticking;

import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefix;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * This pool use <code>Executors.newScheduledThreadPool()</code> pool => it execute {@link Tickable}s in multiple threads => you can
 * do(but it is not recommended) long operations in {@link Tickable}s or execute a lot of {@link Tickable}s without having a timing problem
 * This class perfectly used in high-load systems and systems where you need speed, concurrency and timing
 */
@Prefix("[TickingPool]")
public final class ExecutorPoolBasedTickingPool implements TickingPool {
    private static final int DEFAULT_PARALLELISM = 10;

    private final ConcurrentHashMap<Tickable, Boolean> tickables;
    private final ScheduledExecutorService executor;

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
    public ExecutorPoolBasedTickingPool(int parallelism, ThreadFactory threadFactory) {
        Objects.requireNonNull(threadFactory);
        if(parallelism <= 0) {
            throw new IllegalArgumentException();
        }

        tickables = new ConcurrentHashMap<>();
        executor = Executors.newScheduledThreadPool(parallelism, threadFactory);
    }

    @Override
    public void startTicking(Tickable tickable, long periodInMs) {
        Objects.requireNonNull(tickable);
        if(periodInMs <= 0) {
            throw new IllegalArgumentException();
        }

        tickables.put(tickable, true);

        executor.scheduleAtFixedRate(new ExecutorTask(tickable, tickables), 0, periodInMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stopTicking(Tickable tickable) {
        Objects.requireNonNull(tickable);

        tickables.remove(tickable);
    }

    @Override
    public void stopTicking(Tickable tickable, long periodInMs) {
        Objects.requireNonNull(tickable);
        if(periodInMs <= 0) {
            throw new IllegalArgumentException();
        }

        tickables.remove(tickable, periodInMs);
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

    /**
     * This task handles {@link Tickable}. It check if pool contains {@link Tickable}(if not - stop itself) and if {@link Tickable} is running and tick the {@link Tickable}
     */
    static final class ExecutorTask implements Runnable {
        private Tickable tickable;
        private final ConcurrentHashMap<Tickable, Boolean> tickables;

        public ExecutorTask(Tickable tickable, ConcurrentHashMap<Tickable, Boolean> tickables) {
            this.tickable = tickable;
            this.tickables = tickables;
        }

        @Override
        public void run() {
            if(tickables.containsKey(tickable)) {
                if(tickables.get(tickable)) {
                    try {
                        tickable.tick();
                    } catch (Exception e) {
                        Logger.log(e);
                    }
                }
            } else {
                throw new RuntimeException("Stop the task!");
            }
        }
    }
}
