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

package com.alesharik.webserver.extension.module;

import com.alesharik.webserver.configuration.config.lang.element.ConfigurationElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObject;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObjectArray;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationPrimitive;
import com.alesharik.webserver.extension.module.meta.ScriptElementConverter;
import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class ConfigurationUtils {
    public static Optional<String> getString(String name, ConfigurationElement element, ScriptElementConverter converter) {
        if(element == null)
            return Optional.empty();
        else if(converter.isExecutable(element))
            return Optional.of(converter.execute(element, String.class));
        else if(element instanceof ConfigurationPrimitive.String)
            return Optional.of(((ConfigurationPrimitive.String) element).value());
        else
            throw new ConfigurationError("Unexpected type of " + name + " element! String expected");
    }

    public static Optional<Integer> getInteger(String name, ConfigurationElement element, ScriptElementConverter converter) {
        if(element == null)
            return Optional.empty();
        else if(converter.isExecutable(element))
            return Optional.of(converter.execute(element, Integer.class));
        else if(element instanceof ConfigurationPrimitive.Int)
            return Optional.of(((ConfigurationPrimitive.Int) element).value());
        else
            throw new ConfigurationError("Unexpected type of " + name + " element! Integer expected");
    }

    public static Optional<Boolean> getBoolean(String name, ConfigurationElement element, ScriptElementConverter converter) {
        if(element == null)
            return Optional.empty();
        else if(converter.isExecutable(element))
            return Optional.of(converter.execute(element, Boolean.class));
        else if(element instanceof ConfigurationPrimitive.Boolean)
            return Optional.of(((ConfigurationPrimitive.Boolean) element).value());
        else
            throw new ConfigurationError("Unexpected type of " + name + " element! Integer expected");
    }

    public static Optional<ConfigurationObject> getObject(String name, ConfigurationElement element, ScriptElementConverter converter) {
        if(element == null)
            return Optional.empty();
        else if(element instanceof ConfigurationObject)
            return Optional.of((ConfigurationObject) element);
        else
            throw new ConfigurationError("Unexpected type of " + name + " element! Object expected");
    }

    public static Optional<ConfigurationObjectArray> getArray(String name, ConfigurationElement element, ScriptElementConverter converter) {
        if(element == null)
            return Optional.empty();
        else if(element instanceof ConfigurationObjectArray)
            return Optional.of((ConfigurationObjectArray) element);
        else
            throw new ConfigurationError("Unexpected type of " + name + " element! Object expected");
    }
}
