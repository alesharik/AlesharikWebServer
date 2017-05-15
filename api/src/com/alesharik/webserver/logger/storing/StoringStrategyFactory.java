package com.alesharik.webserver.logger.storing;

import java.io.File;

@FunctionalInterface
public interface StoringStrategyFactory<T> {
    T newInstance(File file);
}
