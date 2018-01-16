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

package com.alesharik.webserver.api.server.wrapper.bundle.processor.impl;

import com.alesharik.webserver.api.server.wrapper.bundle.processor.Filter;
import com.alesharik.webserver.api.server.wrapper.bundle.processor.Handler;
import com.alesharik.webserver.api.server.wrapper.bundle.processor.HttpErrorHandler;
import com.alesharik.webserver.api.server.wrapper.bundle.processor.HttpProcessor;
import com.alesharik.webserver.api.server.wrapper.http.Request;
import com.alesharik.webserver.api.server.wrapper.http.Response;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.List;

@ThreadSafe
public class HttpChainProcessor implements HttpProcessor {
    protected final List<ChainElement> elements = new ArrayList<>();
    protected volatile HttpErrorHandler errorHandler;

    public static HttpChainProcessor chain() {
        return new HttpChainProcessor();
    }

    @Override
    public void process(@Nonnull Request request, @Nonnull Response response) {
        for(ChainElement element : elements) {
            try {
                if(!element.process(request, response))
                    return;
            } catch (ReThrowException e) {
                if(errorHandler != null)
                    errorHandler.handleException(e.getCause());
                else
                    throw e;
            } catch (Exception e) {
                if(errorHandler == null)
                    throw new ReThrowException(e);
                else
                    errorHandler.handleException(e);
            }
        }
    }

    public HttpChainProcessor filter(Filter filter) {
        elements.add(new FilterElement(filter));
        return this;
    }

    public HttpChainProcessor then(Handler handler) {
        elements.add(new HandlerElement(handler));
        return this;
    }

    public HttpChainProcessor process(HttpProcessor processor) {
        elements.add(new ProcessorElement(processor));
        return this;
    }

    public HttpChainProcessor onError(HttpErrorHandler httpErrorHandler) {
        errorHandler = httpErrorHandler;
        return this;
    }

    private static abstract class ChainElement {
        public abstract boolean process(Request request, Response response) throws Exception;
    }

    private static final class FilterElement extends ChainElement {
        private final Filter filter;

        private FilterElement(Filter filter) {
            this.filter = filter;
        }

        @Override
        public boolean process(Request request, Response response) throws Exception {
            return filter.filter(request, response);
        }
    }

    private static final class HandlerElement extends ChainElement {
        private final Handler handler;

        private HandlerElement(Handler handler) {
            this.handler = handler;
        }

        @Override
        public boolean process(Request request, Response response) throws Exception {
            handler.handle(request, response);
            return true;
        }
    }

    private static final class ProcessorElement extends ChainElement {
        private final HttpProcessor processor;

        private ProcessorElement(HttpProcessor processor) {
            this.processor = processor;
        }

        @Override
        public boolean process(Request request, Response response) throws Exception {
            processor.process(request, response);
            return true;
        }
    }
}
