package com.alesharik.webserver.api.server.dashboard;

/**
 * This interface used for give plugins access to Dashboard web socket.
 */
@Deprecated
public interface DashboardWebsocketPlugin {
    /**
     * Return unique name of this plugin.<br>
     * This name must be not null or empty!
     * WARNING!Name must contains no ':' character!
     */
    String getName();

    /**
     * This method called then socket receive message for this plugin.
     *
     * @param message message as String
     */
    void onMessage(String message);
}
