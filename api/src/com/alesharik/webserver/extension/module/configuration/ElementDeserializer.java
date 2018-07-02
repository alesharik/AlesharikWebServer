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

package com.alesharik.webserver.extension.module.configuration;

import com.alesharik.webserver.configuration.config.lang.element.ConfigurationElement;
import com.alesharik.webserver.extension.module.ConfigurationError;
import com.alesharik.webserver.extension.module.meta.ScriptElementConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Can be bean. Deserialize object from configuration element
 */
public interface ElementDeserializer {
    /**
     * Deserialize object
     *
     * @param element   the config
     * @param converter script element converter
     * @return deserialized object. Return <code>null</code> if configuration for object equals "none"
     * @throws ConfigurationError if configuration has errors
     */
    @Nullable
    Object deserialize(@Nonnull ConfigurationElement element, @Nonnull ScriptElementConverter converter) throws ConfigurationError;
}
