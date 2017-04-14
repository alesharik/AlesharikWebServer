package com.alesharik.webserver.control.websockets.control;

import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.main.server.MainServer;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;

/**
 * This class used for control {@link MainServer} with WebSockets
 */
@Deprecated
@ClientEndpoint
public final class ControlWebSocket {
    private final MessageParser messageParser;
    private Session session = null;
    private String logPass;
    private boolean isAuthorized = false;

    public ControlWebSocket(String logPass) {
        this.logPass = logPass;
        messageParser = new MessageParser(this);
    }

    public void connect(URI uri) throws IOException, DeploymentException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, uri);
    }

    @OnMessage
    public String onMessage(String message) {
        Logger.log("sad");
        if(message.equals("Hello") && !isAuthorized) {
            send("LogPass=" + logPass);
        } else if(message.equals("OK") && !isAuthorized) {
            isAuthorized = true;
        } else {
            messageParser.addMessage(message);
        }
        return "";
    }

    @OnOpen
    public void onOpen(Session session) {
        if(this.session == null) {
            this.session = session;
            messageParser.start();
            send("Hello");
        } else {
            close(session);
        }
    }

    @OnClose
    public void onClose(Session session) {
        if(session.equals(this.session)) {
            this.session = null;
        } else {
            close(session);
        }
    }

    @OnError
    public void onError(Throwable throwable) {
        Logger.log(throwable);
    }

    private void close(Session session) {
        try {
            session.close();
            messageParser.shutdown();
        } catch (IOException e) {
            onError(e);
        }
    }

    /**
     * Send message asynchronous
     */
    public void send(String message) {
        session.getAsyncRemote().sendText(message);
    }

    public String sendMessageAndGetResponse(String message) {
        send(message);
        return messageParser.waitMessage();
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    MessageParser getMessageParser() {
        return messageParser;
    }
}
