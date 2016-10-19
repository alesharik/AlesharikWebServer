package com.alesharik.webserver.logger.storingStrategies;

import java.io.File;

@FunctionalInterface
public interface StoringStrategyFactory<T> {
    T newInstance(File file);
}
