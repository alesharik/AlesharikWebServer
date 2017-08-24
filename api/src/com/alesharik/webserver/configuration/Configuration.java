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

package com.alesharik.webserver.configuration;

import org.w3c.dom.Element;

/**
 * The class, implementing this interface, will use for load all AlesharikWebServer XML configuration.
 * You can see AlesharikWebServer XML configuration documentation <a href="https://github.com/alesharik/AlesharikWebServer/wiki/XML-configuration">here</a>.
 * This class works only with <code>modules</code> and <code>main</code> parts of xml.
 */
public interface Configuration {
    void parseModules(Element modules);

    void parseMain(Element main);

    void parseHook(Element hook);

    /**
     * Clear all user-defined hooks
     */
    void clearHooks();

    Module getModuleByName(String name);

    /**
     * Executes after fatal error. All modules must be stopped by calling {@link Module#shutdownNow()} method. All throwables
     * must not interrupt other modules stopping procedure
     */
    void shutdownNow();

    /**
     * Executes after exit code received. All modules must be stopped by calling {@link Module#shutdown()} method.
     * All throwables must not interrupt other modules stopping procedure
     */
    void shutdown();
}
