package com.alesharik.webserver.exceptions;

import lombok.Getter;

public class PluginNotFoundException extends RuntimeException {
    @Getter
    private final String plugin;

    public PluginNotFoundException(String plugin) {
        this.plugin = plugin;
    }

    public PluginNotFoundException(String plugin, String message) {
        super(message);
        this.plugin = plugin;
    }
}
