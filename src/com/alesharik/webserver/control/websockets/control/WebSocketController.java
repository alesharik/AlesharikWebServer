package com.alesharik.webserver.control.websockets.control;

import com.alesharik.webserver.api.LoginPasswordCoder;
import com.alesharik.webserver.api.SerialRepository;
import com.alesharik.webserver.api.server.control.ControlWebSocketPlugin;
import com.alesharik.webserver.api.server.control.ControlWebSocketWrapper;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.plugin.accessManagers.ControlAccessManagerBuilder;
import org.glassfish.grizzly.http.util.Base64Utils;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

/**
 * This is controller of ControlWebSocket
 */
public final class WebSocketController {
    private final HashMap<String, ControlWebSocketWrapper> wrappers = new HashMap<>();
    private final URI uri;
    private final String logPass;
    private ControlWebSocket webSocket;
    private MessageParser messageParser;

    public WebSocketController(URI server, String login, String password) {
        this.uri = server;
        this.logPass = LoginPasswordCoder.encode(login, password);
    }

    /**
     * Connect to remote socket
     */
    public void connect() throws IOException, DeploymentException {
        webSocket = new ControlWebSocket(logPass);
        webSocket.connect(uri);
        messageParser = webSocket.getMessageParser();
        messageParser.setWebSocketController(this);
        while(!webSocket.isAuthorized()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Logger.log(e);
            }
        }
    }

    /**
     * Disconnect form remote socket
     */
    public void disconnect() {
        sendMessage("closeSession");
        webSocket = null;
        messageParser = null;
    }

    public String sendMessageAndGetResponse(String message) {
        if(webSocket == null) {
            throw new RuntimeException("Connection is not established!");
        }
        return webSocket.sendMessageAndGetResponse(message);
    }

    public void sendMessage(String message) {
        if(webSocket == null) {
            throw new RuntimeException("Connection is not established!");
        }
        webSocket.send(message);
    }

    public void sendMessageFromPlugin(String message, String pluginName) {
        sendMessage("plugin:" + pluginName + ":" + message);
    }

    public void sendObjectFromPlugin(Object object, String pluginName) {
        sendMessage("plugin:" + pluginName + ":object:" + object);
    }

    public void onMessage(String plugin, Object object) {
        wrappers.get(plugin).onMessage(object);
    }

    public void onMessage(String plugin, String msg) {
        wrappers.get(plugin).onMessage(msg);
    }

    public void requestSerializer(long uid) {
        sendMessage("getSerializer:" + uid);
        SerialRepository.addSerializedSeriaizer(messageParser.waitMessage());
    }

    public String getComputerInfo() {
        return new String(Base64Utils.decode(sendMessageAndGetResponse("getComputerInfo")));
    }


    public ControlWebSocketWrapper registerPlugin(ControlWebSocketPlugin plugin) {
        return new ControlWebSocketWrapper(this, plugin);
    }

    public void setupControlAccessManagerBuilder(ControlAccessManagerBuilder builder) {
        builder.setWebSocketController(this);
    }
}
