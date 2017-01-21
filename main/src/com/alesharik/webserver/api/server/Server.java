package com.alesharik.webserver.api.server;

import com.alesharik.webserver.api.fileManager.FileManager;

import java.io.IOException;

/**
 * This class used is abstraction of basic server
 */
public abstract class Server {
    protected String host;
    protected int port;

    public Server(String host, int port, FileManager fileManager) {
        this(host, port);
    }

    protected Server(String host, int port) {
        this.host = host;
        this.port = port;
    }

    protected Server() {
    }

    public abstract void start() throws IOException;

    public abstract void shutdown() throws IOException;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
