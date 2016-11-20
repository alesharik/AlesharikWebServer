package com.alesharik.webserver.microservices.client;

import com.alesharik.webserver.api.Utils;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefix;
import com.alesharik.webserver.microservices.api.MicroserviceEvent;
import com.alesharik.webserver.microservices.server.MicroserviceServer;
import com.alesharik.webserver.plugin.accessManagers.MicroserviceAccessManagerBuilder;
import com.alesharik.webserver.router.Router;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Prefix("[MicroserviceClient]")
public class MicroserviceClient {
    private MicroserviceClientExecutorPool pool;
    private MicroserviceServer server;
    private Router router;

    public MicroserviceClient(WorkingMode mode, String routerIp, int routerHost) {
        pool = new MicroserviceClientExecutorPool(mode);
        router = new Router(routerHost, routerIp);

        if(mode == WorkingMode.ADVANCED) {
            //FIXME
            server = new MicroserviceServer(Utils.getExternalIp(), 6800, MicroserviceServer.WorkingMode.SIMPLE, Utils.getExternalIp(), 6000);
        }
        Logger.log("Microservice client successfully initialized!");
    }

    public void start() throws IOException {
        if(server != null) {
            server.start();
            router.start();
        }
    }

    public void shutdown() {
        if(server != null) {
            server.shutdown();
            router.shutdown();
        }
    }

    public void send(String microserviceName, MicroserviceEvent message, String address) {
        try {
            pool.send(address, microserviceName, message);
        } catch (IOException e) {
            Logger.log(e);
        }
    }

    //TODO rewrite this
    public void send(String microserviceName, MicroserviceEvent message) {
        String address = null;
        try {
            address = router.getIpForMicroservice(microserviceName).get();
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        send(microserviceName, message, address);
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
