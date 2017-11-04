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

package com.alesharik.webserver.api.agent.transformer;

import org.objectweb.asm.tree.ClassNode;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

/**
 * If type not match argument class ar argument has no <code>@Param</code> annotation, <code>null</code> will be used as
 * argument.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Param {

    Type value();

    enum Type {
        /**
         * Return class loader. Argument class must
         * be {@link ClassLoader}
         */
        CLASS_LOADER,
        /**
         * Return class name. Argument class must be {@link String}
         */
        CLASS_NAME,
        /**
         * Return class being redefined. May be null. Argument class must be {@link Class}
         */
        CLASS_BEING_REDEFINED,
        /**
         * Return protection domain. Argument class must be {@link java.security.ProtectionDomain}
         */
        PROTECTION_DOMAIN,
        /**
         * Return class bytes. Argument class must be <code>byte[]</code> or <code>{@link Byte}[]</code>
         */
        CLASSFILE_BUFFER,
        /**
         * {@link org.objectweb.asm.tree.ClassNode} of current class
         */
        CLASS_NODE,
        /**
         * Return null
         */
        NULL;

        public static Type[] parse(Method method) {
            Class<?>[] args = method.getParameterTypes();
            Annotation[][] annotations = method.getParameterAnnotations();
            Type[] ret = new Type[args.length];

            for(int i = 0; i < args.length; i++) {
                Annotation[] types = annotations[i];
                Param ann = getAnnotation(types, Param.class);
                if(ann != null) {
                    switch (ann.value()) {
                        case CLASS_BEING_REDEFINED:
                            if(Class.class.isAssignableFrom(args[i]))
                                ret[i] = CLASS_BEING_REDEFINED;
                            else
                                ret[i] = NULL;
                            break;
                        case CLASS_LOADER:
                            if(ClassLoader.class.isAssignableFrom(args[i]))
                                ret[i] = CLASS_LOADER;
                            else
                                ret[i] = NULL;
                            break;
                        case CLASS_NAME:
                            if(String.class.isAssignableFrom(args[i]))
                                ret[i] = CLASS_NAME;
                            else
                                ret[i] = NULL;
                            break;
                        case CLASSFILE_BUFFER:
                            if(Byte[].class.isAssignableFrom(args[i]) || byte[].class.isAssignableFrom(args[i]))
                                ret[i] = CLASSFILE_BUFFER;
                            else
                                ret[i] = NULL;
                            break;
                        case PROTECTION_DOMAIN:
                            if(ProtectionDomain.class.isAssignableFrom(args[i]))
                                ret[i] = PROTECTION_DOMAIN;
                            else
                                ret[i] = NULL;
                            break;
                        case CLASS_NODE:
                            if(ClassNode.class.isAssignableFrom(args[i]))
                                ret[i] = CLASS_NODE;
                            else
                                ret[i] = NULL;
                            break;
                        case NULL:
                        default:
                            ret[i] = NULL;
                            break;
                    }
                } else {
                    ret[i] = NULL;
                }
            }
            return ret;
        }

        @SuppressWarnings("unchecked")
        private static <T> T getAnnotation(Annotation[] annotations, Class<T> annotation) {
            for(Annotation ann : annotations) {
                if(ann.annotationType().equals(annotation)) {
                    return (T) ann;
                }
            }
            return null;
        }
    }
}
