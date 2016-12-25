package com.alesharik.webserver.generators;


/**
 * This interface used in {@link ModularErrorPageGenerator} for add additional error page generators<br>
 * <br>
 */
public interface ErrorPageConstructor extends ErrorPageGenerator {
    int getStatus();
}
