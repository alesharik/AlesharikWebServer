package com.alesharik.webserver.api.serviceThreads;

public abstract class ServiceThreadRunnable implements Runnable {
    protected SharedStorage sharedStorage;

    public ServiceThreadRunnable() {
    }

    public void setSharedStorage(SharedStorage sharedStorage) {
        this.sharedStorage = sharedStorage;
    }

    public abstract void run();
}
