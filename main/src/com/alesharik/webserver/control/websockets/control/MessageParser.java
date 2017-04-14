package com.alesharik.webserver.control.websockets.control;

import com.alesharik.webserver.api.SerialRepository;
import com.alesharik.webserver.logger.Logger;
import one.nio.serial.SerializerNotFoundException;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Deprecated
class MessageParser extends Thread {
    private final ControlWebSocket controlWebSocket;
    private final BlockingDeque<String> queue = new LinkedBlockingDeque<>();
    private WebSocketController webSocketController;
    private boolean isRunning = true;
    private boolean isWaitingMessage = false;
    private String buffer;

    public MessageParser(ControlWebSocket controlWebSocket) {
        this.controlWebSocket = controlWebSocket;
    }

    @SuppressWarnings("Duplicates")
    public void run() {
        while(isRunning) {
            String message = queue.poll();
            if(isWaitingMessage) {
                buffer = message;
                isWaitingMessage = false;
                continue;
            }

            String[] parts = message.split(":");
            switch (parts[0]) {
                case "plugin":
                    parsePluginMessage(parts);
                    break;
            }
        }
    }

    private void parsePluginMessage(String[] parts) {
        String pluginName = parts[1];
        if(parts[2].equals("object")) {
            try {
                webSocketController.onMessage(pluginName, SerialRepository.deserialize(parts[3]));
            } catch (SerializerNotFoundException e) {
                webSocketController.requestSerializer(e.getUid());
                try {
                    webSocketController.onMessage(pluginName, SerialRepository.deserialize(parts[3]));
                } catch (SerializerNotFoundException e1) {
                    Logger.log(e1);
                }
            }
        } else {
            webSocketController.onMessage(pluginName, parts[2]);
        }
    }

    public String waitMessage() {
        isWaitingMessage = true;
        while(isWaitingMessage) {
            try {
                sleep(1);
            } catch (InterruptedException e) {
                Logger.log(e);
            }
        }
        return buffer;
    }

    public void addMessage(String message) {
        queue.add(message);
    }

    public void shutdown() {
        isRunning = false;
    }

    public void setWebSocketController(WebSocketController webSocketController) {
        this.webSocketController = webSocketController;
    }
}
