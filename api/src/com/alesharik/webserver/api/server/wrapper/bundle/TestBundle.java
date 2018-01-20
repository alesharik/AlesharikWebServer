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

package com.alesharik.webserver.api.server.wrapper.bundle;

import com.alesharik.webserver.api.server.wrapper.addon.MessageProcessor;
import com.alesharik.webserver.api.server.wrapper.addon.websocket.WebSocketMessageProcessor;
import com.alesharik.webserver.api.server.wrapper.addon.websocket.WebSocketRequestUpgrader;
import com.alesharik.webserver.api.server.wrapper.bundle.processor.HttpProcessor;
import com.alesharik.webserver.api.server.wrapper.bundle.processor.impl.HttpRouterProcessor;
import com.alesharik.webserver.api.server.wrapper.http.HttpStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.alesharik.webserver.api.server.wrapper.bundle.processor.impl.HttpChainProcessor.chain;
import static com.alesharik.webserver.api.server.wrapper.bundle.processor.impl.HttpRouterProcessor.router;

@HttpBundle("test")
@Deprecated
public class TestBundle implements HttpHandlerBundle {
    private final HttpRouterProcessor test;

    public TestBundle() {
        test = router()
                .path("/test", new WebSocketRequestUpgrader())
                .path("/test/string", chain().then((request, response) -> {
                    response.respond(HttpStatus.OK_200);
                    response.getWriter().write("test");
                }));
    }

    @Override
    public Validator getValidator() {
        return request -> true;
    }

    @Nonnull
    @Override
    public ErrorHandler getErrorHandler() {
        return (e, request, response, pool) -> e.printStackTrace();
    }

    @Nullable
    @Override
    public HttpProcessor getProcessor() {
        return test;
    }

    @Nullable
    @Override
    public MessageProcessor<?, ?> getMessageProcessor(String name, MessageProcessorParameters parameters) {
        return new WebSocketMessageProcessor() {
            @Override
            public void onConnect() {
                System.out.println("Connect");
            }

            @Override
            public void onMessage(String message) {
                System.out.println("Message: " + message);
                broadcaster.sendMessage(message);
            }

            @Override
            public void onClose(int code) {
                System.out.println("Close: " + code);
            }
        };
    }
}
