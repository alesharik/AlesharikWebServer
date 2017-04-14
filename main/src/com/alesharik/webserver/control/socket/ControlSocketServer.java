package com.alesharik.webserver.control.socket;

import com.alesharik.webserver.api.server.Server;

import java.io.IOException;

@Deprecated
public final class ControlSocketServer extends Server {
    public ControlSocketServer(String host, int port) {
        super(host, port);
    }

    @Override
    public void start() throws IOException {

    }

    @Override
    public void shutdown() throws IOException {

    }

}
