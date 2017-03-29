package com.alesharik.webserver.configuration;

import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Module loaded by {@link Configurator} if user uses it.
 * Module will be created WITHOUT calling constructor!
 */
public interface Module {
    void parse(@Nullable Element configNode);

    void reload(@Nullable Element configNode);

    void start();

    void shutdown();

    void shutdownNow();

    @Nonnull
    String getName();

    /**
     * Return module main layer. If layer is <code>null</code>, module don't have layers
     */
    @Nullable
    Layer getMainLayer();
}
