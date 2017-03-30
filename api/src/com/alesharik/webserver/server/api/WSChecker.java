package com.alesharik.webserver.server.api;

/**
 * Check if WSApplication can be registered in {@link org.glassfish.grizzly.websockets.WebSocketEngine}
 */
public class WSChecker {
    /**
     * Return true if WebSocket can be registered
     */
    public boolean enabled() {
        return true;
    }
}
