package com.alesharik.webserver.server.api;

/**
 * Check if WSApplication can be registered in {@link org.glassfish.grizzly.websockets.WebSocketEngine}
 */
public abstract class WSChecker {
    /**
     * Return true if WebSocket can be registered
     */
    public abstract boolean enabled();

    public static final class Disabled extends WSChecker {

        @Override
        public boolean enabled() {
            return false;
        }
    }

    public static final class Enabled extends WSChecker {

        @Override
        public boolean enabled() {
            return true;
        }
    }
}
