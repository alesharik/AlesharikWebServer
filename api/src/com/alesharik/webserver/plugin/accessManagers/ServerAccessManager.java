package com.alesharik.webserver.plugin.accessManagers;

import com.alesharik.webserver.api.errorPageGenerators.ErrorPageGenerator;
import com.alesharik.webserver.api.server.RequestHandler;
import org.glassfish.grizzly.websockets.WebSocketApplication;

public interface ServerAccessManager {
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
    void registerNewWebSocket(WebSocketApplication application, String contextPath, String urlPattern);

    void unregisterWebSocket(WebSocketApplication application);

    void addRequestHandler(RequestHandler requestHandler);

    void removeRequestHandler(RequestHandler requestHandler);

    ErrorPageGenerator getErrorPageGenerator();
}
