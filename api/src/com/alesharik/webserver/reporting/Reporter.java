/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.alesharik.webserver.reporting;

import com.alesharik.webserver.api.ticking.Tickable;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

/**
 * Reporter write report data to file
 * @implNote your class must have empty constructor and it will be called for instance creation
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
    public abstract void setup(@Nullable File file, long tickPeriod, @Nullable Element config);

    /**
     * Reload config
     *
     * @param config the config element
     */
    public abstract void reload(@Nullable Element config);

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
     * Get reporter unique name
     */
    @Nonnull
    public abstract String getName();
}
