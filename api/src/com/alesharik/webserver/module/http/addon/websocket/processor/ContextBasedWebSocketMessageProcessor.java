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
import lombok.RequiredArgsConstructor;

import java.util.function.BiFunction;

@RequiredArgsConstructor
public class ContextBasedWebSocketMessageProcessor extends WiredWebSocketMessageProcessor {
    protected final BiFunction<WebSocketBroadcaster, WebSocketMessageProcessorContext, Processor> supplier;

    @Override
    public void onConnect(WebSocketMessageProcessorContext context, WebSocketBroadcaster broadcaster) {
        getProcessor(context, broadcaster).onConnect();
    }

    @Override
    public void onClose(WebSocketMessageProcessorContext context, WebSocketBroadcaster broadcaster, int code) {
        getProcessor(context, broadcaster).onClose(code);
    }

    @Override
    public void onPing(WebSocketMessageProcessorContext context, WebSocketBroadcaster broadcaster) {
        getProcessor(context, broadcaster).onPing();
    }

    @Override
    public void onPong(WebSocketMessageProcessorContext context, WebSocketBroadcaster broadcaster) {
        getProcessor(context, broadcaster).onPong();
    }

    @Override
    public void onMessage(WebSocketMessageProcessorContext context, WebSocketBroadcaster broadcaster, String message) {
        getProcessor(context, broadcaster).onMessage(message);
    }

    @Override
    public void onMessage(WebSocketMessageProcessorContext context, WebSocketBroadcaster broadcaster, byte[] message) {
        getProcessor(context, broadcaster).onMessage(message);
    }

    @Override
    public void onFragment(WebSocketMessageProcessorContext context, WebSocketBroadcaster broadcaster, boolean last, String fragment) {
        getProcessor(context, broadcaster).onFragment(last, fragment);
    }

    @Override
    public void onFragment(WebSocketMessageProcessorContext context, WebSocketBroadcaster broadcaster, boolean last, byte[] fragment) {
        getProcessor(context, broadcaster).onFragment(last, fragment);
    }

    protected Processor getProcessor(WebSocketMessageProcessorContext context, WebSocketBroadcaster broadcaster) {
        if(context.getParameter("processor") != null)
            return context.getParameter("processor");
        else {
            Processor processor = supplier.apply(broadcaster, context);
            context.setParameter("processor", processor);
            return processor;
        }
    }

    @RequiredArgsConstructor
    public abstract class Processor {
        protected final WebSocketBroadcaster broadcaster;
        protected final WebSocketMessageProcessorContext context;

        void onConnect() {
        }

        void onClose(int code) {
        }

        void onPing() {
            broadcaster.sendPong();
        }

        void onPong() {
        }

        void onMessage(String message) {
        }

        void onMessage(byte[] message) {
        }

        void onFragment(boolean last, String fragment) {
        }

        void onFragment(boolean last, byte[] fragment) {
        }
    }
}
