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

package com.alesharik.webserver.generators;

import com.alesharik.webserver.api.errorPageGenerators.ErrorPageConstructor;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@ThreadSafe
class ErrorPageConstructors {
    private final CopyOnWriteArrayList<ErrorPageConstructor> constructors;
    private final ConcurrentHashMap<Integer, ErrorPageConstructor> defaults;

    public ErrorPageConstructors() {
        constructors = new CopyOnWriteArrayList<>();
        defaults = new ConcurrentHashMap<>();
    }

    public void addConstructor(ErrorPageConstructor constructor) {
        constructors.add(constructor);
    }

    public void removeErrorPageConstructor(ErrorPageConstructor constructor) {
        constructors.remove(constructor);
    }

    public boolean containsConstructor(ErrorPageConstructor constructor) {
        return constructors.contains(constructor);
    }

    public void setDefault(ErrorPageConstructor errorPageConstructor, int status) {
        if(!errorPageConstructor.support(status)) {
            throw new IllegalArgumentException("Error page constructor doesn't support given status!");
        }

        defaults.put(status, errorPageConstructor);
    }

    public Optional<ErrorPageConstructor> getConstructor(int status) {
        if(defaults.containsKey(status)) {
            ErrorPageConstructor constructor = defaults.get(status);
            if(constructor != null) {
                return Optional.of(constructor);
            }
        }
        return constructors.stream()
                .filter(constructor -> constructor.support(status))
                .reduce((constructor, constructor2) -> constructor2);
    }

    public List<ErrorPageConstructor> constructors(int status) {
        ArrayList<ErrorPageConstructor> ret = new ArrayList<>();
        constructors.stream()
                .filter(constructor -> constructor.support(status))
                .forEach(ret::add);
        return Collections.unmodifiableList(ret);
    }
}
