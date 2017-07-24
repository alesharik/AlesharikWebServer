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

package com.alesharik.webserver.module.http;

import com.alesharik.webserver.api.server.wrapper.http.HeaderManager;
import com.alesharik.webserver.api.server.wrapper.http.HttpVersion;
import com.alesharik.webserver.api.server.wrapper.http.Request;
import com.alesharik.webserver.api.server.wrapper.http.Response;
import com.alesharik.webserver.api.server.wrapper.http.header.ObjectHeader;
import com.alesharik.webserver.api.server.wrapper.server.ExecutorPool;
import com.alesharik.webserver.api.server.wrapper.server.HttpRequestHandler;
import com.alesharik.webserver.api.server.wrapper.server.Sender;
import com.alesharik.webserver.api.server.wrapper.server.ServerSocketWrapper;
import com.alesharik.webserver.api.server.wrapper.server.SocketProvider;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.net.ssl.SSLSocket;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

final class DispatcherThread extends Thread {
    private final Selector selector;
    private final HttpRequestHandler httpHandler;
    private final ExecutorPool executorPool;
    private final AtomicBoolean isRunning;
    private final HttpServerModuleImpl.HttpServerStatisticsImpl httpServerStatistics;

    public DispatcherThread(ThreadGroup threadGroup, HttpRequestHandler httpHandler, ExecutorPool executorPool, HttpServerModuleImpl.HttpServerStatisticsImpl httpServerStatistics) throws IOException {
        super(threadGroup, "HttpServer-Dispatcher");
        this.httpHandler = httpHandler;
        this.executorPool = executorPool;
        this.httpServerStatistics = httpServerStatistics;
        this.selector = Selector.open();
        this.isRunning = new AtomicBoolean(true);
    }

