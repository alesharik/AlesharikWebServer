package com.alesharik.webserver.api.server;

import com.alesharik.webserver.api.errorPageGenerators.ErrorPageGenerator;
import com.alesharik.webserver.api.fileManager.FileManager;
import com.alesharik.webserver.control.dashboard.DashboardDataHolder;
import org.glassfish.grizzly.websockets.WebSocketApplication;

import java.io.IOException;

public abstract class WebServer extends Server {
    public WebServer(String host, int port, FileManager fileManager, DashboardDataHolder holder) {
        super(host, port);
    }

    protected WebServer(String host, int port) {
        super(host, port);
    }

    protected WebServer() {
    }

    public abstract void addRequestHandler(RequestHandler requestHandler);

    public abstract void removeRequestHandler(RequestHandler requestHandler);

    @Override
    public abstract void start() throws IOException;

    @Override
    public abstract void shutdown();

    public abstract void registerNewWebSocket(WebSocketApplication application, String contextPath, String urlPattern);

    public abstract void unregisterWebSocket(WebSocketApplication application);

    public abstract ErrorPageGenerator getErrorPageGenerator();
}
