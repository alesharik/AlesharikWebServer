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

package com.alesharik.webserver.module.http.server.impl.handler;

import com.alesharik.webserver.api.cache.object.CachedObjectFactory;
import com.alesharik.webserver.api.cache.object.Recyclable;
import com.alesharik.webserver.api.cache.object.SmartCachedObjectFactory;
import com.alesharik.webserver.api.name.Named;
import com.alesharik.webserver.logger.Debug;
import com.alesharik.webserver.module.http.addon.AddOn;
import com.alesharik.webserver.module.http.addon.AddOnSocketHandler;
import com.alesharik.webserver.module.http.addon.Message;
import com.alesharik.webserver.module.http.addon.MessageProcessor;
import com.alesharik.webserver.module.http.addon.MessageProcessorContext;
import com.alesharik.webserver.module.http.addon.MessageProcessorParameters;
import com.alesharik.webserver.module.http.addon.MessageSender;
import com.alesharik.webserver.module.http.bundle.ErrorHandler;
import com.alesharik.webserver.module.http.bundle.HttpHandlerBundle;
import com.alesharik.webserver.module.http.bundle.processor.HttpProcessor;
import com.alesharik.webserver.module.http.bundle.processor.impl.ReThrowException;
import com.alesharik.webserver.module.http.http.Request;
import com.alesharik.webserver.module.http.http.Response;
import com.alesharik.webserver.module.http.server.BatchingRunnableTask;
import com.alesharik.webserver.module.http.server.ExecutorPool;
import com.alesharik.webserver.module.http.server.HttpRequestHandler;
import com.alesharik.webserver.module.http.server.Sender;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Named("default")
public class DefaultHttpRequestHandler implements HttpRequestHandler {
    private final List<HttpHandlerBundle> bundles;

    public DefaultHttpRequestHandler(List<HttpHandlerBundle> bundles) {
        this.bundles = bundles;
    }

    @Override
    public void handleRequest(Request request, ExecutorPool executorPool, Sender sender) {
        executorPool.executeWorkerTask(BundleSelectTask.create(bundles, request, executorPool, sender));
    }

    @Override
    public AddOnSocketHandler getAddOnSocketHandler(Request request, ExecutorPool executorPool, AddOn addOn) {
        HttpHandlerBundle bundle = null;
        for(HttpHandlerBundle httpHandlerBundle : bundles) {
            if(httpHandlerBundle.getValidator().isRequestValid(request)) {
                bundle = httpHandlerBundle;
                break;
            }
        }
        if(bundle != null)
            //noinspection unchecked
            return addOn.getHandler(bundle.getMessageProcessor(addOn.getName(), new MessageProcessorParametersImpl(request)));
        return null;
    }

    @Override
    public void handleMessage(MessageProcessor messageProcessor, Message message, MessageProcessorContext context, MessageSender messageSender, ExecutorPool executorPool, AddOn addOn, Object sync) {
        //noinspection unchecked
        executorPool.executeWorkerTask(HandleMessageTask.create(messageProcessor, message, messageSender, context, sync));
    }

    @Override
    public void handleMessageTask(Runnable task, ExecutorPool executorPool, AddOn addOn, Object sync) {
        executorPool.executeWorkerTask(MessageTaskImpl.create(task, sync));
    }

    private static final class BundleSelectTask implements BatchingRunnableTask<Object>, Recyclable {
        private static final CachedObjectFactory<BundleSelectTask> FACTORY = new SmartCachedObjectFactory<>(BundleSelectTask::new);
        @Getter
        private final Object key = new Object();

        private List<HttpHandlerBundle> bundles;
        private Request request;
        private ExecutorPool executorPool;
        private Sender sender;

        public static BatchingRunnableTask create(List<HttpHandlerBundle> bundles, Request request, ExecutorPool executorPool, Sender sender) {
            BundleSelectTask task = FACTORY.getInstance();
            task.bundles = bundles;
            task.request = request;
            task.executorPool = executorPool;
            task.sender = sender;
            return task;
        }

