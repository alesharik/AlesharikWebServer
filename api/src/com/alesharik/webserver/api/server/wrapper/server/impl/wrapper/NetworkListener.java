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

package com.alesharik.webserver.api.server.wrapper.server.impl.wrapper;

import com.alesharik.webserver.api.name.Named;
import com.alesharik.webserver.api.server.wrapper.PortRange;
import com.alesharik.webserver.api.server.wrapper.server.SocketProvider;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import static com.alesharik.webserver.configuration.XmlHelper.*;

@Named("network-listener")
public class NetworkListener implements com.alesharik.webserver.api.server.wrapper.server.ServerSocketWrapper {
    private final ServerSocketConfig config = new ServerSocketConfig();
    private ServerSocketChannel serverSocket;

    @Override
    public void registerSelector(Selector selector) {
        if(serverSocket == null)
            return;

        try {
            serverSocket.register(selector, SelectionKey.OP_ACCEPT, new SocketProvider.ServerSocketWrapper(serverSocket, SocketManager.DEFAULT));
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return "network-listener";
    }

    @Override
    public void start() {
        try {
            serverSocket = config.newSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdownNow() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRunning() {
        return serverSocket != null;
    }

    @Override
    public void parseConfig(@Nullable Element element) {
        if(element == null)
            throw new ConfigurationParseError();
        config.parse(element);
    }

    private static final class ServerSocketConfig {
        private PortRange range;
        private String host;
        private int backlog;

        private boolean performance = false;
        private int connectionTime;
        private int latency;
        private int bandwidth;

        private int receiveBufferSize;
        private boolean reuseAddress = false;
        private int soTimeout;

        public void parse(Element element) {
            range = PortRange.getPortsFromXML(element);
            host = getString("host", element, true);
            backlog = getInteger("backlog", element, false, 0);

            Element performance = getXmlElement("performance", element, false);
            if(performance != null) {
                this.performance = true;
                connectionTime = getInteger("connection", element, true, -1);
                latency = getInteger("latency", element, true, -1);
                bandwidth = getInteger("bandwidth", element, true, -1);
            } else {
                this.performance = false;
            }

            receiveBufferSize = getInteger("receiveBuffer", element, false, -1);
            reuseAddress = Boolean.parseBoolean(getString("reuseAddress", element, false));
            soTimeout = getInteger("timeout", element, false, -1);
        }

        public ServerSocketChannel newSocket() throws IOException {
            int firstPort = range.getLower();
            InetAddress byName = InetAddress.getByName(host);
            ServerSocketChannel channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            ServerSocket serverSocket = channel.socket();
            if(firstPort <= range.getUpper()) {
                for(int i = firstPort; i <= range.getUpper(); i++) {
                    serverSocket.bind(new InetSocketAddress(byName, i), backlog);
                }
            }
            if(performance)
                serverSocket.setPerformancePreferences(connectionTime, latency, bandwidth);
            if(receiveBufferSize != -1)
                serverSocket.setReceiveBufferSize(receiveBufferSize);
            serverSocket.setReuseAddress(reuseAddress);
            if(soTimeout != -1)
                serverSocket.setSoTimeout(soTimeout);
            return channel;
        }
    }
}
