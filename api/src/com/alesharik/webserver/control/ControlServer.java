package com.alesharik.webserver.control;

import com.alesharik.webserver.configuration.Module;
import com.alesharik.webserver.control.dashboard.websocket.DashboardWebSocketPlugin;
import com.alesharik.webserver.control.dashboard.websocket.DashboardWebSocketPluginListener;

import javax.annotation.Nonnull;

public interface ControlServer extends Module {
    <T extends DashboardWebSocketPlugin> void addDashboardWebSocketPluginListener(@Nonnull String name, @Nonnull Class<T> pluginClazz, @Nonnull DashboardWebSocketPluginListener<T> listener);

    <T extends DashboardWebSocketPlugin> void removeDashboardWebSocketPluginListener(@Nonnull String name, @Nonnull DashboardWebSocketPluginListener<T> listener);
}
