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

import com.alesharik.webserver.api.ThreadFactories;
import com.alesharik.webserver.api.name.Named;
import com.alesharik.webserver.api.server.wrapper.server.BatchingForkJoinTask;
import com.alesharik.webserver.api.server.wrapper.server.BatchingRunnableTask;
import com.alesharik.webserver.api.server.wrapper.server.ExecutorPool;
import org.jctools.queues.MpscGrowableArrayQueue;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class uses separated {@link ForkJoinPool} for provide {@link ExecutorPool} functionality
 */
@SuppressWarnings("WeakerAccess") //protected allow customize this class
@Named("separated-executor-pool")
public class SeparatedExecutorPool implements ExecutorPool {
    protected final int selectorParallelism;
    protected final int workerParallelism;
    protected final ThreadGroup group;
    protected final ForkJoinPool.ForkJoinWorkerThreadFactory threadFactory;
    protected final Map<Object, BatchingTask> selectorBatches = new ConcurrentHashMap<>();
    protected final Map<Object, BatchingTask> workerBatches = new ConcurrentHashMap<>();

    protected volatile ForkJoinPool selectorPool;
    protected volatile ForkJoinPool workerPool;

    public SeparatedExecutorPool(int selector, int worker, ThreadGroup threadGroup) {
        this.selectorParallelism = selector;
        this.workerParallelism = worker;
        this.group = threadGroup;
        this.threadFactory = ThreadFactories.newFJPThreadFactory(group);
    }

    @Override
    public int getSelectorPoolThreadCount() {
        return selectorParallelism;
    }

    @Override
    public int getSelectorPoolAliveThreadCount() {
        return selectorPool == null ? 0 : selectorPool.getActiveThreadCount();
    }

    @Override
    public int getWorkerPoolThreadCount() {
        return workerParallelism;
    }

    @Override
    public int getWorkerPoolAliveThreadCount() {
        return workerPool == null ? 0 : workerPool.getActiveThreadCount();
    }

    @Override
    public long getSelectorPoolTaskCount() {
        return selectorPool == null ? 0 : selectorPool.getQueuedSubmissionCount();
    }

    @Override
    public long getWorkerPoolTaskCount() {
        return workerPool == null ? 0 : workerPool.getQueuedSubmissionCount();
    }

    @Override
    public ThreadGroup getThreadGroup() {
        return group;
    }

    @Override
    public boolean batchingSupported() {
        return true;
    }

    @Override
    public long maxBatchQueueSize() {
        return 64;
    }

    @Nonnull
    @Override
    public String getName() {
        return "separated-executor-pool";
    }

    @Override
    public void start() {
        System.out.println("Starting separated FJP based executor pool (selector: " + selectorParallelism + " threads, worker: " + workerParallelism + " threads)");
        this.selectorPool = new ForkJoinPool(selectorParallelism, threadFactory, null, false);
        this.workerPool = new ForkJoinPool(workerParallelism, threadFactory, null, false);
        System.out.println("Separated executor pool in " + group.getName() + " thread group successfully started");
    }

    @Override
    public void shutdownNow() {
        System.out.println("Emergency shutdown pool in " + group.getName() + " thread group");
        selectorPool.shutdownNow();
        workerPool.shutdownNow();
    }

    @Override
    public void shutdown() {
        System.out.println("Shutdown pool in " + group.getName() + " thread group");
        selectorPool.shutdown();
        workerPool.shutdown();
        System.out.println("Shutdown successful of pool in " + group.getName() + " thread group");
    }

    @Override
    public boolean isRunning() {
        return workerPool != null && !workerPool.isShutdown() && selectorPool != null && !selectorPool.isShutdown();
    }

    @Override
    public <T, K> Future<T> submitSelectorTask(@Nonnull BatchingForkJoinTask<K, T> task) {
        if(!selectorBatches.getOrDefault(task.getKey(), new BatchingTask(selectorBatches, task.getKey(), selectorPool)).add(task)) {
            BatchingTask task1 = new BatchingTask(selectorBatches, task.getKey(), selectorPool);
            task1.add(task);
            selectorBatches.put(task.getKey(), task1);
        }
        return task;
    }

