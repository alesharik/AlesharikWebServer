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

package com.alesharik.webserver.configuration.module.meta;

import com.alesharik.webserver.configuration.config.lang.element.ConfigurationElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ScriptElementConverter {
    boolean isExecutable(@Nonnull ConfigurationElement element);

    /**
     * Executes element
     *
     * @param element  the element to execute
     * @param expected expected result type class
     * @param <T>      expected result type
     * @return <code>null</code> if executable returns null, otherwise it's value
     * @throws IllegalArgumentException                                                       if element is not an executable element
     * @throws com.alesharik.webserver.configuration.module.ConfigurationError                if returned object doesn't match expected object
     * @throws com.alesharik.webserver.configuration.module.ConfigurationScriptExecutionError if script ended with error
     */
    @Nullable
    <T> T execute(@Nonnull ConfigurationElement element, @Nonnull Class<T> expected);
}
