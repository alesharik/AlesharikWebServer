package com.alesharik.webserver.reporting;

import com.alesharik.webserver.api.ticking.Tickable;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * Reporter write report data to file
 * @implNote your class must have empty constructor and it will be called at loading
 */
public abstract class Reporter implements Tickable {

    protected Reporter() {
    }

    /**
     * Setup reporter. Will be called only at start.
     *
     * @param file       the file to report
     * @param tickPeriod report period in milliseconds
     */
    public abstract void setup(@Nonnull File file, long tickPeriod, Element config);

    /**
     * Reload config
     *
     * @param config the config element
     */
    public abstract void reload(Element config);

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
