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

import com.alesharik.webserver.api.server.wrapper.bundle.processor.HttpErrorHandler;
import com.alesharik.webserver.api.server.wrapper.bundle.processor.HttpProcessor;
import com.alesharik.webserver.api.server.wrapper.http.Request;
import com.alesharik.webserver.api.server.wrapper.http.Response;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import javax.annotation.Nonnull;

public class HttpRouterProcessor implements HttpProcessor {
    protected final Trie<String, HttpProcessor> processors = new PatriciaTrie<>();
    protected volatile HttpErrorHandler errorHandler;
    protected volatile HttpProcessor def;

    @Override
    public void process(@Nonnull Request request, @Nonnull Response response) {
        try {
            HttpProcessor processor = processors.get(request.getContextPath());
            if(processor == null)
                processor = def;
            if(processor == null)
                return;
            processor.process(request, response);
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

    public HttpRouterProcessor path(String path, HttpProcessor processor) {
        processors.put(path, processor);
        return this;
    }

    public HttpRouterProcessor defaultPath(HttpProcessor processor) {
        def = processor;
        return this;
    }

    public HttpRouterProcessor onError(HttpErrorHandler httpErrorHandler) {
        errorHandler = httpErrorHandler;
        return this;
    }
}
