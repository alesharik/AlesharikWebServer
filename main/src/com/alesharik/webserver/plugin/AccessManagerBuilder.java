package com.alesharik.webserver.plugin;

import com.alesharik.webserver.api.control.ControlWebSocketPlugin;
import com.alesharik.webserver.api.control.ControlWebSocketPluginWrapper;
import com.alesharik.webserver.api.errorPageGenerators.ErrorPageGenerator;
import com.alesharik.webserver.api.fileManager.FileManager;
import com.alesharik.webserver.api.server.RequestHandler;
import com.alesharik.webserver.api.server.WebServer;
import com.alesharik.webserver.control.dashboard.DashboardDataHolder;
import com.alesharik.webserver.control.websockets.control.WebSocketController;
import com.alesharik.webserver.plugin.accessManagers.BaseAccessManager;
import com.alesharik.webserver.plugin.accessManagers.ControlAccessManager;
import com.alesharik.webserver.plugin.accessManagers.DashboardAccessManager;
import com.alesharik.webserver.plugin.accessManagers.MicroserviceAccessManager;
import com.alesharik.webserver.plugin.accessManagers.PluginAccessManager;
import com.alesharik.webserver.plugin.accessManagers.ServerAccessManager;
import org.glassfish.grizzly.websockets.WebSocketApplication;

import java.util.Arrays;
import java.util.List;

public final class AccessManagerBuilder {
    private FileManager fileManager;
    private WebSocketController webSocketController;
    private DashboardDataHolder dashboardDataHolder;
    private WebServer webServer;

    public AccessManagerBuilder() {
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void setWebSocketController(WebSocketController webSocketController) {
        this.webSocketController = webSocketController;
    }

    public void setDashboardDataHolder(DashboardDataHolder dashboardDataHolder) {
        this.dashboardDataHolder = dashboardDataHolder;
    }

    public void setWebServer(WebServer webServer) {
        this.webServer = webServer;
    }

    @SuppressWarnings("RedundantCast") //Because IDEA has bugs!!!
    public AccessManager forPermissions(String permissions, String delim) {
        List<AccessPermissions> perms = Arrays.asList(AccessPermissions.fromString(permissions, delim));
        return new AccessManagerImpl(perms.toArray(new AccessPermissions[0]),
                (perms.contains(AccessPermissions.BASE) ? (BaseAccessManager) new BaseAccessManagerImpl(fileManager) : (BaseAccessManager) new Dude()),
                (perms.contains(AccessPermissions.CONTROL) ? (ControlAccessManager) new ControlAccessManagerImpl(webSocketController) : (ControlAccessManager) new Dude()),
                (perms.contains(AccessPermissions.DASHBOARD) ? (DashboardAccessManager) new DashboardAccessManagerImpl(dashboardDataHolder) : (DashboardAccessManager) new Dude()),
                (perms.contains(AccessPermissions.MICROSERVICES) ? (MicroserviceAccessManager) new MicroserviceAccessManagerImpl() : (MicroserviceAccessManager) new Dude()),
                (perms.contains(AccessPermissions.PLUGIN) ? (PluginAccessManager) new PluginAccessManagerImpl() : (PluginAccessManager) new Dude()),
                (perms.contains(AccessPermissions.SERVER) ? (ServerAccessManager) new ServerAccessManagerImpl(webServer) : (ServerAccessManager) new Dude())
        );
    }

    private static final class BaseAccessManagerImpl implements BaseAccessManager {
        private final FileManager fileManager;

        private BaseAccessManagerImpl(FileManager fileManager) {
            this.fileManager = fileManager;
        }

        @Override
        public FileManager getFileManager() {
            return fileManager;
        }
    }

    private static final class ControlAccessManagerImpl implements ControlAccessManager {
        private final WebSocketController webSocketController;

        private ControlAccessManagerImpl(WebSocketController webSocketController) {
            this.webSocketController = webSocketController;
        }

        @Override
        public ControlWebSocketPluginWrapper registerNewControlWebSocketPlugin(ControlWebSocketPlugin plugin) {
            return webSocketController.registerPlugin(plugin);
        }

        @Override
        public void unregisterControlWebSocketPlugin(ControlWebSocketPlugin plugin) {
            webSocketController.unregisterPlugin(plugin);
        }
    }

    private static final class DashboardAccessManagerImpl implements DashboardAccessManager {
        private final DashboardDataHolder dashboardDataHolder;

        private DashboardAccessManagerImpl(DashboardDataHolder dashboardDataHolder) {
            this.dashboardDataHolder = dashboardDataHolder;
        }

        @Override
        public DashboardDataHolder getDataHolder() {
            return dashboardDataHolder;
        }
    }

    private static final class MicroserviceAccessManagerImpl implements MicroserviceAccessManager {

    }

    private static final class PluginAccessManagerImpl implements PluginAccessManager {

    }

    private static final class ServerAccessManagerImpl implements ServerAccessManager {
        private final WebServer webServer;

        private ServerAccessManagerImpl(WebServer webServer) {
            this.webServer = webServer;
        }

        @Override
        public void registerNewWebSocket(WebSocketApplication application, String contextPath, String urlPattern) {
            webServer.registerNewWebSocket(application, contextPath, urlPattern);
        }

        @Override
        public void unregisterWebSocket(WebSocketApplication application) {
            webServer.unregisterWebSocket(application);
        }

        @Override
        public void addRequestHandler(RequestHandler requestHandler) {
            webServer.addRequestHandler(requestHandler);
        }

        @Override
        public void removeRequestHandler(RequestHandler requestHandler) {
            webServer.removeRequestHandler(requestHandler);
        }

        @Override
        public ErrorPageGenerator getErrorPageGenerator() {
            return webServer.getErrorPageGenerator();
        }
    }

    private static final class Dude implements BaseAccessManager, ControlAccessManager, DashboardAccessManager, MicroserviceAccessManager, PluginAccessManager, ServerAccessManager {

        @Override
        public FileManager getFileManager() {
            throw new IllegalStateException();
        }

        @Override
        public DashboardDataHolder getDataHolder() {
            throw new IllegalStateException();
        }

        @Override
        public void registerNewWebSocket(WebSocketApplication application, String contextPath, String urlPattern) {
            throw new IllegalStateException();
        }

        @Override
        public void unregisterWebSocket(WebSocketApplication application) {
            throw new IllegalStateException();
        }

        @Override
        public void addRequestHandler(RequestHandler requestHandler) {
            throw new IllegalStateException();
        }

        @Override
        public void removeRequestHandler(RequestHandler requestHandler) {
            throw new IllegalStateException();
        }

        @Override
        public ErrorPageGenerator getErrorPageGenerator() {
            throw new IllegalStateException();
        }

        @Override
        public ControlWebSocketPluginWrapper registerNewControlWebSocketPlugin(ControlWebSocketPlugin plugin) {
            throw new IllegalStateException();
        }

        @Override
        public void unregisterControlWebSocketPlugin(ControlWebSocketPlugin plugin) {
            throw new IllegalStateException();
        }
    }
}
