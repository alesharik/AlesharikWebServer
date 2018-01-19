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

package com.alesharik.webserver.api.server.wrapper.server.impl.handler;

import com.alesharik.webserver.api.name.Named;
import com.alesharik.webserver.api.server.wrapper.addon.AddOn;
import com.alesharik.webserver.api.server.wrapper.addon.AddOnSocketHandler;
import com.alesharik.webserver.api.server.wrapper.addon.Message;
import com.alesharik.webserver.api.server.wrapper.addon.MessageProcessor;
import com.alesharik.webserver.api.server.wrapper.addon.MessageSender;
import com.alesharik.webserver.api.server.wrapper.bundle.ErrorHandler;
import com.alesharik.webserver.api.server.wrapper.bundle.FilterChain;
import com.alesharik.webserver.api.server.wrapper.bundle.HttpHandler;
import com.alesharik.webserver.api.server.wrapper.bundle.HttpHandlerBundle;
import com.alesharik.webserver.api.server.wrapper.bundle.HttpHandlerResponseDecorator;
import com.alesharik.webserver.api.server.wrapper.http.Request;
import com.alesharik.webserver.api.server.wrapper.http.Response;
import com.alesharik.webserver.api.server.wrapper.server.BatchingRunnableTask;
import com.alesharik.webserver.api.server.wrapper.server.ExecutorPool;
import com.alesharik.webserver.api.server.wrapper.server.HttpRequestHandler;
import com.alesharik.webserver.api.server.wrapper.server.Sender;
import com.alesharik.webserver.logger.Debug;
import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * This class manages all {@link com.alesharik.webserver.api.server.wrapper.bundle.HttpHandlerBundle} bundles
 */
@Named("deprecated-http-request-handler")
public class DeprecatedHttpRequestHandler implements HttpRequestHandler {
    private final Set<HttpHandlerBundle> bundles;

    public DeprecatedHttpRequestHandler(Set<HttpHandlerBundle> bundles) {
        this.bundles = bundles;
    }

    @Override
    public void handleRequest(Request request, ExecutorPool executorPool, Sender sender) {
        executorPool.executeWorkerTask(new BundleSelectTask(bundles, request, executorPool, sender));
    }

    @Override
    public AddOnSocketHandler getAddOnSocketHandler(Request request, ExecutorPool executorPool, AddOn addOn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handleMessage(@Nonnull MessageProcessor messageProcessor, @Nonnull Message message, @Nonnull MessageSender messageSender, @Nonnull ExecutorPool executorPool, @Nonnull AddOn addOn, @Nonnull Object sync) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handleMessageTask(@Nonnull Runnable task, @Nonnull ExecutorPool executorPool, @Nonnull AddOn addOn, @Nonnull Object sync) {
        throw new UnsupportedOperationException();
    }

    @AllArgsConstructor
    private static final class BundleSelectTask implements BatchingRunnableTask<Object> {
        private final Set<HttpHandlerBundle> bundles;
        private final Request request;
        private final ExecutorPool executorPool;
        private final Sender sender;

        @Override
        public void run() {
            HttpHandlerBundle bundle = null;
            for(HttpHandlerBundle httpHandlerBundle : bundles) {
                if(httpHandlerBundle.getValidator().isRequestValid(request)) {
                    bundle = httpHandlerBundle;
                    break;
                }
            }
            Debug.log("Request from " + request.getRemote().toString());
            if(bundle == null) {
                System.err.println("Bundle not found for " + request.getContextPath());
                return;
            }
            FilterChain chain;

            try {
                chain = bundle.getRouter().route(request, bundle.getFilterChains());
            } catch (Exception e) {
                Response response = Response.getResponse();
                bundle.getErrorHandler().handleException(e, request, response, ErrorHandler.Pool.SELECTOR);
                sender.send(request, response);
                return;
            }

            executorPool.executeWorkerTask(new HandleTask(sender, chain, request, bundle.getErrorHandler(), bundle.getHttpHandlers(), bundle.getReponseDecorator()));
        }

        private final Object key = new Object();

        @Override
        public Object getKey() {
            return key;
        }
    }

    @AllArgsConstructor
    private static final class HandleTask implements BatchingRunnableTask<Object> {
        private final Sender sender;
        private final FilterChain chain;
        private final Request request;
        private final ErrorHandler errorHandler;
        private final HttpHandler[] handlers;
        private final HttpHandlerResponseDecorator responseDecorator;

        @Override
        public void run() {
            Response response = null;
            try {
                response = chain.handleRequest(request, handlers, responseDecorator);
            } catch (Exception e) {
                response = Response.getResponse();
                errorHandler.handleException(e, request, response, ErrorHandler.Pool.WORKER);
            } finally {
                Debug.log("Response sent to " + request.getRemote().toString());
                sender.send(request, response);
            }
        }

        private final Object key = new Object();

        @Override
        public Object getKey() {
            return key;
        }
    }
}
