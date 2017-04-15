package com.alesharik.webserver.control.socket;

import com.alesharik.webserver.api.ThreadFactories;
import com.alesharik.webserver.api.control.messaging.ControlSocketClientConnection;
import com.alesharik.webserver.api.control.messaging.ControlSocketMessage;
import net.jcip.annotations.NotThreadSafe;
import sun.misc.Cleaner;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ControlSocketClientConnectionPool {
    private final ExecutorService executorService;
    private final SSLSocketFactory sslSocketFactory;
    private final CopyOnWriteArrayList<ControlSocketClientConnectionImpl> connections;

    public ControlSocketClientConnectionPool(ThreadGroup threadGroup, SSLSocketFactory sslSocketFactory) {
        this.executorService = Executors.newCachedThreadPool(ThreadFactories.newThreadFactory(threadGroup));
        this.sslSocketFactory = sslSocketFactory;
        this.connections = new CopyOnWriteArrayList<>();
    }

    public void shutdown() {
        executorService.shutdown();
        connections.forEach(ControlSocketClientConnectionImpl::close);
    }

    public ControlSocketClientConnection newConnection(String host, int port, ControlSocketClientConnection.Authenticator authenticator) throws IOException {
        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(host, port);

        ControlSocketClientConnectionImpl controlSocketClientConnection = new ControlSocketClientConnectionImpl(sslSocket, authenticator);

        connections.add(controlSocketClientConnection);

        executorService.submit(controlSocketClientConnection);

        Cleaner.create(controlSocketClientConnection, () -> {
            controlSocketClientConnection.close();
            connections.remove(controlSocketClientConnection);
        });

        return controlSocketClientConnection;
    }

    @NotThreadSafe
    private static final class ControlSocketClientConnectionImpl extends AbstractControlSocketConnection implements ControlSocketClientConnection {
        private final ArrayList<Listener> listeners = new ArrayList<>();

        public ControlSocketClientConnectionImpl(SSLSocket sslSocket, Authenticator authenticator) {
            super(sslSocket, authenticator);
        }

        @Override
        public void sendMessage(ControlSocketMessage message) throws IOException {
            if(isClosed()) {
                throw new SocketException("Socket is closed");
            }
            if(!isConnected()) {
                throw new SocketException("Socket is not connected");
            }
            super.sendMessage(message);
        }

        @Override
        protected boolean processAuthentication(String login, String password) {
            return true; //Client always accept authentication
        }

        @Override
        public void addListener(Listener listener) {
            listeners.add(listener);
        }

        @Override
        public void removeListener(Listener listener) {
            listeners.remove(listener);
        }

        @Override
        public boolean containsListener(Listener listener) {
            return listeners.contains(listener);
        }

        @Override
        protected void parseMessageObject(ControlSocketMessage controlSocketMessage) {
            listeners.stream()
                    .filter(listener -> listener.canListen(controlSocketMessage.getClass()))
                    .forEach(listener -> listener.listen(controlSocketMessage));
        }
    }
}
