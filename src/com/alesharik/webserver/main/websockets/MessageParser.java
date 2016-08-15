package com.alesharik.webserver.main.websockets;

import com.alesharik.webserver.logger.Logger;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

class MessageParser extends Thread{
    private final ServerControllerWebSocketApplication application;
    private final BlockingDeque<String> queue = new LinkedBlockingDeque<>();
    private boolean isRunning = true;
    private boolean isWaitingMessage = false;
    private String buffer;

    public MessageParser(ServerControllerWebSocketApplication application) {
        this.application = application;
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

        } else {

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
}
