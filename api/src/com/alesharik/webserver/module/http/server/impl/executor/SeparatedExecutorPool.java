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

package com.alesharik.webserver.module.http.server.impl.executor;

import com.alesharik.webserver.api.ThreadFactories;
import com.alesharik.webserver.api.cache.object.Recyclable;
import com.alesharik.webserver.api.cache.object.SmartCachedObjectFactory;
import com.alesharik.webserver.api.name.Named;
import com.alesharik.webserver.extension.module.layer.SubModule;
import com.alesharik.webserver.module.http.server.BatchingRunnableTask;
import com.alesharik.webserver.module.http.server.ExecutorPool;
import com.alesharik.webserver.module.http.server.SelectorContext;
import com.alesharik.webserver.module.http.server.socket.ServerSocketWrapper;
import org.jctools.queues.atomic.MpscLinkedAtomicQueue;

import javax.annotation.Nonnull;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class uses separated {@link ForkJoinPool} for provide {@link ExecutorPool} functionality
 */
@SuppressWarnings("WeakerAccess") //protected allow customize this class
@Named("separated-executor-pool")
@SubModule("separated-executor-pool")
public class SeparatedExecutorPool implements ExecutorPool {
    protected final int selectorParallelism;
    protected final int workerParallelism;
    protected final ThreadGroup group;
    protected final ForkJoinPool.ForkJoinWorkerThreadFactory threadFactory;
    protected final Map<Object, BatchingTask> workerBatches = new ConcurrentHashMap<>();

    protected volatile List<SelectorWorkerThread> selectorPool;
    protected volatile ForkJoinPool workerPool;
    protected final AtomicBoolean writeLock = new AtomicBoolean();

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
        return -1;
    }

    @Nonnull
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
    public void selectSocket(SelectableChannel socket, SocketChannel socketChannel, ServerSocketWrapper.SocketManager socketManager) {
        SelectorWorkerThread lower = selectorPool.get(0);
        for(SelectorWorkerThread workerThread : selectorPool) {
            if(workerThread.getSocketCount() < lower.getSocketCount())
                lower = workerThread;
        }
        lower.registerSocket(socket, socketChannel, socketManager);
    }

    @Override
    public void executeWorkerTask(@Nonnull BatchingRunnableTask task) {
        BatchingTask t = workerBatches.get(task.getKey());
        if(t == null || t.closed) {
            while(!writeLock.compareAndSet(false, true))
                //noinspection StatementWithEmptyBody
                while(writeLock.get()) ;
            t = BatchingTask.create(workerBatches, task.getKey());
            BatchingTask removedTask = workerBatches.put(task.getKey(), t);
            if(removedTask != null) {
                workerBatches.put(task.getKey(), removedTask);
                removedTask.queue.add(task);
                BatchingTask.FACTORY.putInstance(t);
                return;
            }
            writeLock.set(false);
            t.queue.add(task);
            workerPool.execute(t);
        } else {
            while(!t.closeLock.compareAndSet(false, true))
                //noinspection StatementWithEmptyBody
                while(t.closeLock.get()) ;
            if(t.closed) {
                while(!writeLock.compareAndSet(false, true))
                    //noinspection StatementWithEmptyBody
                    while(writeLock.get()) ;
                t = BatchingTask.create(workerBatches, task.getKey());
                workerBatches.put(task.getKey(), t);
                writeLock.set(false);
                workerPool.execute(t);
            } else
                t.queue.add(task);
            t.closeLock.set(false);
        }
    }

    protected static final class BatchingTask implements Runnable, Recyclable {
        private static final SmartCachedObjectFactory<BatchingTask> FACTORY = new SmartCachedObjectFactory<>(BatchingTask::new);

        private final Queue<Runnable> queue = new MpscLinkedAtomicQueue<>();
        private Map<Object, BatchingTask> toRemove;
        private Object object;
        private volatile boolean closed;
        private final AtomicBoolean closeLock = new AtomicBoolean(false);

        public static BatchingTask create(Map<Object, BatchingTask> toRemove, Object o) {
            BatchingTask task = FACTORY.getInstance();
            task.toRemove = toRemove;
            task.object = o;
            return task;
        }

        @Override
        public void recycle() {
            queue.clear();
            toRemove = null;
            object = null;
            closed = false;
        }

        @Override
        public void run() {
            Runnable poll;
            while((poll = queue.poll()) != null)
                poll.run();
            toRemove.remove(object);
            while(!closeLock.compareAndSet(false, true))
                //noinspection StatementWithEmptyBody
                while(closeLock.get()) ;
            closed = true;
            closeLock.set(false);
            Runnable poll1;
            while((poll1 = queue.poll()) != null)
                poll1.run();
            FACTORY.putInstance(this);
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

        public void registerSocket(SelectableChannel channel, SocketChannel socketChannel, ServerSocketWrapper.SocketManager socketManager) {
            context.registerSocket(channel, socketChannel, socketManager);
        }

        public void shutdown() {
            if(!isRunning)
                return;
            isRunning = false;
            context.wakeup();
            context.close();
        }
    }
}
