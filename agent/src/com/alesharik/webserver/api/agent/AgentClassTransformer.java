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

import com.alesharik.webserver.api.ExecutionStage;
import com.alesharik.webserver.api.agent.transformer.Param;
import com.alesharik.webserver.api.agent.transformer.Transform;
import com.alesharik.webserver.api.agent.transformer.TransformAll;
import com.alesharik.webserver.logger.Prefixes;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

/**
 * Transform classes with {@link com.alesharik.webserver.api.agent.transformer.ClassTransformer} annotated classes
 */
@Prefixes({"[Agent]", "[AgentClassTransformer]"})
final class AgentClassTransformer implements ClassFileTransformer {
    /**
     * ClassName: Transformers
     */
    private static final Set<Class<?>> transformerClasses = new CopyOnWriteArraySet<>();
    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<MethodHolder>> transformers = new ConcurrentHashMap<>();
    private static final CopyOnWriteArrayList<MethodHolder> allTransformers = new CopyOnWriteArrayList<>();

    static void addTransformer(Class<?> transformer, boolean replace) {
        if(transformerClasses.contains(transformer)) {
            if(replace) {
                transformers.forEach((s, methodHolders) -> methodHolders.removeIf(next -> next.methodHandle.getDeclaringClass() == transformer));
                allTransformers.removeIf(next -> next.methodHandle.getDeclaringClass() == transformer);
            } else
                return;
        } else
            transformerClasses.add(transformer);
        Stream.of(transformer.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Transform.class) || method.isAnnotationPresent(TransformAll.class))
                .filter(method -> method.getReturnType().isAssignableFrom(byte[].class))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> {
                    try {
                        method.setAccessible(true);
                        Param.Type[] parse = Param.Type.parse(method);

                        if(method.isAnnotationPresent(Transform.class)) {
                            String value = method.getAnnotation(Transform.class).value();
                            transformers.computeIfAbsent(value, k -> new CopyOnWriteArrayList<>());
                            transformers.get(value).add(new MethodHolder(method, parse));
                        } else {
                            allTransformers.add(new MethodHolder(method, parse));
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            if(className == null || loader == null) return null;

            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(classfileBuffer);
            classReader.accept(classNode, 0);

            byte[] first = classfileBuffer;
            CopyOnWriteArrayList<MethodHolder> transformers = AgentClassTransformer.transformers.get(className);
            if(transformers != null) {
                for(MethodHolder transformer : transformers) {
                    try {
                        byte[] invoke = transformer.invoke(loader, className, classBeingRedefined, protectionDomain, classfileBuffer, classNode);
                        if(invoke == null)
                            continue;
                        classfileBuffer = invoke;
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            }

            for(MethodHolder transformer : allTransformers) {
                try {
                    byte[] invoke = transformer.invoke(loader, className, classBeingRedefined, protectionDomain, classfileBuffer, classNode);
                    if(invoke == null)
                        continue;
                    classfileBuffer = invoke;
                } catch (InvocationTargetException e) {
                    if(e.getCause() instanceof NoClassDefFoundError)
                        System.err.println("Transformer " + transformer.methodHandle.getDeclaringClass().getCanonicalName() + " not found!");
                    else {
                        System.err.println("Exception in transformer " + transformer.methodHandle.getDeclaringClass().getCanonicalName() + '!');
                        e.getCause().printStackTrace();
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
            return Arrays.equals(first, classfileBuffer) ? null : classfileBuffer;
        } catch (Error e) {
            System.err.println("Error in transforming class " + className + ", classloader = " + loader);
            e.printStackTrace();

            System.err.println("Error detected! Stopping application...");
            System.exit(1);
            return null;
        } catch (Throwable e) {
            System.err.println("Error in transforming class " + className + ", classloader = " + loader);
            e.printStackTrace();
            return null;
        }
    }

    static void removeClassLoader(ClassLoader classLoader) {
        for(CopyOnWriteArrayList<MethodHolder> methodHolders : transformers.values())
            methodHolders.removeIf(next -> next.getClassLoader() == classLoader);
        allTransformers.removeIf(next -> next.getClassLoader() == classLoader);
    }

    private static final class MethodHolder {
        private final Method methodHandle;
        private final Param.Type[] args;

        public MethodHolder(Method methodHandle, Param.Type[] args) {
            this.methodHandle = methodHandle;
            this.args = args;
        }

        public byte[] invoke(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer, ClassNode classNode) throws Throwable {
            Stages stages = methodHandle.getAnnotation(Stages.class);
            if(stages != null && ExecutionStage.isEnabled() && !ExecutionStage.valid(stages.value()))
                return null;

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
                    case CLASS_NODE:
                        invokeArgs.add(classNode);
                        break;
                    case NULL:
                    default:
                        invokeArgs.add(null);
                }
            }
            return (byte[]) methodHandle.invoke(null, invokeArgs.toArray());
        }

        public ClassLoader getClassLoader() {
            return methodHandle.getDeclaringClass().getClassLoader();
        }
    }
}
