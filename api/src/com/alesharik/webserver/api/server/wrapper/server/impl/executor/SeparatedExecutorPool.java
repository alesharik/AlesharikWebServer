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
import com.alesharik.webserver.api.server.wrapper.server.ExecutorPool;

import javax.annotation.Nonnull;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;

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
    public <T> Future<T> submitSelectorTask(ForkJoinTask<T> task) {
        return selectorPool.submit(task);
    }

    @Override
    public void executeSelectorTask(Runnable task) {
        selectorPool.execute(task);
    }

    @Override
    public <T> Future<T> submitWorkerTask(ForkJoinTask<T> task) {
        return workerPool.submit(task);
    }

    @Override
    public void executeWorkerTask(Runnable task) {
        workerPool.execute(task);
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
}
