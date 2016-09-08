package com.alesharik.webserver.control.websockets.dashboard;

import com.alesharik.webserver.api.server.dashboard.DashboardWebsocketPlugin;
import com.alesharik.webserver.api.server.dashboard.DashboardWebsocketWrapper;
import com.alesharik.webserver.control.dashboard.PluginDataHolder;
import com.alesharik.webserver.plugin.accessManagers.ControlAccessManagerBuilder;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.Broadcaster;
import org.glassfish.grizzly.websockets.OptimizedBroadcaster;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketListener;

import java.util.HashMap;

/**
 * This application used for manage {@link DashboardWebSocket}s
 */
public final class DashboardWebSocketApplication extends WebSocketApplication {
    private final Broadcaster broadcaster = new OptimizedBroadcaster();
    private final HashMap<String, DashboardWebsocketWrapper> wrappers = new HashMap<>();
    private final DashboardWebSocketParser parser;
    private DashboardWebSocket webSocket;

    public DashboardWebSocketApplication(PluginDataHolder holder) {
        parser = new DashboardWebSocketParser(this, holder);
    }

    @Override
    //TODO write auth with requestPacket
    public WebSocket createSocket(ProtocolHandler handler, HttpRequestPacket requestPacket, WebSocketListener... listeners) {
        DashboardWebSocket dashboardWebSocket = new DashboardWebSocket(handler, requestPacket, listeners);
        dashboardWebSocket.setBroadcaster(broadcaster);
        dashboardWebSocket.setParser(parser);
//        if(webSocket != null) {
//            webSocket.send("close");
//            webSocket.close();
//        }
        webSocket = dashboardWebSocket;
        return dashboardWebSocket;
    }

    @Override
    public void onMessage(WebSocket socket, String text) {
        socket.onMessage(text);
    }

    @Override
    public void onMessage(WebSocket socket, byte[] bytes) {
        socket.onMessage(bytes);
    }

    /**
     * Send message in websocket
     */
    public void sendMessage(String message) {
        webSocket.send(message);
    }

    /**
     * Send message signal to plugin
     */
    public void directMessage(String pluginName, String message) {
        wrappers.get(pluginName).onMessage(message);
    }

    /**
     * Send message in websocket from specific plugin
     */
    public void sendMessage(String pluginName, String message) {
        sendMessage("plugin:" + pluginName + ":" + message);
    }

    /**
     * Register new {@link DashboardWebsocketPlugin}
     *
     * @param plugin plugin
     * @return {@link DashboardWebsocketWrapper}, used for works with websocket
     */
    public DashboardWebsocketWrapper registerNewPlugin(DashboardWebsocketPlugin plugin) {
        DashboardWebsocketWrapper wrapper = new DashboardWebsocketWrapper(this, plugin);
        if(wrapper.getName().contains(":")) {
            throw new IllegalArgumentException("Name must contains no ':' character!");
        }
        wrappers.put(wrapper.getName(), wrapper);
        return wrapper;
    }

    public void setupControlAccessManagerBuilder(ControlAccessManagerBuilder builder) {
        builder.setDashboardWebSocketApplication(this);
    }
}
