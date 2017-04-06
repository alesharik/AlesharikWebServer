package com.alesharik.webserver.control.dashboard.websocket;

import javax.annotation.Nonnull;

/**
 * Send things to WebSocket
 */
public interface WebSocketSender {
    /**
     * Send text message
     *
     * @param data message
     */
    void send(@Nonnull String pluginName, @Nonnull String command, @Nonnull String data);

    /**
     * Send <code>byte[]</code> message
     *
     * @param data message
     */
    void send(@Nonnull String pluginName, @Nonnull String command, byte[] data);
}
