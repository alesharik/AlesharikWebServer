package com.alesharik.webserver.api.control;

import com.alesharik.webserver.configuration.Module;

import javax.annotation.Nonnull;

public interface ControlSocketServerModule extends Module, ControlSocketServerModuleMXBean {

    @Nonnull
    @Override
    default String getName() {
        return "control-web-socket-server";
    }
}