    public void shutdown() {
        isRunning.set(false);
        try {
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handle(ServerSocketWrapper wrapper) {
        if(!wrapper.isRunning())
            return;
        wrapper.registerSelector(selector);
    }

    @Override
    public void run() {
        while(isRunning.get()) {
            try {
                int ops = selector.select();
                if(ops == 0)
                    continue;

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while(iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if(key.isAcceptable()) {
                        SocketProvider.ServerSocketWrapper attachment = (SocketProvider.ServerSocketWrapper) key.attachment();
                        SocketChannel socket = attachment.getServerSocket().accept();
                        if(socket == null)
                            continue;
                        socket.configureBlocking(false);
                        if(!socket.finishConnect())
                            continue;

                        SelectableChannel socketChannel = socket.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ, new Session(socket.socket(), httpHandler, executorPool, httpServerStatistics, attachment.getSocketManager()));
                    } else if(key.isReadable()) {
                        Session socket = (Session) key.attachment();
                        processSession(socket);
                    }
                    iterator.remove();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processSession(Session socket) {
        executorPool.executeSelectorTask(() -> {
            try {
                socket.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static final class Session implements Sender {
        private static final Charset CHARSET = Charset.forName("ISO-8859-1");

        private final AtomicBoolean firstTry;
        private final Socket socket;
        private final ByteArrayOutputStream byteBuffer;
        private final AtomicReference<State> state;
        private final ByteBuffer buffer;
        private final HttpRequestHandler requestHandler;
        private final ExecutorPool pool;
        private final HttpServerModuleImpl.HttpServerStatisticsImpl serverStatistics;
        private final SocketProvider.SocketManager socketManager;

        private Request.Builder builder;

        public Session(Socket socket, HttpRequestHandler requestHandler, ExecutorPool pool, HttpServerModuleImpl.HttpServerStatisticsImpl serverStatistics, SocketProvider.SocketManager socketManager) {
            this.socket = socket;
            this.requestHandler = requestHandler;
            this.pool = pool;
            this.serverStatistics = serverStatistics;
            this.socketManager = socketManager;
            this.byteBuffer = new ByteArrayOutputStream();
            this.state = new AtomicReference<>(State.NOTHING);
            this.buffer = ByteBuffer.allocate(2048);
            this.serverStatistics.aliveConnections.incrementAndGet();
            this.serverStatistics.newConnection();
            this.firstTry = new AtomicBoolean(true);
        }

        public void read() throws IOException {
            if(firstTry.getAndSet(false))
                socketManager.initSocket(socket.getChannel());

            if(state.get().readEnd())
                return;

            int nRead;
            while((nRead = socket.getChannel().read(buffer)) == 2048) {
                byteBuffer.write(socketManager.unwrap(buffer.array()));
                buffer.clear();
            }
            if(nRead == -1) {
                serverStatistics.aliveConnections.decrementAndGet();
                socket.close();
                check();
                return;
            }
            byteBuffer.write(socketManager.unwrap(buffer.array()), 0, nRead);
            check();
        }

        @SuppressFBWarnings("DM_DEFAULT_ENCODING")//FindBugs error
        private void check() throws IOException {
            String str = new String(byteBuffer.toByteArray(), CHARSET);
            while(str.startsWith("\r\n"))
                str = str.replaceFirst("\r\n", "");
            if(builder == null || this.state.get() == State.NOTHING) { //Parse first line
                int firstLine = str.indexOf("\r\n");
                if(firstLine == -1)
                    return;
                builder = Request.Builder.start(str.substring(0, firstLine));
                this.state.set(State.HEADERS);
                str = str.substring(firstLine);
                this.byteBuffer.reset();
                this.byteBuffer.write(str.getBytes(CHARSET));

                builder.withInfo(((InetSocketAddress) socket.getRemoteSocketAddress()), socket.getLocalAddress(), socket instanceof SSLSocket);
            }
            int headerEnd = str.indexOf("\r\n\r\n");
            if(headerEnd != -1 && !str.isEmpty() && this.state.get() == State.HEADERS) {
                builder.withHeaders(str.substring(0, headerEnd));
                str = str.substring(headerEnd);
                this.state.set(State.ALL);
                this.byteBuffer.reset();
                this.byteBuffer.write(str.getBytes(CHARSET));
            }
            if(this.state.get() == State.ALL) { //Parse body
                if(socket.isClosed()) {
                    //Ok :(
                    return;
                }
                if(builder.containsHeader("Content-Length")) {//ALL REQUESTS WITH MESSAGE BODY MUST CONTAINS VALID Content-Length HEADER
                    @SuppressWarnings("unchecked")
                    long length = builder.getHeader(HeaderManager.getHeaderByName("Content-Length", ObjectHeader.class), Long.class);
                    if(byteBuffer.size() >= length) {
                        long last = byteBuffer.size() - length;
                        byte[] bytes = byteBuffer.toByteArray();
                        if(length > Integer.MAX_VALUE)
                            System.err.println("Skipping bytes from " + socket.getRemoteSocketAddress().toString());
                        byte[] bodyBytes = new byte[(int) length];
                        System.arraycopy(bytes, 0, bodyBytes, 0, (int) length);
                        builder.withBody(bodyBytes);
                        byteBuffer.reset();
                        byteBuffer.write(bytes, (int) length, (int) last);
                        publish();
                    }
                } else if(str.contains("\r\n\r\n")) {
                    int index = str.indexOf("\r\n\r\n");
                    if(index != 0) {
//                        if(builder.containsHeader(""))//TODO use Content-Type!
//                            builder.withBody(str.substring(0, index).getBytes());
                        str = str.substring(index + 4);
                    }
                    this.state.set(State.ALL);
                    this.byteBuffer.reset();
                    this.byteBuffer.write(str.getBytes(CHARSET));
                    publish();
                }
            }
        }

        private void publish() {
            serverStatistics.newRequest();
            requestHandler.handleRequest(builder, pool, this);
            builder = null;
            this.state.set(State.NOTHING);
        }

        /**
         * @param request ignored
         */
        @Override
        public void send(Request request, Response response) {
            SocketChannel channel = socket.getChannel();
            byte[] res = response.toByteArray();
            ByteBuffer byteBuffer = ByteBuffer.wrap(socketManager.wrap(res));
            int nSend = 0;
            int count = 0;
            try {
                while((nSend += channel.write(byteBuffer)) < res.length) {
                    count++;
                    if(count > 10) {
                        System.err.println("SocketChannel to " + socket.getRemoteSocketAddress().toString() + " has problems! Try #" + count);
                    }
                }
                socket.getOutputStream().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(((request.getHttpVersion() == HttpVersion.HTTP_1_0 || request.getHttpVersion() == HttpVersion.HTTP_0_9) && !request.containsHeader("Connection: keep-alive")) || request.containsHeader("Connection: close")) {
                try {
                    socket.close();
                    serverStatistics.aliveConnections.decrementAndGet();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(response.getResponseCode() > 499 && response.getResponseCode() < 600)
                serverStatistics.newError();

            serverStatistics.addResponseTimeAvg(System.currentTimeMillis() - response.getCreationTime());

            Response.delete(response);
            Request.Builder.delete((Request.Builder) request);
        }

        enum State {
            NOTHING,
            HEADERS,
            ALL,
            END;

            boolean readEnd() {
                return this == ALL || this == END;
            }
        }
    }
}
