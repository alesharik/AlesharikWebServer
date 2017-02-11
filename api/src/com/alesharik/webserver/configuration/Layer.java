package com.alesharik.webserver.configuration;

public interface Layer {
    SubModule[] getSubModules();

    Layer[] getSubLayers();

    String getName();
}
