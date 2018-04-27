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

package com.alesharik.webserver.configuration.config.lang.parser.elements;

import com.alesharik.webserver.configuration.config.lang.element.ConfigurationElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public final class ObjectImpl implements ConfigurationObject {
    @Getter
    @Nonnull
    private final String name;
    private final Map<String, ConfigurationElement> elements = new HashMap<>();

    @Override
    public ConfigurationElement getElement(String name) {
        return elements.get(name);
    }

    @Override
    public <V extends ConfigurationElement> V getElement(String name, Class<V> clazz) {
        ConfigurationElement element = elements.get(name);
        if(element == null)
            return null;
        return clazz.cast(element);
    }

    @Override
    public Set<String> getNames() {
        return elements.keySet();
    }

    @Override
    public Map<String, ConfigurationElement> getEntries() {
        return elements;
    }

    @Override
    public int getSize() {
        return elements.size();
    }

    @Override
    public boolean hasKey(String name) {
        return elements.containsKey(name);
    }
}
