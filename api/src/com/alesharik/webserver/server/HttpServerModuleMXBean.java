package com.alesharik.webserver.server;

public interface HttpServerModuleMXBean {
    long requestCount();

    int getCoreThreadCount();

    int getMaxThreadCount();

    int getWorkerQueueLimit();

    int getWorkerQueueSize();

    int getSelectorThreadCount();
}
