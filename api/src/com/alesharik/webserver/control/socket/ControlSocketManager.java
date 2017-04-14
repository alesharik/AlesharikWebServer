package com.alesharik.webserver.control.socket;

/**
 * This manager use for register and unregister {@link ControlSocketHandlerFactory}s and work with sockets
 */
public interface ControlSocketManager {
    void registerNewControlSocketHandlerFactory(String name, ControlSocketHandlerFactory factory);

    void unregisterControlSocketHandlerFactory(String name, ControlSocketHandlerFactory factory);
}
