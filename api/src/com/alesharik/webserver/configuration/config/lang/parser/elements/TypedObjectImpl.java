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
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationTypedObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public final class TypedObjectImpl implements ConfigurationTypedObject {
    @Getter
    private final String name;
    @Getter
    private final String type;

    private final Map<String, ConfigurationElement> elements = new HashMap<>();

    public TypedObjectImpl(String name, String type, ConfigurationTypedObject extend) {
        this.name = name;
        this.type = type;
        elements.putAll(extend.getEntries());
    }

    /**
     * @return null - format error
     */
    @Nullable
    public static TypedObjectImpl parse(String description) {
        String[] parts = description.split(":", 2);
        if(parts.length < 2 || parts[0].isEmpty() || parts[1].isEmpty())
            return null;
        return new TypedObjectImpl(parts[0], parts[1]);
    }

    /**
     * @return null - format error
     */
    @Nullable
    public static TypedObjectImpl parse(String description, ConfigurationTypedObject extend) {
        String[] parts = description.split(":", 2);
        if(parts.length < 2 || parts[0].isEmpty() || parts[1].isEmpty())
            return null;
        return new TypedObjectImpl(parts[0], parts[1], extend);
    }

    @Override
    public ConfigurationElement getElement(String name) {
        return elements.get(name);
    }

    @Override
    public <V extends ConfigurationElement> V getElement(String name, Class<V> clazz) {
        return clazz.cast(elements.get(name));
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
