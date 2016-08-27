package com.alesharik.webserver.microservices.client;

import com.alesharik.webserver.api.Utils;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefix;
import com.alesharik.webserver.microservices.api.MicroserviceEvent;
import com.alesharik.webserver.microservices.server.MicroserviceServer;
import com.alesharik.webserver.plugin.accessManagers.MicroserviceAccessManagerBuilder;

import java.io.IOException;

@Prefix("[MicroserviceClient}")
public class MicroserviceClient {
    private MicroserviceClientExecutorPool pool;
    private MicroserviceServer server;

    public MicroserviceClient(WorkingMode mode) {
        pool = new MicroserviceClientExecutorPool(mode);

        if(mode == WorkingMode.ADVANCED) {
            server = new MicroserviceServer(Utils.getExternalIp(), 6800, MicroserviceServer.WorkingMode.SIMPLE);
        }
        Logger.log("Microservice client successfully initialized!");
    }

    public void start() throws IOException {
        if(server != null) {
            server.start();
        }
    }

    public void shutdown() {
        if(server != null) {
            server.shutdown();
        }
    }

    public void send(String microserviceName, MicroserviceEvent message, String address) {
        try {
            pool.send(address, microserviceName, message);
        } catch (IOException e) {
            Logger.log(e);
        }
    }

    public void setupMicroserviceAccessMangerBuilder(MicroserviceAccessManagerBuilder builder) {
        builder.setServer(server);
        builder.setClient(this);
    }

    public enum WorkingMode {
        SIMPLE,
        ADVANCED
    }
}
