package com.alesharik.webserver.api.control;

import com.alesharik.webserver.api.control.messaging.ControlSocketClientConnection;
import com.alesharik.webserver.configuration.Module;

import javax.annotation.Nonnull;
import java.io.IOException;

public interface ControlSocketClientModule extends Module, ControlSocketClientModuleMXBean {
    /**
     * Return "control-socket-client" - name of module
     */
    @Nonnull
    @Override
    default String getName() {
        return "control-socket-client";
    }

    /**
     * Create new connection to server
     *
     * @param host          server host
     * @param port          server port
     * @param authenticator {@link com.alesharik.webserver.api.control.messaging.ControlSocketClientConnection.Authenticator}
     * @return requested connection with server
     * @throws IOException if anything happens
     */
    ControlSocketClientConnection newConnection(String host, int port, ControlSocketClientConnection.Authenticator authenticator) throws IOException;
}
