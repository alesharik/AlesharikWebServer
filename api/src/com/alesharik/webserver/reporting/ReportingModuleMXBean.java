package com.alesharik.webserver.reporting;

public interface ReportingModuleMXBean {
    /**
     * All registered reporter count
     */
    int getReporterCount();

    int getActiveReporterCount();

    /**
     * TickingPool thread count
     */
    int getThreadCount();

    /**
     * {@link ThreadGroup} of current {@link ReportingModule}
     */
    ThreadGroup getThreadGroup();

    /**
     * Execute all reporter's <code>tick()</code> method in caller thread
     */
    void reportAll();
}
