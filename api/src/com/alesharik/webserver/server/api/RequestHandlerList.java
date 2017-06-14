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

package com.alesharik.webserver.server.api;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * This class used for hold multiple {@link RequestHandler}'s in one {@link RequestHandler}
 */
@Deprecated
public final class RequestHandlerList implements RequestHandler {//TODO rewrite and tests
    private ArrayList<RequestHandler> handlers = new ArrayList<>();
    private RequestHandler currentHandler;

    public RequestHandlerList() {
    }

    public void add(RequestHandler handler) {
        handlers.add(handler);
    }

    public boolean contains(RequestHandler handler) {
        return handlers.contains(handler);
    }

    public void remove(RequestHandler handler) {
        handlers.remove(handler);
    }

    public void forEach(Consumer<RequestHandler> consumer) {
        handlers.forEach(consumer);
    }

    public Iterator<RequestHandler> iterator() {
        return handlers.iterator();
    }

    @Override
    public boolean canHandleRequest(Request request) throws IOException {
        for(RequestHandler handler : handlers) {
            if(handler.canHandleRequest(request)) {
                currentHandler = handler;
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleRequest(Request request, Response response) throws Exception {
        currentHandler.handleRequest(request, response);
    }
}
