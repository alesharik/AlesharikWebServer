package com.alesharik.webserver.reporting;

import com.alesharik.webserver.api.ticking.Tickable;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import java.io.File;

/**
 * Reporter write report data to file
 */
public abstract class Reporter implements Tickable {
    /**
     * Setup reporter. Will called while loading or reloading config.
     *
     * @param file       the file to report
     * @param tickPeriod report period in milliseconds
     */
    public abstract void setup(@Nullable File file, long tickPeriod, Element config);

    @Override
    public void tick() throws Exception {
        Thread.currentThread().setName("Reporter: " + getName());
        report();
        Thread.currentThread().setName("Reporter: none");
    }

    /**
     * Report cycle. Executes every <code>tickPeriod</code> milliseconds.
     *
     * @throws Exception if anything happen
     */
    protected abstract void report() throws Exception;

    /**
     * Shutdown reporter gracefully
     */
    public abstract void shutdown();

    /**
     * Shutdown reporter now
     */
    public abstract void shutdownNow();

    /**
     * Get reporter name
     */
    public abstract String getName();
}
