package com.alesharik.webserver.control.dashboard.websocket;

import com.alesharik.webserver.control.dashboard.DashboardDataHolder;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.server.api.WSApplication;
import com.alesharik.webserver.server.api.WSChecker;
import org.apache.commons.lang3.tuple.Pair;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketListener;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Always disabled for auto registration. Must be registered by control server
 */
@WSApplication(value = "/dashboard", checker = WSChecker.Disabled.class)
public class DashboardWebSocketApplication extends WebSocketApplication {
    private final Set<String> plugins;
    private final DashboardDataHolder dashboardDataHolder;
    private final ListenerManager listenerManager;

    public DashboardWebSocketApplication(Set<String> plugins, DashboardDataHolder dashboardDataHolder) {
        this.plugins = plugins;
        this.dashboardDataHolder = dashboardDataHolder;
        this.listenerManager = new ListenerManager();
        plugins.add("menu");
        plugins.add("menuPlugins");
        plugins.add("currentCompInfo");
        plugins.add("system");
    }

    @Override
    public WebSocket createSocket(ProtocolHandler handler, HttpRequestPacket requestPacket, WebSocketListener... listeners) {
        DashboardWebSocket dashboardWebSocket = new DashboardWebSocket(handler, requestPacket, listeners);
        dashboardWebSocket.add(new DashboardWebSocketUserContext(plugins, dashboardDataHolder, listenerManager));
        return dashboardWebSocket;
    }

    public <T extends DashboardWebSocketPlugin> void addDashboardWebSocketPluginListener(@Nonnull String name, @Nonnull Class<T> clazz, @Nonnull DashboardWebSocketPluginListener<T> listener) {
        listenerManager.addListener(name, clazz, listener);
        ;
    }

    public <T extends DashboardWebSocketPlugin> void removeDashboardWebSocketPluginListener(@Nonnull String name, @Nonnull DashboardWebSocketPluginListener<T> listener) {
        listenerManager.removeListener(name, listener);
    }

    @Prefixes({"[DashboardWebSocket]", "[DashboardWebSocketApplication]", "[Listener]"})
    static final class ListenerManager {
        private final Map<String, List<Pair<Class<?>, DashboardWebSocketPluginListener>>> listeners;

        public ListenerManager() {
            listeners = new ConcurrentHashMap<>();
        }

        <T extends DashboardWebSocketPlugin> void addListener(@Nonnull String name, @Nonnull Class<T> clazz, @Nonnull DashboardWebSocketPluginListener<T> listener) {
            List<Pair<Class<?>, DashboardWebSocketPluginListener>> listenerList;
            if(!listeners.containsKey(name)) {
                listenerList = new CopyOnWriteArrayList<>();
                listeners.put(name, listenerList);
            } else {
                listenerList = listeners.get(name);
            }
            listenerList.add(Pair.of(clazz, listener));
        }

        <T extends DashboardWebSocketPlugin> void removeListener(@Nonnull String name, @Nonnull DashboardWebSocketPluginListener<T> listener) {
            if(listeners.containsKey(name)) {
                List<Pair<Class<?>, DashboardWebSocketPluginListener>> listenerList = listeners.get(name);
                listenerList.removeIf(next -> next.getRight().equals(listener));
            }
        }

        @SuppressWarnings("unchecked")
        void listen(@Nonnull DashboardWebSocketPlugin plugin) {
            List<Pair<Class<?>, DashboardWebSocketPluginListener>> listenerList = listeners.get(plugin.getName());
            if(listenerList != null) {
                listenerList.forEach(pair -> {
                    try {
                        pair.getRight().onCreate(pair.getLeft().cast(plugin));
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}
