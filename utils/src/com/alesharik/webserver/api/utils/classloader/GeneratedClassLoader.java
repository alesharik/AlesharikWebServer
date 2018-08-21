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

package com.alesharik.webserver.api.utils.classloader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This classloader allows to define classes at runtime. It won't register itself as MXBean
 */
public class GeneratedClassLoader extends InstantiableClassLoader implements GeneratedClassLoaderMXBean {
    protected final Map<String, byte[]> classes = new ConcurrentHashMap<>();

    public GeneratedClassLoader(ClassLoader parent) {
        super(parent);
    }

    public GeneratedClassLoader() {
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = classes.get(name);
        if(bytes != null)
            return super.defineClass(name, bytes, 0, bytes.length);
        return super.findClass(name);
    }

    /**
     * Add new generated class to classloader
     *
     * @param name new class binary name
     * @param data generated class bytes
     * @throws ClassAlreadyExistsException if class already defined
     */
    public void addGeneratedClass(String name, byte[] data) {
        if(classes.containsKey(name))
            throw new ClassAlreadyExistsException(name);
        classes.put(name, data);
    }

    @Override
    public int getGeneratedClassCount() {
        return classes.size();
    }
}
