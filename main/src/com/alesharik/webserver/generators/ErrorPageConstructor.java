package com.alesharik.webserver.generators;

import org.glassfish.grizzly.http.server.ErrorPageGenerator;

/**
 * This interface used in {@link ModularErrorPageGenerator} for add additional error page generators<br>
 * <br>
 * Extends {@link ErrorPageGenerator}
 */
public interface ErrorPageConstructor extends ErrorPageGenerator {
    int getStatus();
}
