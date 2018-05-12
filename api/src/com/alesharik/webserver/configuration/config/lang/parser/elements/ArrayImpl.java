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
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObjectArray;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EqualsAndHashCode
@ToString
public final class ArrayImpl implements ConfigurationObjectArray {
    @Getter
    @Nonnull
    private final String name;
    private final List<ConfigurationElement> elements = new ArrayList<>();

    public ArrayImpl(@Nonnull String name) {
        if(name == null)
            throw new IllegalArgumentException("name can't be null");
        this.name = name;
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public ConfigurationElement get(int index) {
        return (index >= 0 && elements.size() > index) ? elements.get(index) : null;
    }

    @Override
    public ConfigurationElement[] getElements() {
        return elements.toArray(new ConfigurationElement[0]);
    }

    @Override
    public void append(ConfigurationElement element) {
        elements.add(element);
    }

    @NotNull
    @Override
    public Iterator<ConfigurationElement> iterator() {
        return elements.iterator();
    }
}
