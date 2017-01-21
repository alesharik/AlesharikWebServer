package com.alesharik.webserver.api.agent;

import com.alesharik.webserver.api.sharedStorage.SharedStorageClassTransformer;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public final class Agent {
    private static Instrumentation instrumentation;

    public static void premain(String agentArgs, Instrumentation inst) throws UnmodifiableClassException {
        instrumentation = inst;
//        inst.addTransformer(new AgentClassTransformer(), true);
        inst.addTransformer(new SharedStorageClassTransformer());
    }

    public static void reload(Class<?> clazz) throws UnmodifiableClassException {
        instrumentation.retransformClasses(clazz);
    }

    public static Class[] getAllInitiatedClasses(ClassLoader classLoader) {
        return instrumentation.getInitiatedClasses(classLoader);
    }
}
