package com.alesharik.webserver.logger.mx;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface NamedLoggerMXBean {
    /**
     * Return logger name
     */
    @Nonnull
    String getName();

    /**
     * Return logger storing strategy class canonical name if exists, overwise class name.
     * If logger doesn't have strategy, return empty string
     */
    @Nonnull
    String getStoringStrategy();

    /**
     * Return logger file path
     */
    @Nullable
    String getFile();

    /**
     * Return logger default prefix
     */
    @Nonnull
    String getDefaultPrefix();
}
