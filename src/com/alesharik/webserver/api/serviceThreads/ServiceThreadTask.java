package com.alesharik.webserver.api.serviceThreads;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public abstract class ServiceThreadTask<T> implements Runnable {
    protected SharedStorage sharedStorage;
    protected Future<T> future;

    public ServiceThreadTask() {
        future = new CompletableFuture<>();
    }

    Future<T> getFuture() {
        return future;
    }

    public void setSharedStorage(SharedStorage sharedStorage) {
        this.sharedStorage = sharedStorage;
    }

    abstract T get();
}
