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
import com.alesharik.webserver.api.cache.object.Recyclable;
import com.alesharik.webserver.api.cache.object.SmartCachedObjectFactory;
import com.alesharik.webserver.api.name.Named;
import com.alesharik.webserver.api.server.wrapper.server.BatchingForkJoinTask;
import com.alesharik.webserver.api.server.wrapper.server.BatchingRunnableTask;
import com.alesharik.webserver.api.server.wrapper.server.ExecutorPool;
import com.alesharik.webserver.api.server.wrapper.server.SelectorContext;
import com.alesharik.webserver.api.server.wrapper.server.SocketProvider;
import org.jctools.queues.MpscGrowableArrayQueue;

import javax.annotation.Nonnull;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;

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
    protected final Map<Object, BatchingTask> workerBatches = new ConcurrentHashMap<>();

    protected volatile List<SelectorWorkerThread> selectorPool;
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
        return selectorPool == null ? 0 : selectorPool.size();
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
        return 0;
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
        this.selectorPool = new CopyOnWriteArrayList<>();
        this.workerPool = new ForkJoinPool(workerParallelism, threadFactory, null, false);
        System.out.println("Separated executor pool in " + group.getName() + " thread group successfully started");
    }

    @Override
    public void shutdownNow() {
        System.out.println("Emergency shutdown pool in " + group.getName() + " thread group");
        selectorPool.forEach(SelectorWorkerThread::shutdown);
        workerPool.shutdownNow();
    }

    @Override
    public void shutdown() {
        System.out.println("Shutdown pool in " + group.getName() + " thread group");
        selectorPool.forEach(SelectorWorkerThread::shutdown);
        workerPool.shutdown();
        System.out.println("Shutdown successful of pool in " + group.getName() + " thread group");
    }

    @Override
    public boolean isRunning() {
        return workerPool != null && !workerPool.isShutdown() && selectorPool != null;
    }

    @Override
    public void setSelectorContexts(SelectorContext.Factory factory) {
        selectorPool.forEach(SelectorWorkerThread::shutdown);
        selectorPool.clear();
        for(int i = 0; i < selectorParallelism; i++) {
            SelectorWorkerThread workerThread = new SelectorWorkerThread(factory.newInstance(), group);
            workerThread.start();
            selectorPool.add(workerThread);
        }
    }

    @Override
    public void selectSocket(SelectableChannel socket, SocketChannel socketChannel, SocketProvider.SocketManager socketManager) {
        SelectorWorkerThread lower = selectorPool.get(0);
        for(SelectorWorkerThread workerThread : selectorPool) {
            if(workerThread.getSocketCount() < lower.getSocketCount())
                lower = workerThread;
        }
        lower.registerSocket(socket, socketChannel, socketManager);
    }

    @Override
    public <T, K> Future<T> submitWorkerTask(@Nonnull BatchingForkJoinTask<K, T> task) {
        Object key = task.getKey();
        if(workerBatches.containsKey(key)) {
            if(workerBatches.getOrDefault(key, BatchingTask.FACTORY.getInstance().fill(workerBatches, key, workerPool).runTask()).add(task))
                return task;
        }
        BatchingTask task1 = BatchingTask.FACTORY.getInstance().fill(workerBatches, key, workerPool);
        task1.add(task);
        task1.runTask();
        return task;
    }

    @Override
    public void executeWorkerTask(@Nonnull BatchingRunnableTask task) {
        Object key = task.getKey();
        if(workerBatches.containsKey(key)) {
            if(workerBatches.getOrDefault(key, BatchingTask.FACTORY.getInstance().fill(workerBatches, key, workerPool).runTask()).add(task))
                return;
        }
        BatchingTask task1 = BatchingTask.FACTORY.getInstance().fill(workerBatches, key, workerPool);
        task1.add(task);
        task1.runTask();
    }

    protected static final class BatchingTask implements Runnable, Recyclable {
        private static final int MAX_COUNT = 1024;
        private static final SmartCachedObjectFactory<BatchingTask> FACTORY = new SmartCachedObjectFactory<>(BatchingTask::new, 1, TimeUnit.MILLISECONDS, 32);

        private final MpscGrowableArrayQueue<ForkJoinTask> tasks;
        private final MpscGrowableArrayQueue<Runnable> runnables;
        private final AtomicReference<State> state;

        private volatile Map<Object, BatchingTask> removeMap;
        private volatile Object object;
        private volatile ForkJoinPool pool;
        private volatile StampedLock stampedLock;

        public BatchingTask() {
            this.tasks = new MpscGrowableArrayQueue<>(8, MAX_COUNT);
            this.runnables = new MpscGrowableArrayQueue<>(8, MAX_COUNT);
            this.state = new AtomicReference<>(State.NOT_COMPLETED);
            this.stampedLock = new StampedLock();
        }

        public BatchingTask fill(Map<Object, BatchingTask> removeMap, Object object, ForkJoinPool pool) {
            this.removeMap = removeMap;
            this.object = object;
            this.pool = pool;
            return this;
        }

        public BatchingTask runTask() {
            pool.execute(this);
            removeMap.put(object, this);
            return this;
        }

        public boolean add(ForkJoinTask task) {
            long stamp = stampedLock.readLock();//Use read lock because queue is concurrent, but method need to interrupt run method and allow concurrent queue adding
            try {
                if(state.updateAndGet(state1 -> tasks.size() >= MAX_COUNT ? State.TASKS_COMPLETED : state1) == State.NOT_COMPLETED) {
                    tasks.add(task);
                    return true;
                } else
                    return false;
            } finally {
                stampedLock.unlock(stamp);
            }
        }

        public boolean add(Runnable runnable) {
            long stamp = stampedLock.readLock();//Use read lock because queue is concurrent, but method need to interrupt run method and allow concurrent queue adding
            try {
                if(state.updateAndGet(state1 -> runnables.size() >= MAX_COUNT ? State.ALL_COMPLETED : state1) != State.ALL_COMPLETED) {
                    runnables.add(runnable);
                    return true;
                } else
                    return false;
            } finally {
                stampedLock.unlock(stamp);
            }
        }

        @Override
        public void run() {
            try {
                while(tasks.isEmpty() && runnables.isEmpty())
                    Thread.sleep(1);
                while(!tasks.isEmpty()) {
                    long writeLock = stampedLock.writeLock();
                    try {
                        tasks.poll().invoke();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        stampedLock.unlockWrite(writeLock);
                    }
                }

                long lock = stampedLock.writeLock();
                try {
                    if(state.updateAndGet(state1 -> tasks.isEmpty() ? State.TASKS_COMPLETED : State.NOT_COMPLETED) == State.NOT_COMPLETED)
                        run();
                } finally {
                    stampedLock.unlockWrite(lock);
                }

                while(!runnables.isEmpty()) {
                    long writeLock = stampedLock.writeLock();//TODO use better way to do so(unlock write only if someone tries to add task)
                    try {
                        runnables.poll().run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        stampedLock.unlockWrite(writeLock);
                    }
                }

                lock = stampedLock.writeLock();
                try {
                    if(state.updateAndGet(state1 -> runnables.isEmpty() ? State.ALL_COMPLETED : State.TASKS_COMPLETED) == State.TASKS_COMPLETED)
                        run();
                } finally {
                    stampedLock.unlockWrite(lock);
                }
                removeMap.remove(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
            FACTORY.putInstance(this);
        }

        @Override
        public void recycle() {
            removeMap = null;
            object = null;
            pool = null;
            stampedLock = new StampedLock();
            tasks.clear();
            runnables.clear();
            state.set(State.NOT_COMPLETED);
        }

        protected enum State {
            NOT_COMPLETED,
            TASKS_COMPLETED,
            ALL_COMPLETED
        }
    }

    private static final class SelectorWorkerThread extends Thread {
        private final SelectorContext context;
        private volatile boolean isRunning;

        public SelectorWorkerThread(SelectorContext context, ThreadGroup threadGroup) {
            super(threadGroup, "SelectorThread");
            this.context = context;
        }

        @Override
        public synchronized void start() {
            isRunning = true;
            super.start();
        }

        @Override
        public void run() {
            while(isRunning) {
                try {
                    context.iteration();
                } catch (RuntimeException e) {
                    if(e.getCause().getClass().isAssignableFrom(InterruptedException.class))
                        interrupt();
                    else
                        e.printStackTrace();
                }
            }
        }

        public long getSocketCount() {
            return context.getSocketCount();
        }

        public void registerSocket(SelectableChannel channel, SocketChannel socketChannel, SocketProvider.SocketManager socketManager) {
            context.registerSocket(channel, socketChannel, socketManager);
        }

        public void shutdown() {
            if(!isRunning)
                return;
            isRunning = false;
            context.wakeup();
        }
    }
}
