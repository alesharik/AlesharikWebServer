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

import com.alesharik.webserver.api.agent.Agent;
import com.alesharik.webserver.api.sharedStorage.annotations.UseSharedStorage;

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
    public static synchronized void registerNewSharedStorage(String name, AccessFilter... filters) {
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
    public static synchronized void unregisterSharedStorage(String name) throws IllegalAccessException {
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
    public static synchronized void addAccessFilter(String name, AccessFilter accessFilter) throws IllegalAccessException {
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
    public static synchronized void setField(String storageName, String fieldName, Object o) throws IllegalAccessException {
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
    public static synchronized Object getField(String storageName, String fieldName) throws IllegalAccessException {
        if(sharedStorageMap.containsKey(storageName)) {
            return sharedStorageMap.get(storageName).getObject(fieldName, true);
        } else {
            throw new IllegalStateException("Shared storage " + storageName + " not exists!");
        }
    }

    /**
     * If something wrong, you can retransform class
     *
     * @param clazz the class to retransform
     */
    public static void reload(Class<?> clazz) throws UnmodifiableClassException {
        if(!clazz.isAnnotationPresent(UseSharedStorage.class)) {
            throw new IllegalArgumentException("The class must annotated of @UseSharedStorage annotation");
        }
        Agent.retransform(clazz);
    }
}
