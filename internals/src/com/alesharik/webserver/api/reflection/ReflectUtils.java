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

package com.alesharik.webserver.api.reflection;

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains some useful methods for reflection
 */
@UtilityClass
public class ReflectUtils {
    /**
     * Return all declared methods endpoint(method without overriding) form class and it's superclasses and interfaces
     *
     * @param clazz class
     * @return methods in {@link ArrayList}
     */
    @Nonnull
    public static List<Method> getAllDeclaredMethods(@Nonnull Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        addClassMethods(clazz, methods);
        return methods;
    }

    /**
     * Return all fields form class, all it's superclasses
     * @param clazz class
     * @return fields in {@link ArrayList}
     */
    public static List<Field> getAllDeclaredFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        addClassFields(clazz, fields);
        return fields;
    }

    /**
     * Return all subclasses form class, all it's superclasses
     * @param clazz class
     * @return subclasses in {@link ArrayList} at 1 inner level
     */
    public static List<Class<?>> getAllInnerClasses(Class<?> clazz) {
        List<Class<?>> classes = new ArrayList<>();
        addClassSubclasses(clazz, classes);
        return classes;
    }

    private static void addClassMethods(Class<?> clazz, List<Method> methods) {
        for(Method method : clazz.getDeclaredMethods()) {
            if(Modifier.isAbstract(method.getModifiers()))
                continue;
            boolean ok = true;//Method override existing
            for(Method method1 : methods) {
                if(method1.getName().equals(method.getName()) && Arrays.equals(method1.getParameterTypes(), method.getParameterTypes())) {
                    ok = false;
                    break;
                }
            }
            if(!ok)
                continue;

            method.setAccessible(true);
            methods.add(method);
        }
        for(Class<?> aClass : clazz.getInterfaces()) {
            addInterfaceMethods(aClass, methods);
        }
        Class<?> superclass = clazz.getSuperclass();
        if(superclass != null) {
            addClassMethods(superclass, methods);
        }
    }

    private static void addInterfaceMethods(Class<?> i, List<Method> methods) {
        for(Method method : i.getDeclaredMethods()) {
            if(!method.isDefault())
                continue;

            boolean ok = true;//Method override existing
            for(Method method1 : methods) {
                if(method1.getName().equals(method.getName()) && Arrays.equals(method1.getParameterTypes(), method.getParameterTypes())) {
                    ok = false;
                    break;
                }
            }
            if(!ok)
                continue;

            method.setAccessible(true);
            methods.add(method);
        }
        for(Class<?> aClass : i.getInterfaces()) {
            addInterfaceMethods(aClass, methods);
        }
    }

    private static void addClassFields(Class<?> clazz, List<Field> fields) {
        for(Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            fields.add(field);
        }
        for(Class<?> aClass : clazz.getInterfaces()) {
            addInterfaceFields(aClass, fields);
        }
        Class<?> superclass = clazz.getSuperclass();
        if(superclass != null) {
            addClassFields(superclass, fields);
        }
    }

    private static void addInterfaceFields(Class<?> clazz, List<Field> fields) {
        for(Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            fields.add(field);
        }
        for(Class<?> aClass : clazz.getInterfaces()) {
            addClassFields(aClass, fields);
        }
    }

    private static void addClassSubclasses(Class<?> clazz, List<Class<?>> classes) {
        for(Class<?> aClass : clazz.getDeclaredClasses()) {
            if(!classes.contains(aClass))
                classes.add(aClass);
        }
        for(Class<?> aClass : clazz.getInterfaces()) {
            addClassSubclasses(aClass, classes);
        }
        Class<?> superclass = clazz.getSuperclass();
        if(superclass != null) {
            addClassSubclasses(superclass, classes);
        }
    }
}
