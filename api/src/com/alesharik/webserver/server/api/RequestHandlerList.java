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
