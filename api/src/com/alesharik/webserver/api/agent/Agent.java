package com.alesharik.webserver.api.agent;

import com.alesharik.webserver.logger.Prefixes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Set;

/**
 * Java Agent of AlesharikWebServer
 *
 * @implNote If you create custom server, you MUST use this agent
 */
@Prefixes("[Agent]")
public final class Agent {
    private static Instrumentation instrumentation;

    /**
     * DO NOT CALL IT AFTER MAIN!
     * @implNote you need to call this in premain
     */
    public static void premain(@Nullable String agentArgs, @Nonnull Instrumentation inst) {
        if(instrumentation != null)
            throw new IllegalStateException("WTF ARE YOU DOING?");
        instrumentation = inst;

        inst.addTransformer(new ClassPathScannerTransformer(), false);
        inst.addTransformer(new AgentClassTransformer(), true);
    }

    /**
     * @see Instrumentation#retransformClasses(Class[])
     */
    public static void retransform(Class<?> clazz) throws UnmodifiableClassException {
        instrumentation.retransformClasses(clazz);
    }

    /**
     * @see Instrumentation#redefineClasses(ClassDefinition...)
     */
    public static void redefine(ClassDefinition clazz) throws UnmodifiableClassException, ClassNotFoundException {
        instrumentation.redefineClasses(clazz);
    }

    /**
     * @see Instrumentation#getInitiatedClasses(ClassLoader)
     */
    public static Class[] getAllInitiatedClasses(ClassLoader classLoader) {
        return instrumentation.getInitiatedClasses(classLoader);
    }

    /**
     * Return all existing class loaders
     */
    public static Set<ClassLoader> getAllLoadedClassLoaders() {
        return ClassPathScannerTransformer.getClassLoaders();
    }

    /**
     * Scan custom classloader
     */
    public static void tryScanClassLoader(@Nonnull ClassLoader classLoader) {
        ClassPathScannerTransformer.tryScanClassLoader(classLoader);
    }

    /**
     * Return <code>true</code> if ClassPathScanner scanning some classloaders
     */
    public static boolean isScanning() {
        return !ClassPathScannerTransformer.isFree();
    }
}
