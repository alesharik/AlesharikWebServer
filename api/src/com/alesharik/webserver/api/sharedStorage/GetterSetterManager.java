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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class used for getters and setters. Do not use it!
 */
public final class GetterSetterManager {
    private static final ConcurrentHashMap<String, String> access = new ConcurrentHashMap<>();
    private static final CopyOnWriteArrayList<String> uuid = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<String> s2s = new CopyOnWriteArrayList<>();

    private GetterSetterManager() {
    }

    public static void set(String s1, String s2, Object object) {
        int index = uuid.indexOf(s1);
        if(index == -1) {
            throw new RuntimeException("WTF");
        } else {
            try {
                SharedStorage sharedStorage = SharedStorageManager.sharedStorageMap.get(s2s.get(index));
                sharedStorage.setObject(s2, object, false);
            } catch (IllegalAccessException e) {
                //Okay
            }
        }
    }

    public static Object get(String s1, String s2) {
        int index = uuid.indexOf(s1);
        if(index == -1) {
            throw new RuntimeException("WTF");
        } else {
            try {
                SharedStorage sharedStorage = SharedStorageManager.sharedStorageMap.get(s2s.get(index));
                return sharedStorage.getObject(s2, false);
            } catch (IllegalAccessException e) {
                return null;
            }
        }
    }

    static void register(String s1, String s2) {
        uuid.add(s1);
        s2s.add(s2);
        if(!SharedStorageManager.sharedStorageMap.containsKey(s2)) {
            SharedStorageManager.registerNewSharedStorage(s2);
        }
    }
}
