package com.alesharik.webserver.microservices.client;

import com.alesharik.webserver.api.loadInfo.Info;

import java.util.concurrent.locks.ReentrantLock;

public final class MicroserviceClientInfo implements Info {
    private final ReentrantLock lock = new ReentrantLock();
    private int taskCount = 0;


    @Override
    public void loadInfo() {
        lock.lock();

        lock.unlock();
    }

    public int getTaskCount() {
        lock.lock();
        int count = taskCount;
        lock.unlock();
        return count;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public long getUpdateMillis() {
        return 0;
    }
}
