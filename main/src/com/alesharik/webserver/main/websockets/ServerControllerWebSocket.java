package com.alesharik.webserver.main.websockets;

import com.alesharik.webserver.main.server.MainServer;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.Broadcaster;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocketListener;

/**
 * This class represent WebSocket, which control the server
 */
@Deprecated
//TODO rewrite this
public final class ServerControllerWebSocket extends DefaultWebSocket {
    private ServerControllerWebSocketApplication application;
    private MainServer mainServer;
    private boolean isAuthorized = false;

    public ServerControllerWebSocket(ProtocolHandler protocolHandler, HttpRequestPacket request, WebSocketListener... listeners) {
        super(protocolHandler, request, listeners);
    }

    public void setMainServer(MainServer mainServer) {
        this.mainServer = mainServer;
    }

    public void setBroadcaster(Broadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    public void setApplication(ServerControllerWebSocketApplication application) {
        this.application = application;
    }

    @Override
    public void onMessage(String message) {
        if(message.equals("Hello")) {
            send("Hello");
        } else if(message.startsWith("LogPass=")) {
            if(mainServer.isLogPassValid(message.substring(8))) {
                isAuthorized = true;
                send("OK");
            } else {
                close();
            }
        } else if(isAuthorized) {
            processAuthorizedMessage(message);
        } else {
            processAnonymousMessage(message);
        }
    }

    private void processAnonymousMessage(String message) {
        switch (message) {
            case "closeSession":
                close();
                break;
        }
    }

    private void processAuthorizedMessage(String message) {
        String[] parts = message.split(":");
        switch (parts[0]) {
            case "getComputerInfo":
//                send(Base64Utils.encodeToString(Helpers.getCompInfo().getBytes(Charsets.UTF8_CHARSET), false));
                break;
            case "plugin":
                processPluginMessage(parts);
                break;
            case "system":

        }
    }

    private void processPluginMessage(String[] parts) {
        application.directMessage(parts[1], parts[2]);
    }
}
