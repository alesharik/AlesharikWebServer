package com.alesharik.webserver.logger.storing;

import java.io.File;

/**
 * {@link com.alesharik.webserver.logger.NamedLogger} use this class for create new {@link StoringStrategy}
 *
 * @param <T> storing strategy
 */
@FunctionalInterface
public interface StoringStrategyFactory<T extends StoringStrategy> {
    /**
     * Create new instance
     * @param file log file
     * @return new instance
     */
    T newInstance(File file);
}
