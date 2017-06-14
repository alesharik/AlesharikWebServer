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

package com.alesharik.webserver.api.sharedStorage;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class is a thread-safe shared storage
 */
final class SharedStorage {
    //Use CopyOnWriteArrayList because this class need non-blocking synchronization
    private final CopyOnWriteArrayList<AccessFilter> filters;
    private final CopyOnWriteArrayList<String> names;
    private final CopyOnWriteArrayList<Object> objects;

    public SharedStorage(AccessFilter[] filters) {
        this.filters = new CopyOnWriteArrayList<>(filters);
        names = new CopyOnWriteArrayList<>();
        objects = new CopyOnWriteArrayList<>();
    }

    private void checkAccess(AccessFilter.Type type, String field) throws IllegalAccessException {
        Class<?> clazz = CallingClass.INSTANCE.getCallingClasses()[4];
        for(AccessFilter filter : filters) {
            if(!filter.canAccess(clazz, type, field)) {
                throw new IllegalAccessException("Access denied!");
            }
        }
    }

    public void addFilter(AccessFilter accessFilter) throws IllegalAccessException {
        checkAccess(AccessFilter.Type.ADD_FILTER, null);
        filters.add(accessFilter);
    }

    public void setObject(String name, Object o, boolean external) throws IllegalAccessException {
        if(external) {
            checkAccess(AccessFilter.Type.SET_EXTERNAL, name);
        } else {
            checkAccess(AccessFilter.Type.SET, name);
        }

        if(o == null) {
            return;
        }

        int index = names.indexOf(name);
        if(index == -1) {
            names.add(name);
            objects.add(o);
        } else {
            objects.set(index, o);
        }
    }

    public Object getObject(String name, boolean external) throws IllegalAccessException {
        if(external) {
            checkAccess(AccessFilter.Type.GET_EXTERNAL, name);
        } else {
            checkAccess(AccessFilter.Type.GET, name);
        }

        int index = names.indexOf(name);
        if(index == -1) {
            return null;
        } else {
            return objects.get(index);
        }
    }

    void clear() throws IllegalAccessException {
        checkAccess(AccessFilter.Type.CLEAR, null);
        filters.clear();
        names.clear();
        objects.clear();
    }

    private static class CallingClass extends SecurityManager {
        public static final CallingClass INSTANCE = new CallingClass();

        private CallingClass() {
        }

        public Class[] getCallingClasses() {
            return getClassContext();
        }
    }
}
