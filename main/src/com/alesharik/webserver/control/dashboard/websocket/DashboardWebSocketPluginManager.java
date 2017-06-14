/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.alesharik.webserver.control.dashboard.websocket;

import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenClass;
import com.alesharik.webserver.exceptions.PluginNotFoundException;
import lombok.SneakyThrows;
import one.nio.mgt.Management;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This manager holds all plugin {@link Class}es and {@link DashboardWebSocketPluginManagerMXBean} implementation
 */
@ClassPathScanner
public final class DashboardWebSocketPluginManager {
    private static final Map<String, Class<?>> plugins = new ConcurrentHashMap<>();
    private static final DashboardWebSocketPluginManagerMXBean mxBean = new DashboardWebSocketPluginManagerMXBean() {
        @Override
        public int pluginCount() {
            return plugins.size();
        }

        @Override
        public Collection<Class<?>> pluginClasses() {
            return Collections.unmodifiableCollection(plugins.values());
        }
    };

    static {
        Management.registerMXBean(mxBean, DashboardWebSocketPluginManagerMXBean.class, "com.alesharik.webserver..control.dashboard.websocket:type=DashboardWebSocketPluginManager");
    }

    private DashboardWebSocketPluginManager() {
    }

    @ListenClass(DashboardWebSocketPlugin.class)
    public static void listenPlugin(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getConstructor(WebSocketSender.class);
            constructor.setAccessible(true);
            DashboardWebSocketPlugin dashboardWebSocketPlugin = (DashboardWebSocketPlugin) constructor.newInstance((Object) null);
            plugins.put(dashboardWebSocketPlugin.getName(), clazz);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return new instance from plugin name
     *
     * @param name            name of plugin
     * @param webSocketSender sender for webSocket
     * @return plugin instance
     * @throws PluginNotFoundException is plugin not found
     * @throws RuntimeException        if {@link InvocationTargetException} or {@link InstantiationException} was thrown
     */
    @SneakyThrows
    @Nonnull
    public static DashboardWebSocketPlugin newInstanceForName(@Nonnull String name, @Nonnull WebSocketSender webSocketSender) {
        try {
            if(!plugins.containsKey(name)) {
                throw new PluginNotFoundException(name);
            }
            Constructor<?> constructor = plugins.get(name).getConstructor(WebSocketSender.class);
            constructor.setAccessible(true);
            return (DashboardWebSocketPlugin) constructor.newInstance(webSocketSender);
        } catch (InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
