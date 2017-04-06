package com.alesharik.webserver.control.dashboard.websocket;

import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocketListener;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Dashboard Web Socket. Contains simply ping-like login protocol
 */
final class DashboardWebSocket extends DefaultWebSocket {
    private static final String HELLO_MSG = "hello";

    private final AtomicBoolean isLoggedIn;

    public DashboardWebSocket(ProtocolHandler protocolHandler, HttpRequestPacket request, WebSocketListener... listeners) {
        super(protocolHandler, request, listeners);
        isLoggedIn = new AtomicBoolean(false);
    }

    @Override
    public void onMessage(String text) {
        if(isLoggedIn.get()) {
            super.onMessage(text);
        } else if(HELLO_MSG.equals(text)) {
            isLoggedIn.set(true);
            send(HELLO_MSG);
        }
    }
}
