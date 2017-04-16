package com.alesharik.webserver.control.socket;

import com.alesharik.webserver.api.ThreadFactories;
import com.alesharik.webserver.api.control.messaging.ControlSocketClientConnection;
import com.alesharik.webserver.api.control.messaging.ControlSocketMessage;
import net.jcip.annotations.NotThreadSafe;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

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

        ControlSocketClientConnectionImpl controlSocketClientConnection = new ControlSocketClientConnectionImpl(sslSocket, authenticator, connections);

        connections.add(controlSocketClientConnection);

        executorService.submit(controlSocketClientConnection);
//
//        Cleaner.create(controlSocketClientConnection, () -> {
//            controlSocketClientConnection.close();
//            connections.remove(controlSocketClientConnection);
//        });

        return controlSocketClientConnection;
    }

    @NotThreadSafe
    private static final class ControlSocketClientConnectionImpl extends AbstractControlSocketConnection implements ControlSocketClientConnection {
        private final ArrayList<Listener> listeners = new ArrayList<>();
        private final CopyOnWriteArrayList<ControlSocketClientConnectionImpl> connections;

        public ControlSocketClientConnectionImpl(SSLSocket sslSocket, Authenticator authenticator, CopyOnWriteArrayList<ControlSocketClientConnectionImpl> connections) {
            super(sslSocket, authenticator);
            this.connections = connections;
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
        public void awaitConnection() {
            Signaller signaller = new Signaller(TimeUnit.MILLISECONDS.toNanos(1), 0L);
            waitForSignaller(signaller);
        }

        @Override
        protected void parseMessageObject(ControlSocketMessage controlSocketMessage) {
            listeners.stream()
                    .filter(listener -> listener.canListen(controlSocketMessage.getClass()))
                    .forEach(listener -> listener.listen(controlSocketMessage));
        }

        @Override
        public void run() {
            super.run();
            connections.remove(this);
        }


        private void waitForSignaller(Signaller signaller) {
            if(signaller.thread != null && !isConnected()) {
                try {
                    ForkJoinPool.managedBlock(signaller);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }

        private final class Signaller implements ForkJoinPool.ManagedBlocker {
            long nanos;
            final long deadline;
            volatile Thread thread;

            Signaller(long nanos, long deadline) {
                this.thread = Thread.currentThread();
                this.nanos = nanos;
                this.deadline = deadline;
            }

            public boolean block() {
                if(isReleasable()) {
                    return true;
                } else if(nanos > 0L) {
                    LockSupport.parkNanos(this, nanos);
                } else if(deadline == 0L) {
                    LockSupport.park(this);
                }
                return isReleasable();
            }

            @Override
            public boolean isReleasable() {
                if(deadline != 0L && (nanos <= 0L || (nanos = deadline - System.nanoTime()) <= 0L)) {
                    thread = null;
                    return true;
                }
                return isConnected();
            }
        }
    }
}
