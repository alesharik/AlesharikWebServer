package com.alesharik.webserver.api.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

        return classfileBuffer;
    }

    /**
     * Return all class loaders
     */
    public static Set<ClassLoader> getClassLoaders() {
        return Collections.unmodifiableSet(classLoaders);
    }

    public static void tryScanClassLoader(ClassLoader classLoader) {
        if(classLoader.getClass().getCanonicalName().equals("sun.reflect.DelegatingClassLoader")) {
            return;
        }
        if(!classLoaders.contains(classLoader)) {
            classLoaders.add(classLoader);
            thread.addClassLoader(classLoader);
        }
    }

    public static boolean isFree() {
        return thread.isFree();
    }
}
