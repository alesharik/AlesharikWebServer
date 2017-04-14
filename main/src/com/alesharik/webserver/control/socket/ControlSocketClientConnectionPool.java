package com.alesharik.webserver.control.socket;

import com.alesharik.webserver.api.control.messaging.ControlSocketClientConnection;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

class ControlSocketClientConnectionPool {
    private final ForkJoinPool workerPool;
    private final SSLSocketFactory sslSocketFactory;
    private final ConcurrentHashMap<SSLSocket, ControlSocketClientConnection> connections;

    public ControlSocketClientConnectionPool(int parallelism, SSLSocketFactory sslSocketFactory) {
        this.workerPool = new ForkJoinPool(parallelism);
        this.sslSocketFactory = sslSocketFactory;
        this.connections = new ConcurrentHashMap<>();
    }

    public void shutdown() {
        workerPool.shutdown();
    }
}
