package com.alesharik.webserver.logger.mx;

public interface LoggerListenerThreadMXBean {
    int getListenerCount();

    long getMessagesPerSecond();

    boolean isEnabled();
}
