package com.alesharik.webserver.configuration;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * SubModule is part of module. It is useful for split code into small independent pieces
 *
 * @implNote Implementation must be thread-safe
 */
@ThreadSafe
public interface SubModule {
    /**
     * Return submodule unique name
     */
    @Nonnull
    String getName();

    /**
     * Start submodule
     */
    void start();

    /**
     * Shutdown submodule without waiting
     */
    void shutdownNow();

    /**
     * Shutdown submodule gracefully
     */
    void shutdown();

    /**
     * Reload submodule(if config changed, etc) - shutdown and start it
     */
    default void reload() {
        shutdown();
        start();
    }

    /**
     * Return <code>true</code> if module started, overwise <code>false</code>
     */
    boolean isRunning();
}
