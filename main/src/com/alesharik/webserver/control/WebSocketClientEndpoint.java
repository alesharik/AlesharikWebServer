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

package com.alesharik.webserver.control;

import com.alesharik.webserver.api.LoginPasswordCoder;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.net.URI;

@Deprecated
@ClientEndpoint
public class WebSocketClientEndpoint {
    Session userSession = null;
    private boolean waitMessage = false;
    private String messageText;

    private String password;
    private String login;
    private boolean isSync = false;
    private boolean isOpen = false;
    private URI uri;

    public WebSocketClientEndpoint(URI uri, String login, String password) {
        this.uri = uri;
        this.login = login;
        this.password = password;
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, uri);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @OnOpen
    public void onOpen(Session userSession) {
        this.userSession = userSession;
        isOpen = true;
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        isSync = false;
        this.userSession = null;
    }

    @OnMessage
    public void onMessage(String message) {
        if(!isSync) {
            if(message.equals("Hello")) {
                sendMessage("LogPass=" + LoginPasswordCoder.encode(login, password));
            } else if(message.equals("OK")) {
                this.isSync = true;
            }
            return;
        }
        if(waitMessage) {
            messageText = message;
            waitMessage = false;
        }
    }

    public void sendMessage(String message) {
        if(message.equals("Hello") || message.contains("LogPass=")) {
            this.userSession.getAsyncRemote().sendText(message);
            return;
        }
        if(isSync && isOpen) {
            check();
            this.userSession.getAsyncRemote().sendText(message);
        }
    }

    public String sendMessageAndGetResponse(String message) {
        if(isSync && isOpen) {

            this.userSession.getAsyncRemote().sendText(message);
            waitMessage = true;
            while(waitMessage) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return messageText;
        }
        check();
        return "";
    }

    private void check() {
        if(userSession == null) {
            isSync = false;
        }
        if(!isSync && isOpen) {
            try {
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                container.connectToServer(this, uri).getAsyncRemote().sendText("Hello");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
