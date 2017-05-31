package com.alesharik.webserver.logger.mx;

public interface NamedLoggerMXBean {
    String getName();

    /**
     * Return logger storing strategy class canonical name if exists, overwise class name
     */
    String getStoringStrategy();

    String getFile();

    String getDefaultPrefix();
}
