package com.alesharik.webserver.control.socket;

import com.alesharik.webserver.api.ThreadFactories;
import com.alesharik.webserver.api.control.messaging.ControlSocketClientConnection;
import com.alesharik.webserver.api.control.messaging.ControlSocketMessage;
import com.alesharik.webserver.api.control.messaging.ControlSocketServerConnection;
import lombok.AllArgsConstructor;
import sun.misc.Cleaner;

import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@AllArgsConstructor
class ControlSocketServerConnectionManager extends Thread {
    private static final ControlSocketClientConnection.Authenticator authenticator = new ControlSocketClientConnection.Authenticator() {
        @Override
        public String getPassword() {
            return "";
        }

        @Override
        public String getLogin() {
            return "";
        }
    };

    private final SSLServerSocket sslServerSocket;
    private final ThreadGroup threadGroup = new ThreadGroup("ControlSocketServerConnectionWorkers");

    private final String login;
    private final String password;

    private final CopyOnWriteArrayList<ControlSocketServerConnectionImpl> connections = new CopyOnWriteArrayList<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool(ThreadFactories.newThreadFactory(threadGroup));

    @Override
    public void run() {
        setName("ControlSocketServerConnectionManager-Acceptor");
        while(isAlive()) {
            try {
                Socket client = sslServerSocket.accept();

                ControlSocketServerConnectionImpl controlSocketServerConnection = new ControlSocketServerConnectionImpl(client, authenticator, login, password);

                connections.add(controlSocketServerConnection);

                executorService.submit(controlSocketServerConnection);

                Cleaner.create(controlSocketServerConnection, () -> {
                    controlSocketServerConnection.close();
                    connections.remove(controlSocketServerConnection);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getConnectionCount() {
        return connections.size();
    }

    public void shutdown() {
        executorService.shutdown();
        interrupt();
    }

    public void shutdownNow() {
        executorService.shutdownNow();
        interrupt();
    }

    private static final class ControlSocketServerConnectionImpl extends AbstractControlSocketConnection implements ControlSocketServerConnection {
        private final String login;
        private final String password;

        public ControlSocketServerConnectionImpl(Socket sslSocket, ControlSocketClientConnection.Authenticator authenticator, String login, String password) {
            super(sslSocket, authenticator);
            this.login = login;
            this.password = password;
        }


        @Override
        protected boolean processAuthentication(String login, String password) {
            return this.login.equals(login) && this.password.equals(password);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void parseMessageObject(ControlSocketMessage controlSocketMessage) {
            ControlSocketMessageHandlerManager.getHandlerFor(controlSocketMessage.getClass())
                    .ifPresent(controlSocketMessageHandler -> controlSocketMessageHandler.handleMessage(controlSocketMessage));
        }
    }
}
