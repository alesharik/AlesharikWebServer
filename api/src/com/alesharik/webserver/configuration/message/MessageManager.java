package com.alesharik.webserver.configuration.message;

public interface MessageManager<S> {
    S newMessageStream(String subModuleName);
}
