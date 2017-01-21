package com.alesharik.webserver.main.server;

import com.alesharik.webserver.api.MIMETypes;
import com.alesharik.webserver.api.errorPageGenerators.ErrorPageGenerator;
import com.alesharik.webserver.api.fileManager.FileManager;
import com.alesharik.webserver.api.server.RequestHandler;
import com.alesharik.webserver.api.server.WebServer;
import com.alesharik.webserver.control.dashboard.DashboardDataHolder;
import com.alesharik.webserver.control.dataStorage.AdminDataStorageImpl;
import com.alesharik.webserver.control.websockets.dashboard.DashboardWebSocketApplication;
import com.alesharik.webserver.generators.ModularErrorPageGenerator;
import com.alesharik.webserver.handlers.ControlHttpHandler;
import org.glassfish.grizzly.http.server.CompressionLevel;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;

import java.io.File;
import java.io.IOException;

public final class ControlServer extends WebServer {
    private HttpServer httpServer;
    private ControlHttpHandler controlHttpHandler;
    private ModularErrorPageGenerator errorPageGenerator;
    private DashboardWebSocketApplication dashboardWebSocketApplication;

    public ControlServer(String host, int port, FileManager fileManager, AdminDataStorageImpl adminDataStorageImpl, DashboardDataHolder holder, boolean logRequests, File logFile) {
        super(host, port);
        errorPageGenerator = new ModularErrorPageGenerator(fileManager);
        controlHttpHandler = new ControlHttpHandler(fileManager, adminDataStorageImpl, logRequests, logFile, errorPageGenerator);
        final NetworkListener networkListener = new NetworkListener("grizzly", host, port);
        networkListener.getFileCache().setEnabled(true);
        networkListener.setCompression(String.valueOf(CompressionLevel.ON));
        String compressedMimeTypes = MIMETypes.findType(".html") +
                ',' +
                MIMETypes.findType(".jpg") +
                ',' +
                MIMETypes.findType(".png") +
                ',' +
                MIMETypes.findType(".css") +
                ',' +
                MIMETypes.findType(".js");
        networkListener.setCompressableMimeTypes(compressedMimeTypes);
        httpServer = new HttpServer();
        httpServer.addListener(networkListener);
        ServerConfiguration serverConfiguration = httpServer.getServerConfiguration();
        serverConfiguration.setJmxEnabled(true);
        serverConfiguration.addHttpHandler(controlHttpHandler, "/");
        TCPNIOTransportBuilder transportBuilder = TCPNIOTransportBuilder.newInstance();
        ThreadPoolConfig config = ThreadPoolConfig.defaultConfig();
        config.setCorePoolSize(10)
                .setMaxPoolSize(10)
                .setQueueLimit(-1);
        transportBuilder.setWorkerThreadPoolConfig(config);
        networkListener.setTransport(transportBuilder.build());

        WebSocketAddOn addOn = new WebSocketAddOn();
        networkListener.registerAddOn(addOn);

        dashboardWebSocketApplication = new DashboardWebSocketApplication(holder, controlHttpHandler.getControlRequestHandler());
        registerNewWebSocket(dashboardWebSocketApplication, "", "/dashboard");
    }

    @Override
    public synchronized void addRequestHandler(RequestHandler requestHandler) {
        controlHttpHandler.addRequestHandler(requestHandler);
    }

    @Override
    public synchronized void removeRequestHandler(RequestHandler requestHandler) {
        controlHttpHandler.removeRequestHandler(requestHandler);
    }

    @Override
    public synchronized void start() throws IOException {
        httpServer.start();
    }

    @Override
    public synchronized void shutdown() {
        httpServer.stop();
    }

    @Override
    public synchronized void registerNewWebSocket(WebSocketApplication application, String contextPath, String urlPattern) {
        WebSocketEngine.getEngine().register(contextPath, urlPattern, application);
    }

    @Override
    public synchronized void unregisterWebSocket(WebSocketApplication application) {
        WebSocketEngine.getEngine().unregister(application);
    }

    @Override
    public ErrorPageGenerator getErrorPageGenerator() {
        return errorPageGenerator;
    }
}
