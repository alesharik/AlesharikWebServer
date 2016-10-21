package com.alesharik.webserver.plugin.accessManagers;

import com.alesharik.webserver.api.server.RequestHandler;
import com.alesharik.webserver.api.server.WebServer;
import com.alesharik.webserver.generators.ModularErrorPageGenerator;
import lombok.Getter;
import org.glassfish.grizzly.websockets.WebSocketApplication;

public final class ServerAccessManager {
    WebServer server;
    @Getter
    ModularErrorPageGenerator errorPageGenerator;

    ServerAccessManager() {

    }

    /**
     * Register a WebSocketApplication to a specific context path and url pattern.
     * If you wish to associate this application with the root context, use an
     * empty string for the contextPath argument.
     * <p>
     * <pre>
     * Examples:
     *   // WS application will be invoked:
     *   //    ws://localhost:8080/echo
     *   // WS application will not be invoked:
     *   //    ws://localhost:8080/foo/echo
     *   //    ws://localhost:8080/echo/some/path
     *   registerNewWebSocket(webSocketApplication, "", "/echo");
     *
     *   // WS application will be invoked:
     *   //    ws://localhost:8080/echo
     *   //    ws://localhost:8080/echo/some/path
     *   // WS application will not be invoked:
     *   //    ws://localhost:8080/foo/echo
     *   registerNewWebSocket(webSocketApplication, "", "/echo/*");
     *
     *   // WS application will be invoked:
     *   //    ws://localhost:8080/context/echo
     *
     *   // WS application will not be invoked:
     *   //    ws://localhost:8080/echo
     *   //    ws://localhost:8080/context/some/path
     *   registerNewWebSocket(webSocketApplication, "/context", "/echo");
     * </pre>
     *
     * @param contextPath the context path
     * @param urlPattern  url pattern
     * @param application the WebSocket application.
     */
    public void registerNewWebSocket(WebSocketApplication application, String contextPath, String urlPattern) {
        server.registerNewWebSocket(application, contextPath, urlPattern);
    }

    public void unregisterWebSocket(WebSocketApplication application) {
        server.unregisterWebSocket(application);
    }

    public void addRequestHandler(RequestHandler requestHandler) {
        server.addRequestHandler(requestHandler);
    }

    public void removeRequestHandler(RequestHandler requestHandler) {
        server.removeRequestHandler(requestHandler);
    }
}
