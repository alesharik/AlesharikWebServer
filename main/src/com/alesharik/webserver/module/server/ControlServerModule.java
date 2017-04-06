package com.alesharik.webserver.module.server;

import com.alesharik.webserver.api.fileManager.FileManager;
import com.alesharik.webserver.configuration.Layer;
import com.alesharik.webserver.configuration.Module;
import com.alesharik.webserver.configuration.XmlHelper;
import com.alesharik.webserver.control.AdminDataStorage;
import com.alesharik.webserver.control.dashboard.DashboardDataHolder;
import com.alesharik.webserver.control.dashboard.websocket.DashboardWebSocketApplication;
import com.alesharik.webserver.control.dataStorage.AdminDataStorageImpl;
import com.alesharik.webserver.generators.ModularErrorPageGenerator;
import com.alesharik.webserver.handlers.ControlHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

//TODO remove
public class ControlServerModule implements Module {
    private HttpServer httpServer;
    private FileManager fileManager;
    private DashboardWebSocketApplication app;
    @Override
    public void parse(@Nullable Element configNode) {
        AdminDataStorage adminDataStorage = XmlHelper.getAdminDataStorage("adminDataStorage", configNode, true);
        httpServer = new HttpServer();
        NetworkListener networkListener = new NetworkListener("grizzy", "0.0.0.0", 8000);
        networkListener.registerAddOn(new WebSocketAddOn());
        httpServer.addListener(networkListener);

        fileManager = new FileManager(new File("./serverDashboard"), FileManager.FileHoldingMode.HOLD_AND_CHECK);

        ControlHttpHandler controlHttpHandler = new ControlHttpHandler(fileManager, (AdminDataStorageImpl) adminDataStorage, true, new File("./logs/request-log.log"), new ModularErrorPageGenerator(fileManager));
        httpServer.getServerConfiguration().addHttpHandler(controlHttpHandler);
        DashboardDataHolder dashboardDataHolder = XmlHelper.getDashboardDataHolder("dashboardDataHolder", configNode, true);
//        app = new DashboardWebSocketApplication(dashboardDataHolder, controlHttpHandler.getControlRequestHandler());
//        WebSocketEngine.getEngine().register("", "/dashboard", app);
        List<String> webSocketPlugins = XmlHelper.getList("webSocketPlugins", "plugin", configNode, false);
        WebSocketEngine.getEngine().register("", "/dashboard", new DashboardWebSocketApplication(new HashSet<>(webSocketPlugins), dashboardDataHolder));
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public HttpServer getHttpServer() {
        return httpServer;
    }

    @Override
    public void reload(@Nullable Element configNode) {
        shutdown();
        parse(configNode);
        start();
    }

    @Override
    public void start() {
        try {
            httpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        httpServer.stop();
    }

    @Override
    public void shutdownNow() {
        httpServer.stop();
    }

    @Nonnull
    @Override
    public String getName() {
        return "control-temp";
    }

    @Nullable
    @Override
    public Layer getMainLayer() {
        return null;
    }
}
