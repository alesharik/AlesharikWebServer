package com.alesharik.webserver.plugin;

import com.alesharik.webserver.configuration.Module;

/**
 * This class used in plugin system in AlesharikWebServer.
 * Your plugin must have at least one {@link Plugin}
 */
@Deprecated
public interface Plugin extends Module {
    /**
     * Called by loader then plugin detected
     */
    void preInit();
}
