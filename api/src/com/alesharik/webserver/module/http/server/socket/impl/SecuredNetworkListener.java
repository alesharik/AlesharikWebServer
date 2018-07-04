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

import com.alesharik.webserver.api.cache.object.CachedObjectFactory;
import com.alesharik.webserver.api.cache.object.Recyclable;
import com.alesharik.webserver.api.cache.object.SmartCachedObjectFactory;
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
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.alesharik.webserver.extension.module.ConfigurationUtils.*;

//FIXME refactor
@Named("secured-network-listener")
@SubModule("secured-network-listener")
public class SecuredNetworkListener implements ServerSocketWrapper {
    private final SecuredServerSocketConfig config = new SecuredServerSocketConfig();
    private ServerSocketChannel serverSocket;
    private SocketManagerImpl manager;

    @Start
    public void start() {
        try {
            serverSocket = config.newSocket();
            manager = new SocketManagerImpl(config.sslContext);
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    @Shutdown
    public void shutdownNow() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ShutdownNow
    public void shutdown() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void parseConfig(@Nullable ConfigurationObject element, @Nonnull ScriptElementConverter scriptElementConverter) {
        config.parse(element, scriptElementConverter);
    }

    @Override
    public ServerSocketChannel getChannel() {
        return serverSocket;
    }

    @Override
    public SocketManager getSocketManager() {
        return manager;
    }

    private static final class SecuredServerSocketConfig {
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
        private SSLContext sslContext;

        public void parse(ConfigurationObject element, ScriptElementConverter converter) {
            if(element == null)
                throw new ConfigurationError("SecuredNetworkListener must be configured!");

            ConfigurationElement portElement = element.getElement("port");
            if(portElement == null)
                throw new ConfigurationError("Port can't be null!");
            range = PortRange.Deserializer.deserializeObject(element, converter);

            host = getString("host", element.getElement("host"), converter)
                    .orElseThrow(() -> new ConfigurationError("Host can't be null!"));
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
            reuseAddress = getBoolean("reuse-address", element.getElement("reuse-address"), converter)
                    .orElse(false);
            soTimeout = getInteger("timeout", element.getElement("timeout"), converter)
                    .orElse(-1);

            ConfigurationObject sslConfig = getObject("ssl", element.getElement("ssl"), converter)
                    .orElseThrow(() -> new ConfigurationError("ssl can't be null!"));

            ConfigurationObject keystoreConfig = getObject("keystore", sslConfig.getElement("keystore"), converter)
                    .orElseThrow(() -> new ConfigurationError("keystore can't be null!"));

            String keyStoreType = getString("type", keystoreConfig.getElement("type"), converter)
                    .orElseThrow(() -> new ConfigurationError("keystore type can't be null!"));
            File keyStoreFile = new File(getString("store", keystoreConfig.getElement("store"), converter)
                    .orElseThrow(() -> new ConfigurationError("keystore store can't be null!")));
            String keyStorePassword = getString("password", keystoreConfig.getElement("password"), converter)
                    .orElseThrow(() -> new ConfigurationError("keystore password can't be null!"));

            ConfigurationObject trustoreConfig = getObject("trustore", sslConfig.getElement("trustore"), converter)
                    .orElseThrow(() -> new ConfigurationError("trustore can't be null!"));

            String trustoreType = getString("type", trustoreConfig.getElement("type"), converter)
                    .orElseThrow(() -> new ConfigurationError("trustore type can't be null!"));
            File trustoreFile = new File(getString("store", trustoreConfig.getElement("store"), converter)
                    .orElseThrow(() -> new ConfigurationError("trustore store can't be null!")));
            String trustorePassword = getString("password", trustoreConfig.getElement("password"), converter)
                    .orElseThrow(() -> new ConfigurationError("trustore password can't be null!"));

            try {
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(new FileInputStream(keyStoreFile), keyStorePassword.toCharArray());

                KeyStore trustore = KeyStore.getInstance(trustoreType);
                trustore.load(new FileInputStream(trustoreFile), trustorePassword.toCharArray());

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
                keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
                trustManagerFactory.init(trustore);

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

                this.sslContext = sslContext;
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException | KeyManagementException e) {
                throw new ConfigurationError(e);
            }
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
            return serverSocket.getChannel();
        }
    }


    @RequiredArgsConstructor
    private static final class SocketManagerImpl implements SocketManager {
        private final SSLContext sslContext;
        /**
         * SocketChannel: (value == null - insecure, SSLEngine - secure)
         */
        private final Map<Socket, EngineWrapper> engines = new ConcurrentHashMap<>();

        @Override
        public void init(SocketChannel socketChannel) {
            SSLEngine engine = null;
            try {

                engine = sslContext.createSSLEngine(((InetSocketAddress) socketChannel.getRemoteAddress()).getHostName(), ((InetSocketAddress) socketChannel.getRemoteAddress()).getPort());
                engine.setUseClientMode(false);
                engine.setWantClientAuth(true);
                EngineWrapper value = new EngineWrapper(socketChannel, engine);

                value.doHandshake();
                if(value.isSecure())
                    engines.put(socketChannel.socket(), value);
            } catch (ClosedChannelException e) {
                try {
                    socketChannel.close();
                } catch (IOException e1) {
                    //Ok
                }
            } catch (IOException e) {
                if(engine == null) {
                    e.printStackTrace();
                    return;
                }
                try {
                    System.err.println("SSL connection failed! Remote host " + engine.getPeerHost() + ':' + engine.getPeerPort());
                    socketChannel.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }

        @Override
        public void close(SocketChannel socketChannel) throws IOException {
            engines.remove(socketChannel.socket());
        }

        @Override
        public void read(SocketChannel socketChannel, ByteBuffer buf) {
            if(!isSecure(socketChannel))
                return;

            try {
                EngineWrapper wrapper = engines.get(socketChannel.socket());
                ByteBuffer byteBuffer = wrapper.allocate(EngineWrapper.BufferType.APPLICATION);

                EngineWrapper.Result result;
                do {
                    result = wrapper.receiveData(byteBuffer);
                    buf.put(result.buffer);
                    byteBuffer = result.buffer;
                    byteBuffer.clear();
                } while(result.result.bytesProduced() == byteBuffer.capacity());
                EngineWrapper.Result.delete(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void write(SocketChannel socketChannel, ByteBuffer data, SocketWriter writer) {
            if(!isSecure(socketChannel)) {
                int nSend = 0;
                int count = 0;
                try {
                    while((nSend += socketChannel.write(data)) < data.remaining()) {
                        count++;
                        if(count > 10) {
                            System.err.println("SocketChannel to " + socketChannel.socket().getRemoteSocketAddress().toString() + " has problems! Try #" + count);
                        }
                    }
                    socketChannel.socket().getOutputStream().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            EngineWrapper engineWrapper = engines.get(socketChannel.socket());
            try {
                EngineWrapper.Result.delete(engineWrapper.sendData(data));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean isSecure(SocketChannel socketChannel) {
            return engines.get(socketChannel.socket()) != null;
        }

        static class EngineWrapper {
            private final SocketChannel socketChannel;
            private final SSLEngine engine;
            private final Object wrapLock;
            private final Object unwrapLock;
            private ByteBuffer unwrapSrc;
            private ByteBuffer wrapDst;
            private boolean closed;
            @Getter
            private boolean secure;
            private int u_remaining;

            private int packetBufferSize;
            private int applicationBufferSize;

            public EngineWrapper(SocketChannel socketChannel, SSLEngine engine) {
                this.socketChannel = socketChannel;
                this.engine = engine;
                this.wrapLock = new Object();
                this.unwrapLock = new Object();
                this.closed = false;
                this.unwrapSrc = allocate(BufferType.PACKET);
                this.wrapDst = allocate(BufferType.PACKET);
                this.secure = false;
            }

            Result wrapAndSend(ByteBuffer byteBuffer) throws IOException {
                return wrapAndSend(byteBuffer, false);
            }

            Result wrapAndSend(ByteBuffer src, boolean ignoreClose) throws IOException {
                if(closed && !ignoreClose)
                    close();

                SSLEngineResult.Status status;
                Result result = Result.newInstance();
                synchronized (wrapLock) {
                    wrapDst.clear();
                    do {
                        result.result = engine.wrap(src, wrapDst);
                        status = result.result.getStatus();
                        if(status == SSLEngineResult.Status.BUFFER_OVERFLOW)
                            wrapDst = realloc(wrapDst, true, BufferType.PACKET);
                    } while(status == SSLEngineResult.Status.BUFFER_OVERFLOW);

                    if(status == SSLEngineResult.Status.CLOSED && !ignoreClose) {
                        closed = true;
                        return result;
                    }
                    if(result.result.bytesProduced() > 0) {
                        wrapDst.flip();
                        int length = wrapDst.remaining();
                        assert length == result.result.bytesProduced();
                        while(length > 0)
                            length -= socketChannel.write(wrapDst);
                    }
                }
                return result;
            }

            Result receiveAndUnwrap(ByteBuffer dst, boolean allowSpin) throws IOException {
                SSLEngineResult.Status status = SSLEngineResult.Status.OK;
                Result result = Result.newInstance();
                result.buffer = dst;

                synchronized (unwrapLock) {
                    if(closed)
                        throw new CloseSocketException();
                    boolean needData;
                    if(u_remaining > 0) {
                        unwrapSrc.compact();
                        unwrapSrc.flip();
                        needData = false;
                    } else {
                        unwrapSrc.clear();
                        needData = true;
                    }
                    int nRead;
                    do {
                        if(needData) {
                            do {
                                nRead = socketChannel.read(unwrapSrc);
                            } while(allowSpin && nRead == 0);
                            if(nRead == -1)
                                throw new CloseSocketException();
                            unwrapSrc.flip();
                        }
                        result.result = engine.unwrap(unwrapSrc, result.buffer);
                        if(status == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                            if(unwrapSrc.limit() == unwrapSrc.capacity())
                                unwrapSrc = realloc(unwrapSrc, false, BufferType.PACKET);
                            else {
                                unwrapSrc.position(unwrapSrc.limit());
                                unwrapSrc.limit(unwrapSrc.capacity());
                            }
                            needData = true;
                        } else if(status == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                            result.buffer = realloc(result.buffer, true, BufferType.APPLICATION);
                            needData = false;
                        } else if(status == SSLEngineResult.Status.CLOSED) {
                            closed = true;
                            result.buffer.flip();
                            return result;
                        }
                    } while(status != SSLEngineResult.Status.OK);

                    u_remaining = unwrapSrc.remaining();
                }
                return result;
            }

            public Result sendData(ByteBuffer src) throws IOException {
                Result result = null;
                while(src.remaining() > 0) {
                    result = wrapAndSend(src);
                    if(result.result.getStatus() == SSLEngineResult.Status.CLOSED) {
                        close();
                        return result;
                    }
                    SSLEngineResult.HandshakeStatus status = result.result.getHandshakeStatus();
                    if(status != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING && status != SSLEngineResult.HandshakeStatus.FINISHED)
                        doHandshake();
                }
                return result;
            }

            public Result receiveData(ByteBuffer dest) throws IOException {
                Result result;
                assert dest.position() == 0;

                result = receiveAndUnwrap(dest, false);
                dest = result.buffer;
                SSLEngineResult.Status status = result.result.getStatus();
                if(status == SSLEngineResult.Status.CLOSED) {
                    close();
                    return result;
                }
                SSLEngineResult.HandshakeStatus handshakeStatus = result.result.getHandshakeStatus();
                if(handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING && handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED)
                    doHandshake();

                dest.flip();
                return result;
            }

            void doHandshake() throws IOException {
                engine.beginHandshake();
                ByteBuffer tmp = allocate(BufferType.APPLICATION);
                SSLEngineResult.HandshakeStatus status = engine.getHandshakeStatus();
                while(status != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING && status != SSLEngineResult.HandshakeStatus.FINISHED) {
                    Result result = null;
                    switch (status) {
                        case NEED_TASK:
                            Runnable task;
                            while((task = engine.getDelegatedTask()) != null)
                                task.run();
                        case NEED_WRAP:
                            tmp.clear();
                            tmp.flip();
                            result = wrapAndSend(tmp);
                            break;
                        case NEED_UNWRAP:
                            tmp.clear();
                            result = receiveAndUnwrap(tmp, true);
                            tmp = result.buffer;
                            assert tmp.position() == 0;
                            break;
                    }
                    status = result == null ? engine.getHandshakeStatus() : result.result.getHandshakeStatus();
                    if(result != null)
                        Result.delete(result);
                }
                secure = status != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;
            }

            void close() throws IOException {
                closed = true;
                ByteBuffer tmp = allocate(BufferType.APPLICATION);
                Result r;
                do {
                    tmp.clear();
                    tmp.flip();
                    r = wrapAndSend(tmp, true);
                } while(r.result.getStatus() != SSLEngineResult.Status.CLOSED && r.result.bytesProduced() > 0);
                Result.delete(r);
                engine.closeOutbound();
                try {
                    engine.closeInbound();
                } catch (SSLException e) {
                    if("Inbound closed before receiving peer's close_notify: possible truncation attack?".equals(e.getMessage()))
                        System.out.println("Socket closed too fast for " + engine.getPeerHost() + ":" + engine.getPeerPort() + "!");
                    else
                        e.printStackTrace();
                }
            }

            private ByteBuffer allocate(BufferType type) {
                return allocate(type, -1);
            }

            private ByteBuffer allocate(BufferType type, int len) {
                assert engine != null;
                int size;
                if(type == BufferType.PACKET) {
                    if(packetBufferSize == 0)
                        packetBufferSize = engine.getSession().getPacketBufferSize();
                    if(len > packetBufferSize)
                        packetBufferSize = len;
                    size = packetBufferSize;
                } else {
                    if(applicationBufferSize == 0)
                        applicationBufferSize = engine.getSession().getApplicationBufferSize();
                    if(len > applicationBufferSize)
                        applicationBufferSize = len;
                    size = applicationBufferSize;
                }
                return ByteBuffer.allocate(size);
            }

            private ByteBuffer realloc(ByteBuffer buffer, boolean flip, BufferType type) {
                int nSize = 2 * buffer.capacity();
                ByteBuffer newBuffer = allocate(type, nSize);
                if(flip)
                    buffer.flip();
                newBuffer.put(buffer);
                buffer = newBuffer;

                return buffer;
            }

            enum BufferType {
                PACKET,
                APPLICATION
            }

            static class Result implements Recyclable {
                private static final CachedObjectFactory<Result> factory = new SmartCachedObjectFactory<>(Result::new);
                SSLEngineResult result;
                ByteBuffer buffer;

                public static Result newInstance() {
                    return factory.getInstance();
                }

                public static void delete(Result result) {
                    factory.putInstance(result);
                }

                @Override
                public void recycle() {
                    result = null;
                    buffer = null;
                }
            }
        }
    }
}
