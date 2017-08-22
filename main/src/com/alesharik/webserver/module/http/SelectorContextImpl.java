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

import com.alesharik.webserver.api.cache.object.Recyclable;
import com.alesharik.webserver.api.cache.object.SmartCachedObjectFactory;
import com.alesharik.webserver.api.memory.impl.ByteOffHeapVector;
import com.alesharik.webserver.api.server.wrapper.http.HeaderManager;
import com.alesharik.webserver.api.server.wrapper.http.Request;
import com.alesharik.webserver.api.server.wrapper.http.Response;
import com.alesharik.webserver.api.server.wrapper.http.header.ObjectHeader;
import com.alesharik.webserver.api.server.wrapper.server.CloseSocketException;
import com.alesharik.webserver.api.server.wrapper.server.ExecutorPool;
import com.alesharik.webserver.api.server.wrapper.server.HttpRequestHandler;
import com.alesharik.webserver.api.server.wrapper.server.SelectorContext;
import com.alesharik.webserver.api.server.wrapper.server.Sender;
import com.alesharik.webserver.api.server.wrapper.server.SocketProvider;
import lombok.Getter;
import org.jctools.queues.atomic.MpscAtomicArrayQueue;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;

final class SelectorContextImpl implements SelectorContext {
    private static final SmartCachedObjectFactory<Session> sessionFactory = new SmartCachedObjectFactory<>(Session::new);

    private final Selector readSelector;
    private final Selector writeSelector;
    private final AtomicLong counter;
    private final MpscAtomicArrayQueue<Session> initQueue;
    private final HttpServerModuleImpl.HttpServerStatisticsImpl statistics;
    private final HttpRequestHandler requestHandler;
    private final ExecutorPool executorPool;
    private final StampedLock lock;
    private final AtomicBoolean isWriteLocked = new AtomicBoolean(false);
    private final AtomicBoolean isInRegisterBlock = new AtomicBoolean(false);

