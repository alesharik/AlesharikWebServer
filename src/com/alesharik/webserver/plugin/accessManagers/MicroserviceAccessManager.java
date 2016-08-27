package com.alesharik.webserver.plugin.accessManagers;

import com.alesharik.webserver.microservices.api.Microservice;
import com.alesharik.webserver.microservices.api.MicroserviceEvent;
import com.alesharik.webserver.microservices.client.MicroserviceClient;
import com.alesharik.webserver.microservices.server.MicroserviceServer;

public class MicroserviceAccessManager {
    MicroserviceClient client;
    MicroserviceServer server;
    private boolean isServer;

    MicroserviceAccessManager(boolean isServer) {
        this.isServer = isServer;
    }

    public MicroserviceClient getClient() {
        return this.client;
    }

    public boolean isServer() {
        return isServer;
    }

    public void registerMicroservice(String microserviceName, Microservice microservice) {
        server.registerMicroservice(microserviceName, microservice);
    }

    public void send(String microserviceName, MicroserviceEvent event, String address) {
        client.send(microserviceName, event, address);
    }
}
