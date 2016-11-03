package com.alesharik.webserver.api.sharedStorage;

import com.alesharik.webserver.api.sharedStorage.annotations.UseSharedStorage;
import com.sun.istack.internal.NotNull;

import java.lang.instrument.UnmodifiableClassException;
import java.util.HashMap;

/**
 * This class used for works with shared storage. Here you can register and unregister storage, set storage filters and
 * works with other storage.<br>
 * To start using shared storage you need to annotate class annotation @{@link com.alesharik.webserver.api.sharedStorage.annotations.UseSharedStorage}
 * with storage name. If storage not exists, it creates automatically! You don't need to register your class or etc! You need
 * simply call class in your code and asm do other things! To interact with shared storage you need to define getters nad setters.
 * The getter defined by {@link com.alesharik.webserver.api.sharedStorage.annotations.SharedValueGetter} and the setter
 * defined by {@link com.alesharik.webserver.api.sharedStorage.annotations.SharedValueSetter}.
 */
//TODO write tests for shared storage
public final class SharedStorageManager {
    static final HashMap<String, SharedStorage> sharedStorageMap = new HashMap<>();

    private SharedStorageManager() {
    }

    /**
     * Create new shared storage
     *
     * @param name    name of shared storage
     * @param filters filters
     * @throws IllegalStateException then shared storage already exists
     */
    public static synchronized void registerNewSharedStorage(@NotNull String name, @NotNull AccessFilter... filters) {
        if(sharedStorageMap.containsKey(name)) {
            throw new IllegalStateException("The shared storage with name " + name + " already exists!");
        } else {
            SharedStorage sharedStorage = new SharedStorage(filters);
            sharedStorageMap.put(name, sharedStorage);
        }
    }

    /**
     * Unregister and clear shared storage
     *
     * @param name name of storage
     * @throws IllegalAccessException if filters block this
     * @throws IllegalStateException  if shared storage not exists
     */
    public static synchronized void unregisterSharedStorage(@NotNull String name) throws IllegalAccessException {
        if(sharedStorageMap.containsKey(name)) {
            sharedStorageMap.get(name).clear();
            sharedStorageMap.remove(name);
        } else {
            throw new IllegalStateException("Shared storage " + name + " not exists!");
        }
    }

    /**
     * Add new {@link AccessFilter} to storage
     *
     * @param name         name of storage
     * @param accessFilter filter to add
     * @throws IllegalAccessException if filters block this
     * @throws IllegalStateException  if shared storage not exists
     */
    public static synchronized void addAccessFilter(@NotNull String name, @NotNull AccessFilter accessFilter) throws IllegalAccessException {
        if(sharedStorageMap.containsKey(name)) {
            sharedStorageMap.get(name).addFilter(accessFilter);
        } else {
            throw new IllegalStateException("Shared storage " + name + " not exists!");
        }
    }

    /**
     * Set field in specific storage
     *
     * @param storageName name of storage
     * @param fieldName   name of shared field
     * @param o           object to set
     * @throws IllegalAccessException if filters block this
     * @throws IllegalStateException  if shared storage not exists
     */
    public static synchronized void setField(@NotNull String storageName, @NotNull String fieldName, @NotNull Object o) throws IllegalAccessException {
        if(sharedStorageMap.containsKey(storageName)) {
            sharedStorageMap.get(storageName).setObject(fieldName, o, true);
        } else {
            throw new IllegalStateException("Shared storage " + storageName + " not exists!");
        }
    }

    /**
     * Get field in specific storage
     *
     * @param storageName name of storage
     * @param fieldName   name of shared field
     * @return the field value or null if field empty
     * @throws IllegalAccessException if filters block this
     * @throws IllegalStateException  if shared storage not exists
     */
    public static synchronized Object getField(@NotNull String storageName, @NotNull String fieldName) throws IllegalAccessException {
        if(sharedStorageMap.containsKey(storageName)) {
            return sharedStorageMap.get(storageName).getObject(fieldName, true);
        } else {
            throw new IllegalStateException("Shared storage " + storageName + " not exists!");
        }
    }

    /**
     * If something wrong, you can reload class
     *
     * @param clazz the class to reload
     */
    public static void reload(Class<?> clazz) throws UnmodifiableClassException {
        if(!clazz.isAnnotationPresent(UseSharedStorage.class)) {
            throw new IllegalArgumentException("The class must annotated of @UseSharedStorage annotation");
        }
        ClassTransformerAgent.reload(clazz);
    }
}
