package com.alesharik.webserver.plugin.accessManagers;

import com.alesharik.webserver.api.server.WebServer;
import com.alesharik.webserver.generators.ModularErrorPageGenerator;

public class ServerAccessManagerBuilder {
    private WebServer server;
    private ModularErrorPageGenerator errorPageGenerator;

    public void setServer(WebServer server) {
        this.server = server;
    }

    public void setErrorPageGenerator(ModularErrorPageGenerator errorPageGenerator) {
        this.errorPageGenerator = errorPageGenerator;
    }

    public ServerAccessManager build() {
        ServerAccessManager manager = new ServerAccessManager();
        manager.server = server;
        manager.errorPageGenerator = errorPageGenerator;
        return manager;
    }
}