    public SelectorContextImpl(HttpServerModuleImpl.HttpServerStatisticsImpl statistics, HttpRequestHandler requestHandler, ExecutorPool executorPool) {
        this.statistics = statistics;
        this.requestHandler = requestHandler;
        this.executorPool = executorPool;
        try {
            readSelector = Selector.open();
            writeSelector = Selector.open();
            initQueue = new MpscAtomicArrayQueue<>(2048);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        counter = new AtomicLong(0);
        lock = new StampedLock();
    }

    @Override
    public long getSocketCount() {
        return counter.get();
    }

    @Override
    public void registerSocket(SelectableChannel socket, SocketChannel socketChannel, SocketProvider.SocketManager manager) {
        if(!socket.isOpen())
            return;
        try {
            statistics.aliveConnections.incrementAndGet();
            long stamp = -1;
            try {
                Session fill = sessionFactory.getInstance().fill(manager, socketChannel, counter, requestHandler, executorPool, statistics, writeSelector);
                initQueue.add(fill);
                isWriteLocked.set(true);
                readSelector.wakeup();
                stamp = lock.readLock();
                isInRegisterBlock.set(true);
                socket.register(readSelector, SelectionKey.OP_READ, fill);
            } finally {
                if(lock.validate(stamp))
                    lock.unlockRead(stamp);
                isWriteLocked.set(false);
                isInRegisterBlock.set(false);
            }
            counter.incrementAndGet();
        } catch (ClosedChannelException ignored) {
            //Socket closed - ignore it
        }
    }

    @Override
    public void iteration() {
        try {

            int ops = readSelector.select();

            if(isWriteLocked.get()) {
                while(!isInRegisterBlock.get() && isWriteLocked.get()) {
                    //NOP
                }
                lock.unlockWrite(lock.writeLock());
            }

            Session s;
            while((s = initQueue.poll()) != null) {
                if(!s.init()) {
                    s.close();
                    sessionFactory.putInstance(s);
                }
            }

            if(writeSelector.selectNow() > 0)
                handleWriteOps();

            if(ops == 0)
                return;

            Set<SelectionKey> readKeys = readSelector.selectedKeys();
            Iterator<SelectionKey> iterator = readKeys.iterator();
            while(iterator.hasNext()) {
                SelectionKey next = iterator.next();
                if(!next.isValid()) {
                    iterator.remove();
                    continue;
                }

                if(next.isReadable()) {
                    Session attachment = (Session) next.attachment();
                    if(!attachment.read()) {
                        next.cancel();
                        iterator.remove();
                        attachment.close();
                        attachment.check();
                        attachment.clearOnExit(sessionFactory);
                    }
                }
            }
            readKeys.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Must be executed ONLY after {@link Selector#selectNow()}
     */
    private void handleWriteOps() {
        Set<SelectionKey> selectionKeys = writeSelector.selectedKeys();
        Iterator<SelectionKey> iterator = selectionKeys.iterator();
        while(iterator.hasNext()) {
            SelectionKey next = iterator.next();
            if(!next.isValid()) {
                iterator.remove();
                continue;
            }

            DelayedWrite send = (DelayedWrite) next.attachment();
            try {
                boolean usable = false;
                while(send.getSend().remaining() > 0) {
                    if(send.getSocketChannel().write(send.getSend()) == 0) {
                        send.getSocketChannel().register(writeSelector, SelectionKey.OP_WRITE, send);
                        usable = true;
                        break;
                    }
                }
                if(!usable)
                    DelayedWrite.factory.putInstance(send);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void wakeup() {
        readSelector.wakeup();
    }

    @Getter
    private static final class DelayedWrite implements Recyclable {
        public static final SmartCachedObjectFactory<DelayedWrite> factory = new SmartCachedObjectFactory<>(DelayedWrite::new);

        private volatile ByteBuffer send;
        private volatile SocketChannel socketChannel;

        public DelayedWrite fill(ByteBuffer byteBuffer, SocketChannel socketChannel) {
            this.send = byteBuffer;
            this.socketChannel = socketChannel;
            return this;
        }

        @Override
        public void recycle() {
            send = null;
            socketChannel = null;
        }
    }

    private static final class Session implements Recyclable, Sender {
        private static final ByteOffHeapVector data = ByteOffHeapVector.instance();
        private static final Charset CHARSET = Charset.forName("ISO-8859-1");

        private final ByteBuffer byteBuffer;

        private volatile SocketProvider.SocketManager socketManager;
        private volatile SocketChannel socketChannel;
        private volatile long time;
        private volatile long vector;
        private volatile AtomicLong counter;
        private volatile HttpRequestHandler requestHandler;
        private volatile ExecutorPool executorPool;
        private volatile HttpServerModuleImpl.HttpServerStatisticsImpl statistics;
        private volatile Selector writeSelector;
        private volatile AtomicReference<State> state;
        private volatile Request.Builder builder;

        private volatile SmartCachedObjectFactory<Session> deleteAuto;

        public Session() {
            byteBuffer = ByteBuffer.allocate(2048);
        }

        public Session fill(SocketProvider.SocketManager manager, SocketChannel socketChannel, AtomicLong counter, HttpRequestHandler httpRequestHandler, ExecutorPool executorPool, HttpServerModuleImpl.HttpServerStatisticsImpl serverStatistics, Selector readSelector) {
            this.socketManager = manager;
            this.socketChannel = socketChannel;
            this.vector = data.allocate();
            this.counter = counter;
            this.requestHandler = httpRequestHandler;
            this.executorPool = executorPool;
            this.statistics = serverStatistics;
            this.writeSelector = readSelector;
            this.state = new AtomicReference<>(State.NOTHING);
            return this;
        }

        public void clearOnExit(SmartCachedObjectFactory<Session> sessionSmartCachedObjectFactory) {
            this.deleteAuto = sessionSmartCachedObjectFactory;
        }

        public boolean canDelete() {
            return this.deleteAuto == null;
        }

        public boolean init() {
            try {
                socketManager.initSocket(socketChannel);
                time = System.currentTimeMillis();
                return true;
            } catch (CloseSocketException e) {
                return false;
            }
        }

        /**
         * Check automatically except closed
         *
         * @return false if socket closed
         */
        public boolean read() {
            if(socketManager.hasCustomRW(socketChannel)) {
                try {
                    byte[] read = socketManager.read(socketChannel);
                    if(read.length > 0) {
                        vector = data.write(vector, read);
                        check();
                    }
                    return true;
                } catch (CloseSocketException e) {
                    return false;
                }
            } else {
                try {
                    int nRead;
                    while((nRead = socketChannel.read(byteBuffer)) == 2048) {
                        vector = data.write(vector, byteBuffer.array());
                        byteBuffer.clear();
                    }

                    if(nRead == -1)
                        return false;

                    if(nRead > 0) {
                        vector = data.write(vector, byteBuffer.array(), 0, nRead);
                        check();
                    }
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        /**
         * Return true if request was published
         */
        public boolean check() {
            if(data.size(vector) == 0)
                return false;

            String str = new String(data.toByteArray(vector), CHARSET);
            while(str.startsWith("\r\n"))
                str = str.replaceFirst("\r\n", "");

            if(builder == null || this.state.get() == State.NOTHING) { //Parse first line
                int firstLine = str.indexOf("\r\n");
                if(firstLine == -1)
                    return false;
                builder = Request.Builder.start(str.substring(0, firstLine));
                this.state.set(State.HEADERS);
                str = str.substring(firstLine);

                data.clear(vector);
                this.vector = data.fromByteArray(str.getBytes(CHARSET));

                builder.withInfo(((InetSocketAddress) socketChannel.socket().getRemoteSocketAddress()), socketChannel.socket().getLocalAddress(), socketManager.isSecure(socketChannel));
            }

            int headerEnd = str.indexOf("\r\n\r\n");
            if(headerEnd == -1)
                headerEnd = str.length();
            if(!str.isEmpty() && this.state.get() == State.HEADERS) {
                builder.withHeaders(str.substring(0, headerEnd));
                str = str.substring(headerEnd);
                this.state.set(State.ALL);
                data.clear(vector);
                this.vector = data.fromByteArray(str.getBytes(CHARSET));
            }

            if(this.state.get() == State.ALL) { //Parse body
                if(!socketChannel.isOpen()) {
                    //Ok :(
                    publish();
                    return true;
                }

                if(builder.containsHeader("Content-Length")) {//ALL REQUESTS WITH MESSAGE BODY MUST CONTAINS VALID Content-Length HEADER
                    @SuppressWarnings("unchecked")
                    long length = builder.getHeader(HeaderManager.getHeaderByName("Content-Length", ObjectHeader.class), Long.class);
                    if(data.size(vector) >= length) {
                        long last = data.size(vector) - length;
                        byte[] bytes = data.toByteArray(vector);
                        if(length > Integer.MAX_VALUE)
                            System.err.println("Skipping bytes from " + socketChannel.socket().getRemoteSocketAddress().toString());
                        byte[] bodyBytes = new byte[(int) length];
                        System.arraycopy(bytes, 0, bodyBytes, 0, (int) length);
                        builder.withBody(bodyBytes);
                        data.clear(vector);
                        data.write(vector, bytes, (int) length, (int) last);
                        publish();
                        return true;
                    }
                } else if(str.contains("\r\n\r\n")) {
                    int index = str.indexOf("\r\n\r\n");
                    if(index != -1) {
//                        if(builder.containsHeader(""))//TODO use Content-Type!
//                            builder.withBody(str.substring(0, index).getBytes());
                        str = str.substring(index + 4);
                    }
                    data.clear(vector);
                    this.vector = data.fromByteArray(str.getBytes(CHARSET));
                    publish();
                    return true;
                }
            }
            return false;
        }

        private void publish() {
            statistics.newRequest();
            requestHandler.handleRequest(builder, executorPool, this);
            builder = null;
            this.state.set(Session.State.NOTHING);
        }

        public void close() {
            if(!socketChannel.isOpen())
                return;
            try {
                counter.decrementAndGet();
                statistics.aliveConnections.decrementAndGet();
                socketManager.listenSocketClose(socketChannel);
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void recycle() {
            socketManager = null;
            socketChannel = null;
            time = 0;
            byteBuffer.clear();
            data.clear(vector);
            vector = 0;
            counter = null;
            requestHandler = null;
            executorPool = null;
            statistics = null;
            writeSelector = null;
            state = null;
            builder = null;
            deleteAuto = null;
        }

        @Override
        public void send(@Nullable Request request, Response response) {
            if(socketChannel == null || !socketChannel.isOpen())
                return;

            byte[] toSend = response.toByteArray();
            if(socketManager.hasCustomRW(socketChannel)) {
                try {
                    socketManager.write(socketChannel, toSend);
                } catch (CloseSocketException e) {
                    close();
                }
            } else {
                ByteBuffer byteBuffer = ByteBuffer.wrap(toSend);
                try {
                    while(byteBuffer.remaining() > 0) {
                        if(socketChannel.write(byteBuffer) == 0) {
                            socketChannel.register(writeSelector, SelectionKey.OP_WRITE, DelayedWrite.factory.getInstance().fill(byteBuffer, socketChannel));
                            Response.delete(response);
                            statistics.addResponseTimeAvg(System.currentTimeMillis() - response.getCreationTime());
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                }
            }
//
//            if(((request.getHttpVersion() == HttpVersion.HTTP_1_0 || request.getHttpVersion() == HttpVersion.HTTP_0_9) && !request.containsHeader("Connection: keep-alive")) || request.containsHeader("Connection: close")) {
//                close();
//            }

            statistics.addResponseTimeAvg(System.currentTimeMillis() - response.getCreationTime());
            if(response.getResponseCode() > 499 && response.getResponseCode() < 600)
                statistics.newError();
            Response.delete(response);
            if(request instanceof Request.Builder)
                Request.Builder.delete((Request.Builder) request);

            if(deleteAuto != null)
                deleteAuto.putInstance(this);
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
