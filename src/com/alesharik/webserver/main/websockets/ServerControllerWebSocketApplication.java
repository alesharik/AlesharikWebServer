package com.alesharik.webserver.main.websockets;

import com.alesharik.webserver.main.FileManager;
import com.alesharik.webserver.main.server.WebServer;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.Broadcaster;
import org.glassfish.grizzly.websockets.OptimizedBroadcaster;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketListener;

/**
 * This class represent {@link WebSocketApplication} for control {@link WebServer} with WebSockets
 */
public final class ServerControllerWebSocketApplication extends WebSocketApplication {
    private final Broadcaster broadcaster = new OptimizedBroadcaster();
    private final FileManager fileManager;
    private final WebServer webServer;

    public ServerControllerWebSocketApplication(FileManager fileManager, WebServer webServer) {
        this.fileManager = fileManager;
        this.webServer = webServer;
    }

    @Override
    public WebSocket createSocket(ProtocolHandler handler, HttpRequestPacket requestPacket, WebSocketListener... listeners) {
        ServerControllerWebSocket socket = new ServerControllerWebSocket(handler, requestPacket, listeners);
        socket.setBroadcaster(broadcaster);
        socket.setFileManager(fileManager);
        socket.setWebServer(webServer);
        return socket;
    }

    @Override
    public void onMessage(WebSocket socket, byte[] bytes) {
        socket.onMessage(new String(bytes));
    }

    @Override
    public void onMessage(WebSocket socket, String text) {
        socket.onMessage(text);
    }
}
