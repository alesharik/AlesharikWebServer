package com.alesharik.webserver.control.dashboard.websocket;

import com.alesharik.webserver.control.dashboard.DashboardDataHolder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * DashboardWebSocket plugin used for communicate with dashboard through WebSocket
 */
public abstract class DashboardWebSocketPlugin extends DashboardWebSocketPluginSubModule {
    private final WebSocketSender sender;
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PACKAGE)
    private DashboardDataHolder dashboardDataHolder;

    public DashboardWebSocketPlugin(WebSocketSender sender) {
        this.sender = sender;
    }

    /**
     * On text message received
     */
    public void receive(@Nonnull String command, @Nonnull String text) {
    }

    /**
     * On <code>byte[]</code> message received
     */
    public void receive(@Nonnull String command, byte[] data) {
    }

    /**
     * Send command from this plugin to Dashboard
     *
     * @param command command
     * @param data    data for command
     */
    public final void send(String command, String data) {
        sender.send(getName(), command, data);
    }

    /**
     * Send command from this plugin to Dashboard
     *
     * @param command command
     * @param data    data for command.
     */
    public final void send(String command, byte[] data) {
        sender.send(getName(), command, data);
    }
}
