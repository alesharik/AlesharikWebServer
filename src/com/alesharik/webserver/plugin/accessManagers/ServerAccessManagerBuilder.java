package com.alesharik.webserver.plugin.accessManagers;

import com.alesharik.webserver.api.server.Server;
import com.alesharik.webserver.generators.ModularErrorPageGenerator;

public class ServerAccessManagerBuilder {
    private Server server;
    private ModularErrorPageGenerator errorPageGenerator;

    public void setServer(Server server) {
        this.server = server;
    }

    public void setErrorPageGenerator(ModularErrorPageGenerator errorPageGenerator) {
        this.errorPageGenerator = errorPageGenerator;
    }

    public void build() {

    }
}
