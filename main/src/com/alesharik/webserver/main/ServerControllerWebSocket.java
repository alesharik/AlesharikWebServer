package com.alesharik.webserver.main;

import com.alesharik.webserver.control.ServerConsoleCommandHandler;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.Broadcaster;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocketListener;

import java.io.File;

//TODO delete this
public class ServerControllerWebSocket extends DefaultWebSocket {
    private String logpass;
    private ServerConsoleCommandHandler serverConsoleCommandHandler;

    public ServerControllerWebSocket(ProtocolHandler protocolHandler, HttpRequestPacket request, String logpass, WebSocketListener... listeners) {
        super(protocolHandler, request, listeners);
        this.logpass = logpass;
        this.serverConsoleCommandHandler = new ServerConsoleCommandHandler(new File(System.getProperty("user.dir")));
    }

    @Override
    public void onMessage(byte[] data) {

    }

    @Override
    public void onMessage(String text) {
        if(text.equals("Hello")) {
            this.send("Hello");
        } else if(text.contains("LogPass=")) {
            if(text.equals("LogPass=" + logpass)) {
                this.send("OK");
            }
        } else if(text.equals("getBaseInfo")) {
            try {
//                this.send("<pre>" + Helpers.getCompInfo() + "</pre>");
            } catch (Exception e) {
                this.send("<pre>" + e.getLocalizedMessage() + "</pre>");
            }
        } else if(text.contains("Command=")) {
            String[] parts = text.split("=");
            String command = parts[1];
            String params = parts[3];
            this.send(this.serverConsoleCommandHandler.handle(command, params));
        }
    }

    public void setBroadcaster(Broadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }
}
