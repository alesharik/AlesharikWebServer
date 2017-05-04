package com.alesharik.webserver.control.dashboard.websocket;

/**
 * @param <T> plugin
 */
@FunctionalInterface
public interface DashboardWebSocketPluginListener<T> {
    void onCreate(T plugin);
}
