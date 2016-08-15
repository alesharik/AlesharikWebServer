package com.alesharik.webserver.main.websockets;

import com.alesharik.webserver.main.FileManager;
import com.alesharik.webserver.main.Helpers;
import com.alesharik.webserver.main.server.WebServer;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.util.Base64Utils;
import org.glassfish.grizzly.websockets.Broadcaster;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocketListener;

/**
 * This class represent WebSocket, which control the server
 */
public final class ServerControllerWebSocket extends DefaultWebSocket {
    private FileManager fileManager;
    private WebServer webServer;
    private boolean isAuthorized = false;

    public ServerControllerWebSocket(ProtocolHandler protocolHandler, HttpRequestPacket request, WebSocketListener... listeners) {
        super(protocolHandler, request, listeners);
    }

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void setWebServer(WebServer webServer) {
        this.webServer = webServer;
    }

    public void setBroadcaster(Broadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @Override
    public void onMessage(String message) {
        if(message.equals("Hello")) {
            send("Hello");
        } else if(message.startsWith("LogPass=")) {
            if(webServer.isLogPassValid(message.substring(8))) {
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
        switch (message) {
            case "getComputerInfo":
                send(Base64Utils.encodeToString(Helpers.getCompInfo().getBytes(), false));
                break;
        }
    }
}
