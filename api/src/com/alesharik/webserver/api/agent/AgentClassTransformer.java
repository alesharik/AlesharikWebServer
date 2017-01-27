package com.alesharik.webserver.api.agent;

import com.alesharik.webserver.logger.Logger;
import one.nio.util.JavaInternals;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import sun.misc.Unsafe;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

final class AgentClassTransformer implements ClassFileTransformer {
    private static final Unsafe UNSAFE = JavaInternals.unsafe;
    private static final MethodHandles.Lookup METHOD_HANDLES_LOOKUP = MethodHandles.lookup();

    private static final CopyOnWriteArrayList<Class<?>> anonymousClasses = new CopyOnWriteArrayList<>();

    /**
     * ClassName: Transformers
     */
    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<MethodHolder>> transformers = new ConcurrentHashMap<>();

    private static final CopyOnWriteArrayList<MethodHolder> allTransformers = new CopyOnWriteArrayList<>();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] data = executeASM(classfileBuffer);
        CopyOnWriteArrayList<MethodHolder> transformers = AgentClassTransformer.transformers.get(className);
        if(transformers != null) {
            for(MethodHolder transformer : transformers) {
                try {
                    data = transformer.invoke(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
                } catch (Throwable throwable) {
                    Logger.log(throwable);
                }
            }
        }
        for(MethodHolder allTransformer : allTransformers) {
            try {
                data = allTransformer.invoke(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
            } catch (Throwable throwable) {
                Logger.log(throwable);
            }
        }
        return data;
    }

    private static byte[] executeASM(byte[] classfileBuffer) {
        AtomicBoolean isTransformer = new AtomicBoolean(false);
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
        classReader.accept(new AgentClassVisitor(classWriter, isTransformer), 0);
        byte[] bytes = classWriter.toByteArray();
        if(isTransformer.get()) {
            Class<?> clazz = UNSAFE.defineAnonymousClass(AgentClassTransformer.class, bytes, null);
            addTransformer(clazz);
            anonymousClasses.add(clazz);
        }
        return bytes;
    }

    private static void addTransformer(Class<?> transformer) {
        if(!transformer.isAnnotationPresent(ClassTransformer.class)) {
            return;
        }
        Stream.of(transformer.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Transform.class) || method.isAnnotationPresent(TransformAll.class))
                .filter(method -> method.getReturnType().isAssignableFrom(byte[].class))
                .filter(method -> (method.getModifiers() & Modifier.STATIC) == Modifier.STATIC)
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
                        Logger.log(e);
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

    static List<Class<?>> getCreatedClasses() {
        return Collections.unmodifiableList(anonymousClasses);
    }
}
