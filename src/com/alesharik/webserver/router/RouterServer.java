package com.alesharik.webserver.router;

import com.alesharik.webserver.api.server.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

//TODO use Linux asyncronous socket
public class RouterServer extends Server {
    private final ConcurrentHashMap<ArrayList<String>, String> servers = new ConcurrentHashMap<>();
    private final RouterServerRequestProcessor processor;

    public RouterServer(int port, String host, RouterServer.WorkingMode mode) {
        super(host, port);
        processor = new RouterServerRequestProcessor(port, host, mode, servers);
    }

    public void start() {
        processor.start();
    }

    public void shutdown() throws IOException {
        processor.shutdown();
    }

    public enum WorkingMode {
        SIMPLE,
        ADVANCED
    }
}
