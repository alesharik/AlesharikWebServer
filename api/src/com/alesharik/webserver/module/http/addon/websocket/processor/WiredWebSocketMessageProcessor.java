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

package com.alesharik.webserver.module.http.addon.websocket.processor;

import com.alesharik.webserver.module.http.addon.websocket.impl.WebSocketBroadcaster;

import javax.annotation.Nonnull;

public abstract class WiredWebSocketMessageProcessor implements WebSocketMessageProcessor {
    @Override
    public void processMessage(@Nonnull WebSocketMessage message, @Nonnull WebSocketBroadcaster broadcaster, @Nonnull WebSocketMessageProcessorContext context) {
        if(message.isConnect())
            onConnect(context, broadcaster);
        else if(message.isClose())
            onClose(context, broadcaster, Integer.parseInt(message.getMessageString()));
        else if(message.isPing())
            onPing(context, broadcaster);
        else if(message.isPong())
            onPong(context, broadcaster);
        else if(message.isDataMessage()) {
            if(message.isFragment()) {
                if(message.isByte())
                    onFragment(context, broadcaster, message.isEnd(), message.getMessage());
                else
                    onFragment(context, broadcaster, message.isEnd(), message.getMessageString());
            } else {
                if(message.isByte())
                    onMessage(context, broadcaster, message.getMessage());
                else
                    onMessage(context, broadcaster, message.getMessageString());
            }
        }
    }

    public void onConnect(WebSocketMessageProcessorContext context, WebSocketBroadcaster broadcaster) {
    }

    public void onClose(WebSocketMessageProcessorContext context, WebSocketBroadcaster broadcaster, int code) {
    }

    public void onPing(WebSocketMessageProcessorContext context, WebSocketBroadcaster broadcaster) {

    }

    public void onPong(WebSocketMessageProcessorContext context, WebSocketBroadcaster broadcaster) {
    }

    public void onMessage(WebSocketMessageProcessorContext context, WebSocketBroadcaster broadcaster, String message) {
    }

    public void onMessage(WebSocketMessageProcessorContext context, WebSocketBroadcaster broadcaster, byte[] message) {
    }

    public void onFragment(WebSocketMessageProcessorContext context, WebSocketBroadcaster broadcaster, boolean last, String fragment) {
    }

    public void onFragment(WebSocketMessageProcessorContext context, WebSocketBroadcaster broadcaster, boolean last, byte[] fragment) {
    }
}
