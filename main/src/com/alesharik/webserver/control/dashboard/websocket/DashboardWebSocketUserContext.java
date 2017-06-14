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

import com.alesharik.webserver.control.dashboard.DashboardDataHolder;
import com.alesharik.webserver.exceptions.PluginNotFoundException;
import com.alesharik.webserver.exceptions.WebSocketNotConnectedException;
import com.alesharik.webserver.logger.Prefixes;
import lombok.Setter;
import org.glassfish.grizzly.http.util.Base64Utils;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketListener;
import org.glassfish.grizzly.websockets.draft06.ClosingFrame;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Handle all messages form WebSocket.
 * Text message:<br>
 * name    command<br>   data
 * system :   gc<br>    : data
 * Byte message:<br>
 * byte type      name     command  Base64 encoded byte array<br>
 * byte    : messaging : command :          Base64<br>
 */
@Prefixes("[DashboardWebSocketUserContext]")
final class DashboardWebSocketUserContext implements WebSocketListener {
    private final AtomicBoolean isClosed;
    private final Map<String, DashboardWebSocketPlugin> plugins;
    private final Sender sender;
    private WebSocket webSocket;

    public DashboardWebSocketUserContext(@Nonnull Set<String> plugins, @Nonnull DashboardDataHolder dashboardDataHolder, @Nonnull DashboardWebSocketApplication.ListenerManager listenerManager) {
        this.isClosed = new AtomicBoolean(true);
        this.sender = new Sender();
        this.plugins = plugins.stream()
                .map(s -> {
                    try {
                        DashboardWebSocketPlugin dashboardWebSocketPlugin = DashboardWebSocketPluginManager.newInstanceForName(s, sender);
                        dashboardWebSocketPlugin.setDashboardDataHolder(dashboardDataHolder);
                        listenerManager.listen(dashboardWebSocketPlugin);
                        return dashboardWebSocketPlugin;
                    } catch (PluginNotFoundException e) {
                        System.err.println("DashboardWebSocketPlugin " + e.getPlugin() + " not found! Skipping...");
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toConcurrentMap(DashboardWebSocketPluginSubModule::getName, dashboardWebSocketPluginSubModule -> dashboardWebSocketPluginSubModule));

    }

    @Override
    public void onClose(WebSocket socket, DataFrame frame) {
        webSocket = null;
        sender.setWebSocket(null);
        isClosed.set(true);

        ClosingFrame closingFrame = (ClosingFrame) frame;
        if(closingFrame.getCode() == WebSocket.NORMAL_CLOSURE) {
            plugins.values().forEach(DashboardWebSocketPlugin::shutdown);
        } else {
            plugins.values().forEach(DashboardWebSocketPlugin::shutdownNow);
        }
    }

    @Override
    public void onConnect(WebSocket socket) {
        webSocket = socket;
        sender.setWebSocket(webSocket);
        isClosed.set(false);
        plugins.values().forEach(DashboardWebSocketPlugin::start);
    }

    @Override
    public void onMessage(WebSocket socket, String text) {
        if(text.startsWith("byte:")) {
            parseByteMessage(text);
        } else {
            parseStringMessage(text);
        }
    }

    private void parseByteMessage(String text) {
        String[] parts = text.split(":", 4);
        if(plugins.containsKey(parts[1])) {
            DashboardWebSocketPlugin plugin = plugins.get(parts[1]);
            if(plugin.isRunning()) {
                plugin.receive(parts[2], Base64Utils.decodeFast(parts[3]));
            } else {
                System.err.println("Plugin " + parts[1] + " not started!");
            }
        } else {
            System.out.println("Plugin " + parts[1] + " not found!");
        }
    }

    private void parseStringMessage(String text) {
        String[] parts = text.split(":", 3);
        if(parts.length == 2) {
            String[] n = new String[3];
            System.arraycopy(parts, 0, n, 0, parts.length);
            parts = n;
            parts[2] = "";
        }
        if(plugins.containsKey(parts[0])) {
            DashboardWebSocketPlugin plugin = plugins.get(parts[0]);
            if(plugin.isRunning()) {
                plugin.receive(parts[1], parts[2]);
            } else {
                System.err.println("Plugin " + parts[0] + " not started!");
            }
        } else {
            System.out.println("Plugin " + parts[0] + " not found!");
        }
    }

    @Override
    public void onMessage(WebSocket socket, byte[] bytes) {
        System.err.println("Byte message not expected!");
    }

    @Override
    public void onPing(WebSocket socket, byte[] bytes) {
        webSocket.sendPong(bytes);
    }

    @Override
    public void onPong(WebSocket socket, byte[] bytes) {
    }

    @Override
    public void onFragment(WebSocket socket, String fragment, boolean last) {
        System.err.println("Fragment message not expected!");
    }

    @Override
    public void onFragment(WebSocket socket, byte[] fragment, boolean last) {
        System.err.println("Fragment message not expected!");
    }

    public boolean isClosed() {
        return isClosed.get();
    }

    public void close() {
        if(!isClosed()) {
            webSocket.close();
            isClosed.set(true);
        }
    }

    private static final class Sender implements WebSocketSender {
        @Setter
        private WebSocket webSocket;

        public Sender() {
            webSocket = null;
        }

        @Override
        public void send(@Nonnull String pluginName, @Nonnull String command, @Nonnull String data) {
            if(webSocket == null || !webSocket.isConnected()) {
                throw new WebSocketNotConnectedException();
            }
            webSocket.send(pluginName + ':' + command + ':' + data);
        }

        @Override
        public void send(@Nonnull String pluginName, @Nonnull String command, byte[] data) {
            if(webSocket == null || !webSocket.isConnected()) {
                throw new WebSocketNotConnectedException();
            }
            webSocket.send(pluginName + ':' + command + ':' + Base64Utils.encodeToString(data, false));
        }
    }
}
