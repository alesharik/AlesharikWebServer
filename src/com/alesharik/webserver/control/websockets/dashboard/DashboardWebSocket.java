package com.alesharik.webserver.control.websockets.dashboard;

import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.Broadcaster;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocketListener;

/**
 * This WebSocket used in dashboard for data transfer
 */
public final class DashboardWebSocket extends DefaultWebSocket {
    private DashboardWebSocketParser parser;

    public DashboardWebSocket(ProtocolHandler protocolHandler, HttpRequestPacket request, WebSocketListener... listeners) {
        super(protocolHandler, request, listeners);
    }

    public void setBroadcaster(Broadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    public void setParser(DashboardWebSocketParser parser) {
        this.parser = parser;
    }

    @Override
    public void onMessage(String text) {
        parser.parse(text);
    }
}
