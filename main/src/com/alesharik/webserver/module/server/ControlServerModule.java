package com.alesharik.webserver.module.server;

import com.alesharik.webserver.api.fileManager.FileManager;
import com.alesharik.webserver.configuration.Layer;
import com.alesharik.webserver.configuration.Module;
import com.alesharik.webserver.configuration.XmlHelper;
import com.alesharik.webserver.control.AdminDataStorage;
import com.alesharik.webserver.control.dashboard.DashboardDataHolder;
import com.alesharik.webserver.control.dataStorage.AdminDataStorageImpl;
import com.alesharik.webserver.control.websockets.dashboard.DashboardWebSocketApplication;
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

//TODO remove
public class ControlServerModule implements Module {
    private HttpServer httpServer;

    @Override
    public void parse(@Nullable Element configNode) {
        AdminDataStorage adminDataStorage = XmlHelper.getAdminDataStorage("adminDataStorage", configNode, true);
        httpServer = new HttpServer();
        NetworkListener networkListener = new NetworkListener("grizzy", "0.0.0.0", 8000);
        networkListener.registerAddOn(new WebSocketAddOn());
        httpServer.addListener(networkListener);

        FileManager fileManager = new FileManager(new File("./serverDashboard"), FileManager.FileHoldingMode.HOLD_AND_CHECK);

        ControlHttpHandler controlHttpHandler = new ControlHttpHandler(fileManager, (AdminDataStorageImpl) adminDataStorage, true, new File("./logs/request-log.log"), new ModularErrorPageGenerator(fileManager));
        httpServer.getServerConfiguration().addHttpHandler(controlHttpHandler);

        DashboardDataHolder dashboardDataHolder = new DashboardDataHolder();
        WebSocketEngine.getEngine().register("", "/dashboard", new DashboardWebSocketApplication(dashboardDataHolder, controlHttpHandler.getControlRequestHandler()));
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
