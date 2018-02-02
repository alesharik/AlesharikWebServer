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

package com.alesharik.webserver.api.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.alesharik.webserver.api.agent.ClassLoaderHelper.*;

/**
 * This class hold {@link ClassPathScannerThread} and send new class loaders to it.
 * This class also hold all ClassLoaders.
 * It do not perform any bytecode transformations
 */
final class ClassPathScannerTransformer implements ClassFileTransformer {
    private static final ClassPathScannerThread thread;
    private static final Set<ClassLoader> classLoaders;

    static {
        thread = new ClassPathScannerThread();
        thread.start();

        classLoaders = new HashSet<>();
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        tryScanClassLoader(loader);
        return null;
    }

    /**
     * Return all class loaders
     */
    public static Set<ClassLoader> getClassLoaders() {
        return Collections.unmodifiableSet(classLoaders);
    }

    public static void tryScanClassLoader(ClassLoader classLoader) {
        if(classLoader.getClass().getCanonicalName().equals("sun.reflect.DelegatingClassLoader") || isIgnored(classLoader) || isClosed(classLoader)) {
            return;
        }
        if(isRescanable(classLoader)) {
            thread.addClassLoader(classLoader);
            if(!classLoaders.contains(classLoader))
                classLoaders.add(classLoader);
        } else if(!classLoaders.contains(classLoader)) {
            classLoaders.add(classLoader);
            thread.addClassLoader(classLoader);
        }
    }

    public static void reloadClassLoader(ClassLoader classLoader) {
        if(!classLoaders.contains(classLoader))
            tryScanClassLoader(classLoader);
        else
            thread.rescanClassLoader(classLoader);
    }

    public static boolean isFree() {
        return thread.isFree();
    }

    static void shutdown() {
        thread.interrupt();
    }
}
