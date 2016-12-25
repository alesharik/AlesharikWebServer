package com.alesharik.webserver.main.websockets;

import com.alesharik.webserver.api.SerialRepository;
import com.alesharik.webserver.api.server.control.ControlWebSocketWrapper;
import com.alesharik.webserver.main.server.MainServer;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.utils.Charsets;
import org.glassfish.grizzly.websockets.Broadcaster;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.OptimizedBroadcaster;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketListener;

import java.io.Serializable;
import java.util.HashMap;

/**
 * This class represent {@link WebSocketApplication} for control {@link MainServer} with WebSockets
 */
public final class ServerControllerWebSocketApplication extends WebSocketApplication {
    private final HashMap<String, ControlWebSocketWrapper> wrappers = new HashMap<>();
    private final Broadcaster broadcaster = new OptimizedBroadcaster();
    private final MainServer mainServer;
    private ServerControllerWebSocket webSocket;

    public ServerControllerWebSocketApplication(MainServer mainServer) {
        this.mainServer = mainServer;
    }

    @Override
    public WebSocket createSocket(ProtocolHandler handler, HttpRequestPacket requestPacket, WebSocketListener... listeners) {
        ServerControllerWebSocket socket = new ServerControllerWebSocket(handler, requestPacket, listeners);
        socket.setBroadcaster(broadcaster);
        socket.setApplication(this);
        socket.setMainServer(mainServer);
        webSocket = socket;
        return socket;
    }

    @Override
    public void onClose(WebSocket socket, DataFrame frame) {
        webSocket = null;
    }

    @Override
    public void onMessage(WebSocket socket, byte[] bytes) {
        socket.onMessage(new String(bytes, Charsets.UTF8_CHARSET));
    }

    @Override
    public void onMessage(WebSocket socket, String text) {
        socket.onMessage(text);
    }

    /**
     * Send message signal to plugin
     */
    public void directMessage(String pluginName, String message) {
        wrappers.get(pluginName).onMessage(message);
    }

    public void sendObjectFromPlugin(Serializable object, String pluginName) {
        sendMessage("plugin:" + pluginName + ":object:" + SerialRepository.serialize(object));
    }

    public void requestSerializer(long uid) {
        sendMessage("getSerializer:" + uid);
//        SerialRepository.addSerializedSerializer(messageParser.waitMessage());
    }

    public void sendMessage(String message) {
        if(webSocket == null) {
            throw new RuntimeException("Connection is not established!");
        }
        webSocket.send(message);
    }

    /**
     * Send message in websocket from specific plugin
     */
    public void sendMessage(String pluginName, String message) {
        sendMessage("plugin:" + pluginName + ":" + message);
    }
}
