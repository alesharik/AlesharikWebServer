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
