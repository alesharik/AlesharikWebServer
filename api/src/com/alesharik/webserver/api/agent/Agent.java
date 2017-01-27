package com.alesharik.webserver.api.agent;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.List;

public final class Agent {
    static Instrumentation instrumentation;

    public static void premain(String agentArgs, Instrumentation inst) throws UnmodifiableClassException {
        instrumentation = inst;
        inst.addTransformer(new AgentClassTransformer(), true);
    }

    public static void reload(Class<?> clazz) throws UnmodifiableClassException {
        instrumentation.retransformClasses(clazz);
    }

    public static Class[] getAllInitiatedClasses(ClassLoader classLoader) {
        return instrumentation.getInitiatedClasses(classLoader);
    }

    public static List<Class<?>> getCreatedClasses() {
        return AgentClassTransformer.getCreatedClasses();
    }
}
