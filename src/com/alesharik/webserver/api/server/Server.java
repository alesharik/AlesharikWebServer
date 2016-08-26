package com.alesharik.webserver.api.server;

import com.alesharik.webserver.main.FileManager;

import java.io.IOException;

/**
 * This class is abstraction on server
 */
public abstract class Server {

    public Server(String host, int port, FileManager fileManager) {
    }

    protected Server() {
    }

    public abstract void start() throws IOException;

    public abstract void shutdown();
}
