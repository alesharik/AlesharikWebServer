package com.alesharik.webserver.api.sharedStorage;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public final class ClassTransformerAgent {
    private static Instrumentation instrumentation;

    public static void premain(String agentArgs, Instrumentation inst) throws UnmodifiableClassException {
        instrumentation = inst;
        inst.addTransformer(new SharedStorageClassTransformer(), true);
    }

    static void reload(Class<?> clazz) throws UnmodifiableClassException {
        instrumentation.retransformClasses(clazz);
    }
}
