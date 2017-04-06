package com.alesharik.webserver.control.dashboard.websocket;

import com.alesharik.webserver.configuration.SubModule;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SubModule implementation for DashboardWebSocketPlugin
 */
public abstract class DashboardWebSocketPluginSubModule implements SubModule {
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * Return plugin name
     */
    @Override
    public abstract String getName();

    @Override
    public void start() {
        isRunning.set(true);
    }

    /**
     * Executes on abnormal webSocket close code
     */
    @Override
    public void shutdownNow() {
        isRunning.set(false);
    }

    /**
     * Executes on normal webSocket close code
     */
    @Override
    public void shutdown() {
        isRunning.set(false);
    }

    @Override
    public boolean isRunning() {
        return isRunning.get();
    }
}
