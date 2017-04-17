package com.alesharik.webserver.api.control.messaging;

/**
 * Server side interface. Handle client messages. Auto registered in server
 * @implNote You need to annotate message class by @{@link WireControlSocketMessage} annotation!
 */
public interface ControlSocketMessageHandler<T extends ControlSocketMessage> {
    /**
     * Handle message
     *
     * @param message    the message
     * @param connection connection, that receive message
     */
    void handleMessage(T message, ControlSocketServerConnection connection);
}
