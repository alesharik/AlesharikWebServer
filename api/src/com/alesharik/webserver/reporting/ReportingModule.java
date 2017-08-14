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

import com.alesharik.webserver.configuration.Module;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Reporting module manages all {@link Reporter}s
 */
@ThreadSafe
public interface ReportingModule extends Module, ReportingModuleMXBean {
    /**
     * Add reporter to active reporters
     *
     * @param reporter already initiated reporter
     */
    void enableReporter(Reporter reporter);

    /**
     * Add reporter to active reporters
     *
     * @param reporter already initiated reporter
     * @param time     tick time in milliseconds
     */
    void enableReporter(Reporter reporter, long time);

    /**
     * Remove reporter from active reporters
     *
     * @param reporter already initiated reporter
     */
    void disableReporter(Reporter reporter);

    @Nonnull
    @Override
    default String getName() {
        return "reporting-module";
    }
}
