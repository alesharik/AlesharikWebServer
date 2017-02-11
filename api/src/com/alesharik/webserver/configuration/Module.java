package com.alesharik.webserver.configuration;

import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Module {
    void parse(Element configNode);

    void reload(Element configNode);

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
