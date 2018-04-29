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

package com.alesharik.webserver.configuration.config.lang.element;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public interface ConfigurationObject extends ConfigurationElement, Appendable {
    @Nullable
    ConfigurationElement getElement(String name);

    @Nullable
    <V extends ConfigurationElement> V getElement(String name, Class<V> clazz);

    Set<String> getNames();

    Map<String, ConfigurationElement> getEntries();

    int getSize();

    boolean hasKey(String name);

    @Override
    default void append(ConfigurationElement element) {
        getEntries().put(element.getName(), element);
    }
}
