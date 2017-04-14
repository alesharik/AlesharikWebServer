package com.alesharik.webserver.control.socket;

import com.alesharik.webserver.api.control.ControlSocketServerModule;
import com.alesharik.webserver.api.control.messaging.ControlSocketServerConnection;
import com.alesharik.webserver.configuration.Layer;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class ControlSocketServerModuleImpl implements ControlSocketServerModule {
    @Override
    public int connectionCount() {
        return 0;
    }

    @Override
    public List<ControlSocketServerConnection> getConnections() {
        return null;
    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public Set<String> getListenAddresses() {
        return null;
    }

    @Override
    public void parse(@Nullable Element configNode) {

    }

    @Override
    public void reload(@Nullable Element configNode) {

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void shutdownNow() {

    }

    @Nullable
    @Override
    public Layer getMainLayer() {
        return null;
    }
}
