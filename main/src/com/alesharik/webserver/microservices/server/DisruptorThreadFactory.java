package com.alesharik.webserver.microservices.server;

import java.util.concurrent.ThreadFactory;

class DisruptorThreadFactory implements ThreadFactory {
    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("MicroserviceServerDisruptor");

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(THREAD_GROUP, r);
        thread.setName("DisruptorThread");
        return thread;
    }
}
