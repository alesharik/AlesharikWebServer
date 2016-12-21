package com.alesharik.webserver.plugin.accessManagers;

import com.alesharik.webserver.microservices.client.MicroserviceClient;
import com.alesharik.webserver.microservices.server.MicroserviceServer;

public class MicroserviceAccessManagerBuilder {
    private MicroserviceServer server = null;
    private MicroserviceClient client = null;
    private boolean isServer;

    public MicroserviceAccessManagerBuilder(boolean isServer) {
        this.isServer = isServer;
    }

    public void setServer(MicroserviceServer server) {
        this.server = server;
    }

    public void setClient(MicroserviceClient client) {
        this.client = client;
    }

    public MicroserviceAccessManager build() {
        MicroserviceAccessManager manager = new MicroserviceAccessManager(isServer);
        if(isServer) {
            if(server == null || client == null) {
                throw new IllegalArgumentException();
            }
        } else {
            if(client == null) {
                throw new IllegalArgumentException();
            }
        }
        manager.server = server;
        manager.client = client;
        return manager;
    }
}
