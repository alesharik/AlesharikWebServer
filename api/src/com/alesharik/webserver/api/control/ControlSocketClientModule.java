package com.alesharik.webserver.api.control;

import com.alesharik.webserver.api.control.messaging.ControlSocketClientConnection;
import com.alesharik.webserver.configuration.Module;

import javax.annotation.Nonnull;
import java.io.IOException;

public interface ControlSocketClientModule extends Module, ControlSocketClientModuleMXBean {
    @Nonnull
    @Override
    default String getName() {
        return "control-web-socket-client";
    }

    ControlSocketClientConnection newConnection(String host, int port, ControlSocketClientConnection.Authenticator authenticator) throws IOException;
}
