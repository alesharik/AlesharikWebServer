package com.alesharik.webserver.configuration;

import com.alesharik.webserver.configuration.message.MessageManager;

public interface SubModule {
    void setMessageManager(MessageManager manager);

    String getName();

    void start();

    void shutdownNow();

    void reload();
}
