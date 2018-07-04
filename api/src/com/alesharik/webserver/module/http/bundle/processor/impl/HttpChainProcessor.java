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

package com.alesharik.webserver.module.http.bundle.processor.impl;

import com.alesharik.webserver.module.http.bundle.processor.Filter;
import com.alesharik.webserver.module.http.bundle.processor.Handler;
import com.alesharik.webserver.module.http.bundle.processor.HttpErrorHandler;
import com.alesharik.webserver.module.http.bundle.processor.HttpProcessor;
import com.alesharik.webserver.module.http.http.Request;
import com.alesharik.webserver.module.http.http.Response;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.List;

/**
 * This processor represent filtered handler chain(something like a stream for requests)
 */
@ThreadSafe
public class HttpChainProcessor implements HttpProcessor {
    protected final List<ChainElement> elements = new ArrayList<>();
    protected volatile HttpErrorHandler errorHandler;

    /**
     * Create new instance
     *
     * @return new instance
     */
    @Nonnull
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
                    errorHandler.handleException(e.getCause(), request, response);
                else
                    throw e;
            } catch (Exception e) {
                if(errorHandler == null)
                    throw new ReThrowException(e);
                else
                    errorHandler.handleException(e, request, response);
            }
        }
    }

    /**
     * Add filter to the chain
     *
     * @param filter the filter
     * @return this instance
     */
    @Nonnull
    public HttpChainProcessor filter(@Nonnull Filter filter) {
        elements.add(new FilterElement(filter));
        return this;
    }

    /**
     * Add handler to the chain
     *
     * @param handler the handler
     * @return this instance
     */
    @Nonnull
    public HttpChainProcessor then(@Nonnull Handler handler) {
        elements.add(new HandlerElement(handler));
        return this;
    }

    /**
     * Add processor to the chain
     *
     * @param processor th processor
     * @return this instance
     */
    @Nonnull
    public HttpChainProcessor process(@Nonnull HttpProcessor processor) {
        elements.add(new ProcessorElement(processor));
        return this;
    }

    /**
     * Add local error handler to the chain. It will handle all exceptions from this chain and subprocessors
     *
     * @param httpErrorHandler the error handler
     * @return this instance
     */
    @Nonnull
    public HttpChainProcessor onError(@Nonnull HttpErrorHandler httpErrorHandler) {
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
