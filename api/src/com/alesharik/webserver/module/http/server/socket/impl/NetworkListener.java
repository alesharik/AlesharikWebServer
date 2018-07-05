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

package com.alesharik.webserver.module.http.server.socket.impl;

import com.alesharik.webserver.api.name.Named;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObject;
import com.alesharik.webserver.extension.module.ConfigurationError;
import com.alesharik.webserver.extension.module.Shutdown;
import com.alesharik.webserver.extension.module.ShutdownNow;
import com.alesharik.webserver.extension.module.Start;
import com.alesharik.webserver.extension.module.layer.SubModule;
import com.alesharik.webserver.extension.module.meta.ScriptElementConverter;
import com.alesharik.webserver.module.http.PortRange;
import com.alesharik.webserver.module.http.server.CloseSocketException;
import com.alesharik.webserver.module.http.server.socket.ServerSocketWrapper;
import com.alesharik.webserver.module.http.server.socket.SocketWriter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static com.alesharik.webserver.extension.module.ConfigurationUtils.*;

@Named("network-listener")
@SubModule("network-listener")
public class NetworkListener implements ServerSocketWrapper {
    private final ServerSocketConfig config = new ServerSocketConfig();
    private ServerSocketChannel serverSocket;

    @Override
    public void parseConfig(@Nullable ConfigurationObject element, ScriptElementConverter converter) {
        config.parse(element, converter);
    }

    @Override
    public ServerSocketChannel getChannel() {
        return serverSocket;
    }

    @Override
    public SocketManager getSocketManager() {
        return SocketManagerImpl.INSTANCE;
    }

    @Start
    public void start() {
        try {
            serverSocket = config.newSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ShutdownNow
    public void shutdownNow() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Shutdown
    public void shutdown() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final class SocketManagerImpl implements SocketManager {
        private static final SocketManager INSTANCE = new SocketManagerImpl();

        @Override
        public void init(SocketChannel socketChannel) {

        }

        @Override
        public void close(SocketChannel socketChannel) {

        }

        @Override
        public void read(SocketChannel socketChannel, ByteBuffer byteBuffer) throws IOException {
            if(socketChannel.read(byteBuffer) == -1)
                throw new CloseSocketException();
        }

        @Override
        public void write(SocketChannel socketChannel, ByteBuffer data, SocketWriter writer) {
            writer.write(socketChannel, data);
        }
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

        public void parse(ConfigurationObject element, ScriptElementConverter converter) {
            if(element == null)
                throw new ConfigurationError("Network listener must be configured!");
            ConfigurationElement portElement = element.getElement("port");
            if(portElement == null)
                throw new ConfigurationError("Port can't be null!");
            range = PortRange.Deserializer.deserializeObject(portElement, converter);

            host = getString("host", element.getElement("host"), converter)
                    .orElseThrow(() -> new ConfigurationError("Host element must be set!"));

            backlog = getInteger("backlog", element.getElement("backlog"), converter)
                    .orElse(0);

            performance = false;
            getObject("performance", element.getElement("performance"), converter)
                    .ifPresent(object -> {
                        performance = true;
                        connectionTime = getInteger("connection", object.getElement("connection"), converter)
                                .orElse(-1);
                        latency = getInteger("latency", object.getElement("latency"), converter)
                                .orElse(-1);
                        bandwidth = getInteger("bandwidth", object.getElement("bandwidth"), converter)
                                .orElse(-1);
                    });

            receiveBufferSize = getInteger("receive-buffer", element.getElement("receive-buffer"), converter)
                    .orElse(-1);
            soTimeout = getInteger("timeout", element.getElement("timeout"), converter)
                    .orElse(-1);
            reuseAddress = getBoolean("reuse-address", element.getElement("reuse-address"), converter)
                    .orElse(false);
        }

        public ServerSocketChannel newSocket() throws IOException {
            int firstPort = range.getLower();
            InetAddress byName = InetAddress.getByName(host);
            ServerSocketChannel channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            ServerSocket serverSocket = channel.socket();
            if(firstPort <= range.getUpper())
                for(int i = firstPort; i <= range.getUpper(); i++)
                    serverSocket.bind(new InetSocketAddress(byName, i), backlog);
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
