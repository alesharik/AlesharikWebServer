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

import com.alesharik.webserver.api.cache.object.CachedObjectFactory;
import com.alesharik.webserver.api.cache.object.Recyclable;
import com.alesharik.webserver.api.cache.object.SmartCachedObjectFactory;
import com.alesharik.webserver.api.memory.impl.ByteOffHeapVector;
import com.alesharik.webserver.module.http.addon.AddOn;
import com.alesharik.webserver.module.http.addon.AddOnSocketContext;
import com.alesharik.webserver.module.http.addon.AddOnSocketHandler;
import com.alesharik.webserver.module.http.http.HttpStatus;
import com.alesharik.webserver.module.http.http.Request;
import com.alesharik.webserver.module.http.http.Response;
import com.alesharik.webserver.module.http.server.CloseSocketException;
import com.alesharik.webserver.module.http.server.ExecutorPool;
import com.alesharik.webserver.module.http.server.HttpRequestHandler;
import com.alesharik.webserver.module.http.server.SelectorContext;
import com.alesharik.webserver.module.http.server.Sender;
import com.alesharik.webserver.module.http.server.socket.ServerSocketWrapper;
import com.alesharik.webserver.module.http.server.socket.SocketWriter;
import lombok.Getter;
import org.jctools.queues.atomic.MpscLinkedAtomicQueue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class SelectorContextImpl implements SelectorContext {
    private static final int SESSION_BUFFER_SIZE;
    private static final int MAX_MESSAGE_SIZE;

    static {
        if(System.getProperty("module.http.SESSION_BUFFER_SIZE") != null)
            SESSION_BUFFER_SIZE = Integer.parseInt(System.getProperty("module.http.SESSION_BUFFER_SIZE"));
        else
            SESSION_BUFFER_SIZE = 16 * 1024;
        if(System.getProperty("module.http.MAX_MESSAGE_SIZE") != null)
            MAX_MESSAGE_SIZE = Integer.parseInt(System.getProperty("module.http.MAX_MESSAGE_SIZE"));
        else
            MAX_MESSAGE_SIZE = 512 * 1024 * 1024;
    }

    private final HttpServerModuleImpl.HttpServerStatisticsImpl serverStatistics;
    private final HttpRequestHandler requestHandler;
    private final ExecutorPool executorPool;
    private final List<String> addons;

    private final Selector readSelector;
    private final Selector writeSelector;

    private final AtomicInteger socketCount = new AtomicInteger(0);
    private final MpscLinkedAtomicQueue<Session> init = new MpscLinkedAtomicQueue<>();
    private final AtomicBoolean writeLock = new AtomicBoolean(false);

    public SelectorContextImpl(HttpServerModuleImpl.HttpServerStatisticsImpl serverStatistics, HttpRequestHandler requestHandler, ExecutorPool executorPool, List<String> addons) {
        this.serverStatistics = serverStatistics;
        this.requestHandler = requestHandler;
        this.executorPool = executorPool;
        this.addons = addons;

        try {
            readSelector = Selector.open();
            writeSelector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getSocketCount() {
        return socketCount.get();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void registerSocket(SelectableChannel socket, SocketChannel socketChannel, ServerSocketWrapper.SocketManager manager) {
        if(!socket.isOpen())
            return;
        Session session = Session.create(socketChannel, manager, requestHandler, executorPool, serverStatistics, addons, writeSelector);

        while(!writeLock.compareAndSet(false, true))
            while(writeLock.get()) ;
        try {
            init.add(session);
            readSelector.wakeup();
            try {
                socket.register(readSelector, SelectionKey.OP_READ, session);
                socketCount.incrementAndGet();
            } catch (ClosedChannelException e) {
                init.remove(session);
            }
        } finally {
            writeLock.set(false);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void iteration() {
        try {
            int ops = readSelector.select();
            if(writeLock.get())
                while(writeLock.get()) ;

            Session session;
            while((session = init.poll()) != null) {
                if(!session.init()) {
                    session.close();
                    Session.recycle(session);
                }
            }

            if(writeSelector.selectNow() > 0)
                handleWrite();

            if(ops == 0)
                return;

            Set<SelectionKey> selectionKeys = readSelector.selectedKeys();
            for(SelectionKey selectionKey : selectionKeys) {
                if(!selectionKey.isValid())
                    continue;

                if(selectionKey.isReadable()) {
                    Session s = (Session) selectionKey.attachment();
                    if(!s.read()) {
                        selectionKey.cancel();
                        s.flushRemainingData();
                        s.close();
                        Session.recycle(s);
                    }
                }
            }
            selectionKeys.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleWrite() {
        Set<SelectionKey> selectionKeys = writeSelector.selectedKeys();
        for(SelectionKey selectionKey : selectionKeys) {
            if(!selectionKey.isValid())
                continue;
            DelayedWrite write = (DelayedWrite) selectionKey.attachment();
            write.write(writeSelector);
            DelayedWrite.recycle(write);
        }
        selectionKeys.clear();
    }

    @Override
    public void wakeup() {
        readSelector.wakeup();
    }

    @Override
    public void close() {
        try {
            readSelector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writeSelector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Getter
    private static final class DelayedWrite implements Recyclable {
        public static final SmartCachedObjectFactory<DelayedWrite> FACTORY = new SmartCachedObjectFactory<>(DelayedWrite::new);

        private volatile ByteBuffer send;
        private volatile SocketChannel socketChannel;

        public static DelayedWrite create(ByteBuffer send, SocketChannel socket) {
            DelayedWrite write = FACTORY.getInstance();
            write.send = send;
            write.socketChannel = socket;
            return write;
        }

        public static void recycle(DelayedWrite write) {
            FACTORY.putInstance(write);
        }

        public void write(Selector writeSelector) {
            if(!socketChannel.isOpen())
                return;

            try {
                while(send.remaining() > 0) {
                    if(socketChannel.write(send) == 0) {
                        DelayedWrite write = create(send, socketChannel);
                        socketChannel.register(writeSelector, SelectionKey.OP_WRITE, write);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void recycle() {
            send = null;
            socketChannel = null;
        }
    }

    private static final class Session implements Recyclable, Sender, SocketWriter, AddOnSocketContext {
        private static final ByteOffHeapVector vector = ByteOffHeapVector.instance();
        private static final CachedObjectFactory<Session> FACTORY = new SmartCachedObjectFactory<>(Session::new);

        private final ByteBuffer buffer = ByteBuffer.allocateDirect(SESSION_BUFFER_SIZE);
        private final Map<String, Object> params = new HashMap<>();
        private SocketChannel socket;
        private ServerSocketWrapper.SocketManager socketManager;
        private int messageSize;
        private long tempBuffer;
        private State state = State.EMPTY;
        private Request.Builder request;
        private int bodyLength = -1;
        private byte[] buf = new byte[4096];

        private AddOn addOn;
        private AddOnSocketHandler addOnSocketHandler;
        private Request handshakeRequest;

        private HttpRequestHandler requestHandler;
        private ExecutorPool executorPool;
        private HttpServerModuleImpl.HttpServerStatisticsImpl statistics;
        private List<String> addons;
        private Selector writeSelector;

        public static Session create(SocketChannel socketChannel, ServerSocketWrapper.SocketManager manager, HttpRequestHandler requestHandler, ExecutorPool executorPool, HttpServerModuleImpl.HttpServerStatisticsImpl statistics, List<String> addons, Selector writeSelector) {
            Session session = FACTORY.getInstance();
            session.socket = socketChannel;
            session.socketManager = manager;
            session.tempBuffer = vector.allocate();
            session.requestHandler = requestHandler;
            session.executorPool = executorPool;
            session.statistics = statistics;
            session.writeSelector = writeSelector;
            session.addons = addons;
            return session;
        }

        public static void recycle(Session session) {
            FACTORY.putInstance(session);
        }

        @Override
        public void recycle() {
            socket = null;
            socketManager = null;
            buffer.clear();
            messageSize = 0;
            vector.clear(tempBuffer);
            tempBuffer = 0;
            requestHandler = null;
            addOnSocketHandler = null;
            addOn = null;
            executorPool = null;
            state = State.EMPTY;
            bodyLength = -1;
            statistics = null;
            addons = null;
            params.clear();
            //noinspection PointlessNullCheck
            if(handshakeRequest != null && handshakeRequest instanceof Request.Builder)
                Request.Builder.delete((Request.Builder) handshakeRequest);
            handshakeRequest = null;
        }

        public boolean init() {
            if(!socket.isOpen())
                return false;
            try {
                socketManager.init(socket);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public void writeBytes(@Nonnull ByteBuffer byteBuffer) {
            try {
                socketManager.write(socket, byteBuffer, this);
            } catch (CloseSocketException e) {
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Nonnull
        @Override
        public SocketChannel getChannel() {
            return socket;
        }

        @Override
        public void setParameter(@Nonnull String name, @Nullable Object o) {
            if(o == null)
                params.remove(name);
            params.put(name, o);
        }

        @Nullable
        @Override
        public Object getParameter(@Nonnull String name) {
            return params.get(name);
        }

        public void close() {
            try {
                socketManager.close(socket);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public Request getHandshakeRequest() {
            return handshakeRequest;
        }

        public boolean read() {
            if(!socket.isOpen())
                return false;
            do {
                buffer.clear();
                try {
                    socketManager.read(socket, buffer);
                } catch (CloseSocketException e) {
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                if(!incrementAndCheckMessageSize(buffer.position()))
                    return false;
                int size = buffer.position();
                int off = 0;
                while(size > 0) {
                    buffer.position(off);
                    if(!process(size))
                        return false;
                    size -= buffer.position();
                    off += buffer.position();
                }
            } while(!buffer.hasRemaining());
            return true;
        }

        private boolean process(final int size) {
            if(addOnSocketHandler != null) {
                requestHandler.handleMessageTask(() -> addOnSocketHandler.handle(buffer.duplicate(), this), executorPool, addOn, this);
                return true;
            }

            if(state == State.EMPTY) {
                while(buffer.position() < size) {//Cut empty lines at start
                    char c = (char) buffer.get();
                    if(c != '\n' && c != '\r') {
                        state = State.FIRST_LINE;
                        tempBuffer = vector.write(tempBuffer, Character.toString(c).getBytes(StandardCharsets.ISO_8859_1));
                        break;
                    }
                }
            }

            if(state == State.FIRST_LINE) {
                StringBuilder firstLine = new StringBuilder();
                if(vector.size(tempBuffer) > 0) {
                    firstLine.append(new String(vector.toByteArray(tempBuffer), StandardCharsets.ISO_8859_1));
                    vector.clear(tempBuffer);
                }
                if(!firstLine.toString().endsWith("\r")) {
                    while(buffer.position() < size) {
                        char c = (char) buffer.get();
                        firstLine.append(c);
                        if(c == '\r')
                            break;
                    }
                }

                if(buffer.position() >= size) {
                    tempBuffer = vector.write(tempBuffer, firstLine.toString().getBytes(StandardCharsets.ISO_8859_1));
                } else {
                    char c = (char) buffer.get();
                    if(c != '\n')
                        return false;
                    firstLine.append(c);

                    String first = firstLine.toString();
                    if(!first.endsWith("\r\n"))
                        throw new RuntimeException("WAT");
                    first = first.substring(0, first.length() - 2);//Delete /r/n
                    request = Request.Builder.start(first)
                            .withInfo((InetSocketAddress) socket.socket().getRemoteSocketAddress(), socket.socket().getLocalAddress(), socketManager.isSecure(socket));
                    state = State.HEADERS;
                }
            }
            if(state == State.HEADERS) {
                StringBuilder headerBuilder = new StringBuilder();
                if(vector.size(tempBuffer) > 0) {
                    headerBuilder.append(new String(vector.toByteArray(tempBuffer), StandardCharsets.ISO_8859_1));
                    vector.clear(tempBuffer);
                }
                while(buffer.position() < size) {
                    char c = (char) buffer.get();
                    headerBuilder.append(c);
                    if(c == '\n' && headerBuilder.charAt(headerBuilder.length() - 2) == '\r') {//End found
                        String header = headerBuilder.toString();
                        headerBuilder.setLength(0);
                        if(header.equals("\r\n")) {//Body start
                            request.buildHeaders();
                            state = State.BODY;
                            break;
                        } else
                            request.withHeader(header);
                    }
                }
                if(headerBuilder.length() > 0)
                    tempBuffer = vector.write(tempBuffer, headerBuilder.toString().getBytes(StandardCharsets.ISO_8859_1));
            }
            if(bodyLength == -1) {
                Long length = request.getHeader("Content-Length");
                if(length == null)
                    state = State.END;
                else
                    bodyLength = length.intValue();
            }
            if(state == State.BODY) {
                int nRead = (int) Math.min(bodyLength - vector.size(tempBuffer), size - buffer.position());
                while(nRead > 0) {
                    int read = Math.min(buf.length, nRead);
                    buffer.get(buf, 0, read);
                    nRead -= read;
                    tempBuffer = vector.write(tempBuffer, buf, 0, read);
                }
                if(vector.size(tempBuffer) == bodyLength) {
                    request.withBody(vector.toByteArray(tempBuffer));
                    vector.clear(tempBuffer);
                    state = State.END;
                } else if(vector.size(tempBuffer) > bodyLength)
                    throw new RuntimeException("WAT");
            }
            if(state == State.END) {
                statistics.newRequest();
                try {
                    requestHandler.handleRequest(request, executorPool, this);
                } catch (CloseSocketException e) {
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                request = null;
                messageSize = 0;
                state = State.EMPTY;
            }
            return true;
        }

        public void flushRemainingData() {
            int size = buffer.position();
            int off = 0;
            while(size > 0) {
                buffer.position(off);
                if(!process(size))
                    return;
                size -= buffer.position();
                off += buffer.position();
            }
        }

        @Override
        public void send(Request request, Response response) {
            if(!socket.isOpen()) {
                System.err.println("Socket closed! Ignoring response...");
                return;
            }

            sendInternal(response);

            if(response.isUpgraded()) {
                if(!addons.contains(response.getUpgrade())) {
                    System.err.println("AddOn is not registered! Name: " + response.getUpgrade());
                    Response.delete(response);
                    sendInternalServerError();
                    if(request instanceof Request.Builder)
                        Request.Builder.delete((Request.Builder) request);
                    return;
                }
                AddOn addOn = AddOnManager.getAddOn(response.getUpgrade());
                if(addOn == null) {
                    System.err.println("AddOn with name not found! Name: " + response.getUpgrade());
                    Response.delete(response);
                    sendInternalServerError();
                    if(request instanceof Request.Builder)
                        Request.Builder.delete((Request.Builder) request);
                    return;
                }
                handshakeRequest = request;
                AddOnSocketHandler handler = requestHandler.getAddOnSocketHandler(request, executorPool, addOn);
                this.addOnSocketHandler = handler;
                this.addOn = addOn;
                handler.init(this);
            } else {
                if(request instanceof Request.Builder)
                    Request.Builder.delete((Request.Builder) request);
            }

            statistics.addResponseTimeAvg(System.currentTimeMillis() - response.getCreationTime());
            if(response.getResponseCode() > 499 && response.getResponseCode() < 600)
                statistics.newError();
            Response.delete(response);

        }

        protected void sendInternalServerError() {
            Response r = Response.getResponse();
            r.respond(HttpStatus.INTERNAL_SERVER_ERROR_500);
            sendInternal(r);
            Response.delete(r);
        }

        protected void sendInternal(Response response) {
            byte[] data = response.toByteArray();
            try {
                socketManager.write(socket, ByteBuffer.wrap(data), this);
            } catch (CloseSocketException e) {
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean incrementAndCheckMessageSize(int delta) {
            if(addOnSocketHandler != null)
                return true;

            messageSize += delta;
            if(messageSize > MAX_MESSAGE_SIZE) {
                Response response = Response.getResponse();
                response.respond(HttpStatus.REQUEST_ENTITY_TOO_LARGE_413);
                sendInternal(response);
                Response.delete(response);
                return false;
            }
            return true;
        }

        @Override
        public void write(SocketChannel socketChannel, ByteBuffer byteBuffer) {
            while(byteBuffer.remaining() > 0) {
                try {
                    if(socketChannel.write(byteBuffer) == 0) {
                        socketChannel.register(writeSelector, SelectionKey.OP_WRITE, DelayedWrite.create(byteBuffer.duplicate(), socket));
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        enum State {
            /**
             * Empty lines
             */
            EMPTY,
            /**
             * First line
             */
            FIRST_LINE,
            HEADERS,
            BODY,
            END
        }
    }
}
