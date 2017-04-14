package com.alesharik.webserver.control.socket;

import java.util.concurrent.ConcurrentHashMap;

@Deprecated
public class ControlSocketManagerImpl implements ControlSocketManager {
    private final ConcurrentHashMap<String, ControlSocketHandlerFactory> factories;

    public ControlSocketManagerImpl() {
        factories = new ConcurrentHashMap<>();
    }

    public void receiveMessage(String name, String message) {

    }

    public void receiveMessage(String name, Object message) {

    }

    @Override
    public void registerNewControlSocketHandlerFactory(String name, ControlSocketHandlerFactory factory) {

    }

    @Override
    public void unregisterControlSocketHandlerFactory(String name, ControlSocketHandlerFactory factory) {

    }
}
