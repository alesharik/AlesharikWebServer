package com.alesharik.webserver.api.control;

import com.alesharik.webserver.configuration.Module;

import javax.annotation.Nonnull;

public interface ControlSocketServerModule extends Module, ControlSocketServerModuleMXBean {

    /**
     * Return "control-socket-server" - name of module
     */
    @Nonnull
    @Override
    default String getName() {
        return "control-socket-server";
    }
}
