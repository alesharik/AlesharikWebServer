package com.alesharik.webserver.api.server.control;

import com.alesharik.webserver.api.control.ControlWebSocketPlugin;
import com.alesharik.webserver.api.control.ControlWebSocketPluginWrapper;
import com.alesharik.webserver.control.websockets.control.WebSocketController;

import java.io.Serializable;

public class ControlWebSocketWrapper implements ControlWebSocketPluginWrapper {
    private final WebSocketController webSocketController;
    private final ControlWebSocketPlugin plugin;

    public ControlWebSocketWrapper(WebSocketController webSocketController, ControlWebSocketPlugin plugin) {
        this.webSocketController = webSocketController;
        this.plugin = plugin;
    }

    public String getName() {
        return plugin.getName();
    }

    @Override
    public ControlWebSocketPlugin getPlugin() {
        return plugin;
    }

    public void send(String message) {
        webSocketController.sendMessageFromPlugin(message, getName());
    }

    public <T extends Object & Serializable> void send(T object) {
        webSocketController.sendObjectFromPlugin(object, getName());
    }

    public void onMessage(String message) {
        plugin.onMessage(message);
    }

    public void onMessage(Object message) {
        plugin.onMessage(message);
    }
}
