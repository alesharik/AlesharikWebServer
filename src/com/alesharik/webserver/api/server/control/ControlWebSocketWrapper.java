package com.alesharik.webserver.api.server.control;

import com.alesharik.webserver.control.websockets.control.WebSocketController;

public class ControlWebSocketWrapper {
    private final WebSocketController webSocketController;
    private final ControlWebSocketPlugin plugin;

    public ControlWebSocketWrapper(WebSocketController webSocketController, ControlWebSocketPlugin plugin) {
        this.webSocketController = webSocketController;
        this.plugin = plugin;
    }

    public String getName() {
        return plugin.getName();
    }

    public void send(String message) {
        webSocketController.sendMessageFromPlugin(message, getName());
    }

    public void send(Object object) {
        webSocketController.sendObjectFromPlugin(object, getName());
    }

    public void onMessage(String message) {
        plugin.onMessage(message);
    }

    public void onMessage(Object message) {
        plugin.onMessage(message);
    }
}
