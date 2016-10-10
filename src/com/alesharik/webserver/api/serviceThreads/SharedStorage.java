package com.alesharik.webserver.api.serviceThreads;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class used for hold concurrent data
 */
public final class SharedStorage {
    private ConcurrentHashMap<String, Object> objects;

    private SharedStorage() {
        objects = new ConcurrentHashMap<>();
    }

    public static SharedStorage create() {
        return new SharedStorage();
    }

    public void set(String key, Object value) {
        objects.put(key, value);
    }

    public Object get(String key) {
        return objects.get(key);
    }

    public <T> T get(String key, Class<T> clazz) {
        return clazz.cast(get(key));
    }

    public boolean contains(String key) {
        return objects.containsKey(key);
    }

    public void remove(String key) {
        objects.remove(key);
    }
}
