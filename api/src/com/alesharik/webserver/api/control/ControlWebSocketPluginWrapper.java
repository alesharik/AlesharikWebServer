package com.alesharik.webserver.api.control;

import java.io.Serializable;

/**
 * Class, implementing this interface, used only for send messages from ControlWebSocket
 */
@Deprecated
public interface ControlWebSocketPluginWrapper {
    /**
     * Return name of {@link ControlWebSocketPlugin}
     */
    String getName();

    /**
     * Return the plugin, used for process messages from ControlWebSocket
     */
    ControlWebSocketPlugin getPlugin();

    /**
     * Send text message to peer
     *
     * @param message the message
     * @throws NullPointerException if message is null
     */
    void send(String message);

    /**
     * Send object to peer. Object serialized with one-nio serialization
     *
     * @param object the object
     * @param <T>    the object type
     */
    <T extends Object & Serializable> void send(T object);
}
