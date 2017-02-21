package com.alesharik.webserver.api.agent;

import com.alesharik.webserver.logger.Prefix;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Set;

/**
 * Java Agent of AlesharikWebServer
 *
 * @implNote If you create custom server, you MUST call {@link #premain(String, Instrumentation)} method!
 */
@Prefix("[Agent]")
public final class Agent {
    private static Instrumentation instrumentation;

    /**
     * @param agentArgs might be null
     */
    public static void premain(String agentArgs, Instrumentation inst) {
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
}
