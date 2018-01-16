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
import com.alesharik.webserver.api.server.wrapper.bundle.ErrorHandler;
import com.alesharik.webserver.api.server.wrapper.bundle.HttpHandlerBundle;
import com.alesharik.webserver.api.server.wrapper.bundle.processor.HttpProcessor;
import com.alesharik.webserver.api.server.wrapper.bundle.processor.impl.ReThrowException;
import com.alesharik.webserver.api.server.wrapper.http.Request;
import com.alesharik.webserver.api.server.wrapper.http.Response;
import com.alesharik.webserver.api.server.wrapper.server.BatchingRunnableTask;
import com.alesharik.webserver.api.server.wrapper.server.ExecutorPool;
import com.alesharik.webserver.api.server.wrapper.server.HttpRequestHandler;
import com.alesharik.webserver.api.server.wrapper.server.Sender;
import com.alesharik.webserver.logger.Debug;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Named("default-http-request-handler")
public class DefaultHttpRequestHandler implements HttpRequestHandler {
    private final Set<HttpHandlerBundle> bundles;

    public DefaultHttpRequestHandler(Set<HttpHandlerBundle> bundles) {
        this.bundles = bundles;
    }

    @Override
    public void handleRequest(Request request, ExecutorPool executorPool, Sender sender) {
        executorPool.executeWorkerTask(new BundleSelectTask(bundles, request, executorPool, sender));
    }

    @AllArgsConstructor
    private static final class BundleSelectTask implements BatchingRunnableTask<Object> {
        private final Set<HttpHandlerBundle> bundles;
        private final Request request;
        private final ExecutorPool executorPool;
        private final Sender sender;
        private final Object key = new Object();

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

            executorPool.executeWorkerTask(new HandleTask(sender, request, bundle.getErrorHandler(), bundle.getProcessor()));
        }

        @Override
        public Object getKey() {
            return key;
        }
    }

    @RequiredArgsConstructor
    private static final class HandleTask implements BatchingRunnableTask<Object> {
        private final Sender sender;
        private final Request request;
        private final ErrorHandler errorHandler;
        private final HttpProcessor processor;
        private final Object key = new Object();

        @Override
        public void run() {
            Response response = null;
            try {
                response = Response.getResponse();
                processor.process(request, response);
            } catch (ReThrowException e) {
                response = Response.getResponse();
                errorHandler.handleException(e.getCause(), request, response, ErrorHandler.Pool.WORKER);
            } catch (Exception e) {
                response = Response.getResponse();
                errorHandler.handleException(e, request, response, ErrorHandler.Pool.WORKER);
            } finally {
                Debug.log("Response sent to " + request.getRemote().toString());
                sender.send(request, response);
            }
        }

        @Override
        public Object getKey() {
            return key;
        }
    }
}
