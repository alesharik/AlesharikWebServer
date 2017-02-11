package com.alesharik.webserver.reporting;

import com.alesharik.webserver.configuration.Module;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface ReportingModule extends Module, ReportingModuleMXBean {
    @Override
    void parse(Element configNode);

    @Override
    void start();

    @Override
    void shutdown();

    @Override
    void shutdownNow();

    @Override
    String getName();

    /**
     * Register new reporter.
     */
    void registerNewReporter(@Nonnull Reporter reporter);

    void unregisterReporter(@Nonnull Reporter reporter);

    @Override
    int getReporterCount();

    @Override
    int getThreadCount();

    @Override
    ThreadGroup getThreadGroup();

    @Override
    void reportAll();

    @Override
    void reload(Element configNode);

    @Override
    int getActiveReporterCount();
}
