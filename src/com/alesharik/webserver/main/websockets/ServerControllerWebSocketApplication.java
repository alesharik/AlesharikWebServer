package com.alesharik.webserver.main.websockets;

import com.alesharik.webserver.main.FileManager;
import com.alesharik.webserver.main.server.MainServer;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.Broadcaster;
import org.glassfish.grizzly.websockets.OptimizedBroadcaster;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketListener;

/**
 * This class represent {@link WebSocketApplication} for control {@link MainServer} with WebSockets
 */
public final class ServerControllerWebSocketApplication extends WebSocketApplication {
    private final Broadcaster broadcaster = new OptimizedBroadcaster();
    private final FileManager fileManager;
    private final MainServer mainServer;

    public ServerControllerWebSocketApplication(FileManager fileManager, MainServer mainServer) {
        this.fileManager = fileManager;
        this.mainServer = mainServer;
    }

    @Override
    public WebSocket createSocket(ProtocolHandler handler, HttpRequestPacket requestPacket, WebSocketListener... listeners) {
        ServerControllerWebSocket socket = new ServerControllerWebSocket(handler, requestPacket, listeners);
        socket.setBroadcaster(broadcaster);
        socket.setFileManager(fileManager);
        socket.setMainServer(mainServer);
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