    @Override
    public void executeSelectorTask(@Nonnull BatchingRunnableTask task) {
        if(!selectorBatches.getOrDefault(task.getKey(), new BatchingTask(selectorBatches, task.getKey(), selectorPool)).add(task)) {
            BatchingTask task1 = new BatchingTask(selectorBatches, task.getKey(), selectorPool);
            task1.add(task);
            selectorBatches.put(task.getKey(), task1);
        }
    }

    @Override
    public <T, K> Future<T> submitWorkerTask(@Nonnull BatchingForkJoinTask<K, T> task) {
        if(!workerBatches.getOrDefault(task.getKey(), new BatchingTask(workerBatches, task.getKey(), workerPool)).add(task)) {
            BatchingTask task1 = new BatchingTask(workerBatches, task.getKey(), workerPool);
            task1.add(task);
            workerBatches.put(task.getKey(), task1);
        }
        return task;
    }

    @Override
    public void executeWorkerTask(@Nonnull BatchingRunnableTask task) {
        if(!workerBatches.getOrDefault(task.getKey(), new BatchingTask(workerBatches, task.getKey(), workerPool)).add(task)) {
            BatchingTask task1 = new BatchingTask(workerBatches, task.getKey(), workerPool);
            task1.add(task);
            workerBatches.put(task.getKey(), task1);
        }
    }

    protected static final class BatchingTask extends ForkJoinTask<Void> {
        private static final int MAX_COUNT = 1024;

        private static final long serialVersionUID = 7621064335625241602L;

        private final MpscGrowableArrayQueue<ForkJoinTask> tasks;
        private final MpscGrowableArrayQueue<Runnable> runnables;
        private final AtomicReference<State> state;

        private final Map<Object, BatchingTask> removeMap;
        private final Object object;

        public BatchingTask(Map<Object, BatchingTask> removeMap, Object object, ForkJoinPool pool) {
            this.removeMap = removeMap;
            this.object = object;
            tasks = new MpscGrowableArrayQueue<>(8, MAX_COUNT);
            runnables = new MpscGrowableArrayQueue<>(8, MAX_COUNT);
            state = new AtomicReference<>(State.NOT_COMPLETED);
            pool.execute(this);
        }

        @Override
        public Void getRawResult() {
            return null;
        }

        @Override
        protected void setRawResult(Void value) {

        }

        public boolean add(ForkJoinTask task) {
            return state.updateAndGet(state1 -> {
                if(tasks.size() == MAX_COUNT)
                    return State.TASKS_COMPLETED;
                if(state1 == State.NOT_COMPLETED)
                    tasks.add(task);
                return state1;
            }) == State.NOT_COMPLETED;
        }

        public boolean add(Runnable runnable) {
            return state.updateAndGet(state1 -> {
                if(runnables.size() == MAX_COUNT)
                    return State.ALL_COMPLETED;
                if(state1 != State.ALL_COMPLETED)
                    runnables.add(runnable);
                return state1;
            }) != State.ALL_COMPLETED;
        }

        @Override
        protected boolean exec() {
            try {
                while(!tasks.isEmpty())
                    try {
                        tasks.poll().invoke();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                if(state.updateAndGet(state1 -> tasks.isEmpty() ? State.TASKS_COMPLETED : State.NOT_COMPLETED) == State.NOT_COMPLETED)
                    exec();

                while(!runnables.isEmpty())
                    try {
                        runnables.poll().run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                if(state.updateAndGet(state1 -> runnables.isEmpty() ? State.ALL_COMPLETED : State.TASKS_COMPLETED) == State.TASKS_COMPLETED)
                    exec();

                removeMap.remove(object);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        protected enum State {
            NOT_COMPLETED,
            TASKS_COMPLETED,
            ALL_COMPLETED
        }
    }
}
