package com.alesharik.webserver.api.agent;

import com.alesharik.webserver.api.agent.transformer.Param;
import com.alesharik.webserver.api.agent.transformer.Transform;
import com.alesharik.webserver.api.agent.transformer.TransformAll;
import sun.misc.VM;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * Transform classes with {@link com.alesharik.webserver.api.agent.transformer.ClassTransformer} annotated classes
 */
final class AgentClassTransformer implements ClassFileTransformer {
    private static final MethodHandles.Lookup METHOD_HANDLES_LOOKUP = MethodHandles.lookup();
    /**
     * ClassName: Transformers
     */
    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<MethodHolder>> transformers = new ConcurrentHashMap<>();
    private static final CopyOnWriteArrayList<MethodHolder> allTransformers = new CopyOnWriteArrayList<>();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if(VM.isSystemDomainLoader(loader) || className.toLowerCase().contains("nashorn")) {
            return classfileBuffer;
        }
        CopyOnWriteArrayList<MethodHolder> transformers = AgentClassTransformer.transformers.get(className);
        if(transformers != null) {
            for(MethodHolder transformer : transformers) {
                try {
                    classfileBuffer = transformer.invoke(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
        for(MethodHolder transformer : allTransformers) {
            try {
                classfileBuffer = transformer.invoke(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return classfileBuffer;
    }


    static void addTransformer(Class<?> transformer) {
        Stream.of(transformer.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Transform.class) || method.isAnnotationPresent(TransformAll.class))
                .filter(method -> method.getReturnType().isAssignableFrom(byte[].class))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> {
                    try {
                        MethodHandle methodHandle = METHOD_HANDLES_LOOKUP.unreflect(method);
                        Param.Type[] parse = Param.Type.parse(method);

                        if(method.isAnnotationPresent(Transform.class)) {
                            String value = method.getAnnotation(Transform.class).value();
                            transformers.computeIfAbsent(value, k -> new CopyOnWriteArrayList<>());
                            transformers.get(value).add(new MethodHolder(methodHandle, parse));
                        } else {
                            allTransformers.add(new MethodHolder(methodHandle, parse));
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private static final class MethodHolder {
        private final MethodHandle methodHandle;
        private final Param.Type[] args;

        public MethodHolder(MethodHandle methodHandle, Param.Type[] args) {
            this.methodHandle = methodHandle;
            this.args = args;
        }

        public byte[] invoke(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws Throwable {
            ArrayList<Object> invokeArgs = new ArrayList<>(args.length);
            for(Param.Type type : args) {
                switch (type) {
                    case CLASS_BEING_REDEFINED:
                        invokeArgs.add(classBeingRedefined);
                        break;
                    case CLASS_LOADER:
                        invokeArgs.add(loader);
                        break;
                    case PROTECTION_DOMAIN:
                        invokeArgs.add(protectionDomain);
                        break;
                    case CLASSFILE_BUFFER:
                        invokeArgs.add(classfileBuffer);
                        break;
                    case CLASS_NAME:
                        invokeArgs.add(className);
                        break;
                    case NULL:
                    default:
                        invokeArgs.add(null);
                }
            }
            return (byte[]) methodHandle.invokeWithArguments(invokeArgs);
        }
    }
}
