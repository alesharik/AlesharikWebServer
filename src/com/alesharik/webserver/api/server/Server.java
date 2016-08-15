package com.alesharik.webserver.api.server;

import com.alesharik.webserver.main.FileManager;
import com.alesharik.webserver.plugin.accessManagers.ServerAccessManagerBuilder;
import org.glassfish.grizzly.websockets.WebSocketApplication;

import java.io.IOException;

/**
 * This class is abstraction on server
 */
public abstract class Server {

    public Server(String host, int port, FileManager fileManager) {
    }

    protected Server() {
    }

    public abstract void addRequestHandler(RequestHandler requestHandler);

    public abstract void removeRequestHandler(RequestHandler requestHandler);

    public abstract void start() throws IOException;

    public abstract void shutdown();

    public abstract void registerNewWebSocket(WebSocketApplication application, String contextPath, String urlPattern);

    public abstract void unregisterWebSocket(WebSocketApplication application);

    public void setupServerAccessManagerBuilder(ServerAccessManagerBuilder builder) {
        builder.setServer(this);
    }
}
