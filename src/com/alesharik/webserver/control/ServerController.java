package com.alesharik.webserver.control;

import java.net.URI;

public class ServerController {

    public WebSocketClientEndpoint connect(String server, String login, String password) throws Exception {
        WebSocketClientEndpoint endpoint = new WebSocketClientEndpoint(new URI(server), login, password);
        endpoint.sendMessage("Hello");
        return endpoint;
    }
}

