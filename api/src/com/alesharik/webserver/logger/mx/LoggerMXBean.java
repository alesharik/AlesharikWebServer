package com.alesharik.webserver.logger.mx;

public interface LoggerMXBean {
    String getLogFile();

    int getMessageQueueCapacity();

    int getNamedLoggerCount();

    long getMessagesParsedPerSecond();
}
