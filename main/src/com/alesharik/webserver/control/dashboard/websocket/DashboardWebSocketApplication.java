package com.alesharik.webserver.control.dashboard.websocket;

import com.alesharik.webserver.control.dashboard.DashboardDataHolder;
import com.alesharik.webserver.server.api.WSApplication;
import com.alesharik.webserver.server.api.WSChecker;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketListener;

import java.util.Set;

/**
 * Always disabled for auto registration. Must be registered by control server
 */
@WSApplication(value = "/dashboard", checker = WSChecker.Disabled.class)
public class DashboardWebSocketApplication extends WebSocketApplication {
    private final Set<String> plugins;
    private final DashboardDataHolder dashboardDataHolder;

    public DashboardWebSocketApplication(Set<String> plugins, DashboardDataHolder dashboardDataHolder) {
        this.plugins = plugins;
        this.dashboardDataHolder = dashboardDataHolder;
        plugins.add("menu");
        plugins.add("menuPlugins");
        plugins.add("currentCompInfo");
        plugins.add("system");
    }

    @Override
    public WebSocket createSocket(ProtocolHandler handler, HttpRequestPacket requestPacket, WebSocketListener... listeners) {
        DashboardWebSocket dashboardWebSocket = new DashboardWebSocket(handler, requestPacket, listeners);
        dashboardWebSocket.add(new DashboardWebSocketUserContext(plugins, dashboardDataHolder));
        return dashboardWebSocket;
    }
}
