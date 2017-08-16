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

package com.alesharik.webserver.api.server.wrapper.bundle.impl;

import com.alesharik.webserver.api.server.wrapper.bundle.Filter;
import com.alesharik.webserver.api.server.wrapper.bundle.FilterChain;
import com.alesharik.webserver.api.server.wrapper.bundle.HttpHandler;
import com.alesharik.webserver.api.server.wrapper.bundle.HttpHandlerResponseDecorator;
import com.alesharik.webserver.api.server.wrapper.http.Request;
import com.alesharik.webserver.api.server.wrapper.http.Response;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BasicFilterChain implements FilterChain {
    private final List<Filter> filters;

    public BasicFilterChain() {
        filters = new CopyOnWriteArrayList<>();
    }

    public BasicFilterChain with(Filter filter) {
        filters.add(filter);
        return this;
    }

    @Override
    public boolean accept(Request request, Response response) {
        return true;
    }

    @Nonnull
    @Override
    public Response handleRequest(Request request, HttpHandler[] httpHandlers, HttpHandlerResponseDecorator decorator) {
        Response response = Response.getResponse();
        for(Filter filter : filters) {
            if(!filter.accept(request, response))
                return response;
        }
        for(HttpHandler httpHandler : httpHandlers) {
            if(httpHandler.getFilter().accept(request, response)) {
                httpHandler.handle(request, response);
                decorator.decorate(request, response, false);
                return response;
            }
        }
        decorator.decorate(request, response, true);
        return response;
    }
}
