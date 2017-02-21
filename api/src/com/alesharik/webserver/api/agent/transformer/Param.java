package com.alesharik.webserver.api.agent.transformer;

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
                            if(args[i].isAssignableFrom(Class.class)) {
                                ret[i] = CLASS_BEING_REDEFINED;
                            } else {
                                ret[i] = NULL;
                            }
                            break;
                        case CLASS_LOADER:
                            if(args[i].isAssignableFrom(ClassLoader.class)) {
                                ret[i] = CLASS_LOADER;
                            } else {
                                ret[i] = NULL;
                            }
                            break;
                        case CLASS_NAME:
                            if(args[i].isAssignableFrom(String.class)) {
                                ret[i] = CLASS_NAME;
                            } else {
                                ret[i] = NULL;
                            }
                            break;
                        case CLASSFILE_BUFFER:
                            if(args[i].isAssignableFrom(Byte[].class) || args[i].isAssignableFrom(byte[].class)) {
                                ret[i] = CLASSFILE_BUFFER;
                            } else {
                                ret[i] = NULL;
                            }
                            break;
                        case PROTECTION_DOMAIN:
                            if(args[i].isAssignableFrom(ProtectionDomain.class)) {
                                ret[i] = PROTECTION_DOMAIN;
                            } else {
                                ret[i] = NULL;
                            }
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
