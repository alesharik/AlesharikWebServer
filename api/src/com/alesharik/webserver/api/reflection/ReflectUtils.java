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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public class ReflectUtils {
    public static List<Method> getAllDeclaredMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        addClass(clazz, methods, null);
        return methods;
    }

    public static List<Field> getAllDeclaredFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        addClass(clazz, fields, null);
        return fields;
    }

    public static List<Class<?>> getAllInnerClasses(Class<?> clazz) {
        List<Class<?>> classes = new ArrayList<>();
        addClass(clazz, classes, null);
        return classes;
    }

    private static void addClass(Class<?> clazz, List<Method> methods, Method nil) {
        for(Method method : clazz.getDeclaredMethods()) {
            if(Modifier.isAbstract(method.getModifiers()))
                continue;
            method.setAccessible(true);
            methods.add(method);
        }
        for(Class<?> aClass : clazz.getInterfaces()) {
            addInterface(aClass, methods);
        }
        Class<?> superclass = clazz.getSuperclass();
        if(superclass != null) {
            addClass(superclass, methods, null);
        }
    }

    private static void addInterface(Class<?> i, List<Method> methods) {
        for(Method method : i.getDeclaredMethods()) {
            if(!method.isDefault())
                continue;
            method.setAccessible(true);
            methods.add(method);
        }
        for(Class<?> aClass : i.getInterfaces()) {
            addInterface(aClass, methods);
        }
    }

    private static void addClass(Class<?> clazz, List<Field> fields, Field nil) {
        for(Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            fields.add(field);
        }
        Class<?> superclass = clazz.getSuperclass();
        if(superclass != null) {
            addClass(superclass, fields, null);
        }
    }

    private static void addClass(Class<?> clazz, List<Class<?>> classes, Class<?> nil) {
        classes.addAll(Arrays.asList(clazz.getDeclaredClasses()));
        Class<?> superclass = clazz.getSuperclass();
        if(superclass != null) {
            addClass(superclass, classes, null);
        }
    }
}
