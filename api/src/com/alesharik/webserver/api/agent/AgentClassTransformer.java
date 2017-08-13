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

import com.alesharik.webserver.api.agent.transformer.Param;
import com.alesharik.webserver.api.agent.transformer.Transform;
import com.alesharik.webserver.api.agent.transformer.TransformAll;
import com.alesharik.webserver.logger.Prefixes;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
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
    private static final MethodHandles.Lookup METHOD_HANDLES_LOOKUP = MethodHandles.lookup();
    /**
     * ClassName: Transformers
     */
    private static final Set<Class<?>> transformerClasses = new CopyOnWriteArraySet<>();
    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<MethodHolder>> transformers = new ConcurrentHashMap<>();
    private static final CopyOnWriteArrayList<MethodHolder> allTransformers = new CopyOnWriteArrayList<>();

    @SuppressFBWarnings("DM_EXIT")
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            if(className == null || loader == null) return null;

            byte[] first = classfileBuffer;
            CopyOnWriteArrayList<MethodHolder> transformers = AgentClassTransformer.transformers.get(className);
            if(transformers != null) {
                for(MethodHolder transformer : transformers) {
                    try {
                        byte[] invoke = transformer.invoke(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
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
                    byte[] invoke = transformer.invoke(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
                    if(invoke == null)
                        continue;
                    classfileBuffer = invoke;
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


    static void addTransformer(Class<?> transformer) {
        if(transformerClasses.contains(transformer)) {
            return;
        } else {
            transformerClasses.add(transformer);
        }
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