        @Override
        public void run() {
            HttpHandlerBundle bundle = null;
            for(HttpHandlerBundle httpHandlerBundle : bundles) {
                if(httpHandlerBundle.getValidator().isRequestValid(request)) {
                    bundle = httpHandlerBundle;
                    break;
                }
            }
            Debug.log("Request from " + request.getRemote().toString() + ": " + request.getRawUri());
            if(bundle == null) {
                System.err.println("Bundle not found for " + request.getContextPath());
                return;
            }

            executorPool.executeWorkerTask(HandleTask.create(sender, request, bundle.getErrorHandler(), bundle.getProcessor()));

            FACTORY.putInstance(this);
        }

        @Override
        public void recycle() {
            bundles = null;
            request = null;
            executorPool = null;
            sender = null;
        }
    }

    private static final class HandleTask implements BatchingRunnableTask<Object>, Recyclable {
        private static final CachedObjectFactory<HandleTask> FACTORY = new SmartCachedObjectFactory<>(HandleTask::new);
        @Getter
        private final Object key = new Object();
        private Sender sender;
        private Request request;
        private ErrorHandler errorHandler;
        private HttpProcessor processor;

        public static HandleTask create(Sender sender, Request request, ErrorHandler errorHandler, HttpProcessor processor) {
            HandleTask task = FACTORY.getInstance();
            task.sender = sender;
            task.request = request;
            task.errorHandler = errorHandler;
            task.processor = processor;
            return task;
        }

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
                Debug.log("Response sent to " + request.getRemote().toString() + ": " + response.getResponseCode());
                sender.send(request, response);
            }
            FACTORY.putInstance(this);
        }

        @Override
        public void recycle() {
            sender = null;
            request = null;
            errorHandler = null;
            processor = null;
        }
    }

    @RequiredArgsConstructor
    private static final class HandleMessageTask implements BatchingRunnableTask<Object>, Recyclable {
        private static final CachedObjectFactory<HandleMessageTask> FACTORY = new SmartCachedObjectFactory<>(HandleMessageTask::new);

        private MessageProcessor<Message, MessageSender<Message>, MessageProcessorContext> processor;
        private Message message;
        private MessageSender<Message> messageSender;
        private Object sync;
        private MessageProcessorContext context;

        public static HandleMessageTask create(MessageProcessor<Message, MessageSender<Message>, MessageProcessorContext> processor, Message message, MessageSender<Message> sender, MessageProcessorContext context, Object sync) {
            HandleMessageTask task = FACTORY.getInstance();
            task.processor = processor;
            task.message = message;
            task.messageSender = sender;
            task.sync = sync;
            task.context = context;
            return task;
        }

        @Override
        public Object getKey() {
            return sync;
        }

        @Override
        public void run() {
            processor.processMessage(message, messageSender, context);
            FACTORY.putInstance(this);
        }

        @Override
        public void recycle() {
            processor = null;
            messageSender = null;
            message = null;
            sync = null;
            context = null;
        }
    }

    private static final class MessageTaskImpl implements BatchingRunnableTask<Object>, Recyclable {
        private static final CachedObjectFactory<MessageTaskImpl> FACTORY = new SmartCachedObjectFactory<>(MessageTaskImpl::new);
        private Runnable runnable;
        private Object sync;

        public static MessageTaskImpl create(Runnable runnable, Object sync) {
            MessageTaskImpl task = FACTORY.getInstance();
            task.runnable = runnable;
            task.sync = sync;
            return task;
        }

        @Override
        public Object getKey() {
            return sync;
        }

        @Override
        public void run() {
            runnable.run();
            FACTORY.putInstance(this);
        }

        @Override
        public void recycle() {
            sync = null;
            runnable = null;
        }
    }

    private static final class MessageProcessorParametersImpl implements MessageProcessorParameters {
        private final Request request;

        public MessageProcessorParametersImpl(Request request) {
            this.request = request.clone();
        }

        @Override
        public String getPath() {
            return request.getContextPath();
        }

        @Override
        public Request getHandshakeRequest() {
            return request;
        }
    }
}
