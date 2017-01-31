package com.alesharik.webserver.configuration;

import org.w3c.dom.Element;

public interface Module {
    void parse(Element configNode);

    void start();

    void shutdown();

    void shutdownNow();

    String getName();
}
