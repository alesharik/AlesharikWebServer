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

import com.alesharik.webserver.api.cache.object.CachedObjectFactory;
import com.alesharik.webserver.api.cache.object.Recyclable;
import com.alesharik.webserver.api.cache.object.SmartCachedObjectFactory;
import com.alesharik.webserver.api.collections.ConcurrentLiveHashMap;
import com.alesharik.webserver.api.name.Named;
import com.alesharik.webserver.api.server.wrapper.PortRange;
import com.alesharik.webserver.api.server.wrapper.server.CloseSocketException;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import lombok.Getter;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;

import static com.alesharik.webserver.configuration.XmlHelper.*;

@Named("secured-network-listener")
public class SecuredNetworkListener implements com.alesharik.webserver.api.server.wrapper.server.ServerSocketWrapper {
    private final SecuredServerSocketConfig config = new SecuredServerSocketConfig();
    private ServerSocketWrapper serverSocket;

    @Override
    public void registerSelector(Selector selector) {
        try {
            serverSocket.getServerSocket().register(selector, SelectionKey.OP_ACCEPT, serverSocket);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return "secured-network-listener";
    }

    @Override
    public void start() {
        try {
            serverSocket = config.newSocket();
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    @Override
    public void shutdownNow() {
        try {
            serverSocket.getServerSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        try {
            serverSocket.getServerSocket().close();
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

        SecuredServerSocketConfig() {

        }

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
            }
            this.performance = false;


            receiveBufferSize = getInteger("receiveBuffer", element, false, -1);
            reuseAddress = Boolean.parseBoolean(getString("reuseAddress", element, false));
            soTimeout = getInteger("timeout", element, false, -1);

            Element sslConfig = getXmlElement("ssl", element, true);

            Element keyStoreConfig = getXmlElement("keystore", sslConfig, true);

            String keyStoreType = getString("type", keyStoreConfig, true);
            File keyStoreFile = getFile("store", keyStoreConfig, true);
            String keyStorePassword = getString("password", keyStoreConfig, true);

            Element trustoreConfig = getXmlElement("truststore", sslConfig, true);

            String trustoreType = getString("type", trustoreConfig, true);
            File trustoreFile = getFile("store", trustoreConfig, true);
            String trustorePassword = getString("password", trustoreConfig, true);

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
                throw new ConfigurationParseError(e);
            }
        }

        public ServerSocketWrapper newSocket() throws IOException {
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
            return new ServerSocketWrapper(serverSocket.getChannel(), new SocketManagerImpl(sslContext));
        }

        private static final class SocketManagerImpl implements SocketManager {
            private final SSLContext sslContext;
            /**
             * SocketChannel: (value == null - insecure, SSLEngine - secure)
             */
            private final Map<Socket, EngineWrapper> engines;

            public SocketManagerImpl(SSLContext sslContext) {
                this.sslContext = sslContext;
                this.engines = new ConcurrentLiveHashMap<>();
            }

            @Override
            public void initSocket(SocketChannel socketChannel) {
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
            public byte[] read(SocketChannel socketChannel) {
                if(!isSecure(socketChannel))
                    return new byte[0];

                try {
                    EngineWrapper wrapper = engines.get(socketChannel.socket());
                    ByteBuffer byteBuffer = wrapper.allocate(EngineWrapper.BufferType.APPLICATION);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    EngineWrapper.Result result;
                    do {
                        result = wrapper.receiveData(byteBuffer);
                        byteArrayOutputStream.write(result.buffer.array(), 0, result.buffer.remaining());
                        byteBuffer = result.buffer;
                        byteBuffer.clear();
                    } while(result.result.bytesProduced() == byteBuffer.capacity());
                    EngineWrapper.Result.delete(result);
                    return byteArrayOutputStream.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new byte[0];
            }

            @Override
            public void write(SocketChannel socketChannel, byte[] data) {
                ByteBuffer toSend = ByteBuffer.wrap(data);
                if(!isSecure(socketChannel)) {
                    int nSend = 0;
                    int count = 0;
                    try {
                        while((nSend += socketChannel.write(toSend)) < data.length) {
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
                    EngineWrapper.Result.delete(engineWrapper.sendData(toSend));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean hasCustomRW(SocketChannel socketChannel) {
                return isSecure(socketChannel);
            }

            @Override
            public boolean isSecure(SocketChannel socketChannel) {
                return engines.get(socketChannel.socket()) != null;
            }

            @Override
            public void listenSocketClose(SocketChannel socketChannel) {
                EngineWrapper engineWrapper = engines.remove(socketChannel.socket());
                if(engineWrapper == null)
                    return;
                try {
                    engineWrapper.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
}
