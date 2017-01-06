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
