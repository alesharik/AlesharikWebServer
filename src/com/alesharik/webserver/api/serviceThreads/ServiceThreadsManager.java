package com.alesharik.webserver.api.serviceThreads;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public final class ServiceThreadsManager {
    private final Thread thread;
    private final ThreadFactory threadFactory;
    private final SharedStorage sharedStorage = SharedStorage.create();
    private Executor executor;

    ServiceThreadsManager(Thread thread, ThreadFactory threadFactory) {
        this.thread = thread;
        this.threadFactory = threadFactory;
    }

    public void start() {
        executor = Executors.newFixedThreadPool(10, threadFactory);
    }

    public void shutdown() {
        executor = null;
    }

    public boolean isRunning() {
        return executor != null;
    }

    private void checkExecutor() {
        if(executor == null) {
            throw new IllegalStateException();
        }
    }

    public void execute(Runnable task) {
        checkExecutor();
        executor.execute(task);
    }

    public void execute(ServiceThreadRunnable runnable) {
        runnable.setSharedStorage(sharedStorage);
        executor.execute(runnable);
    }

    public <T> Future<T> publish(ServiceThreadTask<T> threadTask) {
        threadTask.setSharedStorage(sharedStorage);
        executor.execute(threadTask);
        return threadTask.getFuture();
    }
}
