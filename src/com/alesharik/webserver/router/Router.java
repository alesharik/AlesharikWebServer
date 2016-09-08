package com.alesharik.webserver.router;

import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.microservices.server.MicroserviceServer;

import java.io.IOException;

public class Router {
    private RouterClient client;

    public Router(int port, String host, MicroserviceServer server) {
        client = new RouterClient(port, host, server);
    }

    public void start() {
        client.start();
    }

    public void shutdown() {
        client.shutdown();
    }

    public String get(String name) {
        try {
            return client.get(name);
        } catch (IOException e) {
            Logger.log(e);
            return "";
        }
    }
}

