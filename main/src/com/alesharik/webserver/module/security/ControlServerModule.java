/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.alesharik.webserver.module.security;

import com.alesharik.webserver.api.fileManager.FileManager;
import com.alesharik.webserver.configuration.Layer;
import com.alesharik.webserver.configuration.XmlHelper;
import com.alesharik.webserver.control.AdminDataStorage;
import com.alesharik.webserver.control.ControlServer;
import com.alesharik.webserver.control.dashboard.DashboardDataHolder;
import com.alesharik.webserver.control.dashboard.websocket.DashboardWebSocketApplication;
import com.alesharik.webserver.control.dashboard.websocket.DashboardWebSocketPlugin;
import com.alesharik.webserver.control.dashboard.websocket.DashboardWebSocketPluginListener;
import com.alesharik.webserver.control.data.storage.AdminDataStorageImpl;
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
@Deprecated
public class ControlServerModule implements ControlServer {
    private HttpServer httpServer;
    private FileManager fileManager;
    private DashboardWebSocketApplication webSocketApplication;

    @Override
    public void parse(@Nullable Element configNode) {
        AdminDataStorage adminDataStorage = XmlHelper.getAdminDataStorage("adminDataStorage", configNode, true);
        httpServer = new HttpServer();
        NetworkListener networkListener = new NetworkListener("grizzy", "0.0.0.0", 8000);
        networkListener.registerAddOn(new WebSocketAddOn());
        httpServer.addListener(networkListener);

        fileManager = new FileManager(new File("./serverDashboard"), FileManager.FileHoldingMode.HOLD_AND_CHECK);

        ControlHttpHandler controlHttpHandler = new ControlHttpHandler(fileManager, (AdminDataStorageImpl) adminDataStorage, true, new File("./logs/request-log.log"));
        httpServer.getServerConfiguration().addHttpHandler(controlHttpHandler);
        DashboardDataHolder dashboardDataHolder = XmlHelper.getDashboardDataHolder("dashboardDataHolder", configNode, true);
        List<String> webSocketPlugins = XmlHelper.getList("webSocketPlugins", "plugin", configNode, false);
        webSocketApplication = new DashboardWebSocketApplication(new HashSet<>(webSocketPlugins), dashboardDataHolder);
        WebSocketEngine.getEngine().register("", "/dashboard", webSocketApplication);
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

    @Override
    public <T extends DashboardWebSocketPlugin> void addDashboardWebSocketPluginListener(@Nonnull String name, @Nonnull Class<T> pluginClazz, @Nonnull DashboardWebSocketPluginListener<T> listener) {
        webSocketApplication.addDashboardWebSocketPluginListener(name, pluginClazz, listener);
    }

    @Override
    public <T extends DashboardWebSocketPlugin> void removeDashboardWebSocketPluginListener(@Nonnull String name, @Nonnull DashboardWebSocketPluginListener<T> listener) {
        webSocketApplication.removeDashboardWebSocketPluginListener(name, listener);
    }
}
