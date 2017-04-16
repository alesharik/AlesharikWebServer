package com.alesharik.webserver.api.control.messaging;

/**
 * Server side interface. Handle client message
 */
public interface ControlSocketMessageHandler<T extends ControlSocketMessage> {
    void handleMessage(T message, ControlSocketServerConnection connection);
}
