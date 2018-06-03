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

package com.alesharik.webserver.configuration.module.meta.impl;

import com.alesharik.webserver.api.reflection.ReflectUtils;
import com.alesharik.webserver.base.bean.Bean;
import com.alesharik.webserver.base.bean.context.BeanContext;
import com.alesharik.webserver.base.exception.DevError;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObject;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObjectArray;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationPrimitive;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationTypedObject;
import com.alesharik.webserver.configuration.module.Configuration;
import com.alesharik.webserver.configuration.module.ConfigurationError;
import com.alesharik.webserver.configuration.module.ConfigurationValue;
import com.alesharik.webserver.configuration.module.LinkModule;
import com.alesharik.webserver.configuration.module.meta.ConfigurationLinker;
import com.alesharik.webserver.configuration.module.meta.ModuleProvider;
import com.alesharik.webserver.internals.instance.ClassInstantiator;
import lombok.SneakyThrows;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Bean(singleton = true)
public final class ConfigurationLinkerImpl implements ConfigurationLinker {
    @Override
    @SneakyThrows(IllegalAccessException.class)
    public void link(@Nonnull ConfigurationTypedObject object, @Nonnull Object module, @Nonnull ModuleProvider provider, @Nonnull BeanContext context) {
        if(module.getClass().isAnnotationPresent(Configuration.class))
            handleFields(object, module, provider, context);
        for(Field field : ReflectUtils.getAllDeclaredFields(module.getClass())) {
            if(Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers()))
                continue;

            if(field.getType().isAnnotationPresent(Configuration.class)) {
                Object o = context.createObject(field.getType());
                if(o == null)
                    o = ClassInstantiator.instantiate(field.getType());
                handleFields(object, o, provider, context);
                field.setAccessible(true);
                try {
                    field.set(module, o);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    private void handleFields(ConfigurationObject object, Object config, ModuleProvider provider, BeanContext context) throws IllegalAccessException {
        for(Field field : ReflectUtils.getAllDeclaredFields(config.getClass())) {
            if(Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers()))
                continue;

            field.setAccessible(true);
            if(field.isAnnotationPresent(LinkModule.class))
                handleLinkModuleField(object, config, provider, field);
            if(field.isAnnotationPresent(ConfigurationValue.class)) {
                ConfigurationValue a = field.getAnnotation(ConfigurationValue.class);
                if(ConfigurationElement.class.isAssignableFrom(field.getType())) {
                    ConfigurationElement element = getElement(object, a.value(), config.getClass(), a.optional());
                    if(element == null) {
                        if(a.optional())
                            field.set(config, null);
                        else
                            throw new ConfigurationError("Element " + a.value() + " not found! Class: " + config.getClass().getCanonicalName() + ", field: " + field.getName());
                    }
                    field.set(config, element);
                } else if(isPrimitive(field.getType())) {
                    ConfigurationElement element = getElement(object, a.value(), config.getClass(), a.optional());
                    if(element == null) {
                        if(a.optional())
                            field.set(config, null);
                        else
                            throw new ConfigurationError("Element " + a.value() + " not found! Class: " + config.getClass().getCanonicalName() + ", field: " + field.getName());
                    }
                    field.set(config, handlePrimitive(element, field, config, null));
                } else if(Collection.class.isAssignableFrom(field.getType())) {
                    ConfigurationElement element = getElement(object, a.value(), config.getClass(), a.optional());
                    field.set(config, handleCollection(element, field, config, context, provider, null));
                } else {
                    ConfigurationElement element = getElement(object, a.value(), config.getClass(), a.optional());
                    if(element == null) {
                        if(a.optional())
                            field.set(config, null);
                        else
                            throw new ConfigurationError("Element " + a.value() + " not found! Class: " + config.getClass().getCanonicalName() + ", field: " + field.getName());
                    }
                    field.set(config, handleObject(element, field, config, context, provider, null));
                }
            }
        }
    }

    private boolean isPrimitive(Class<?> clazz) {
        return isBasicPrimitive(clazz)
                || (clazz.isArray() || isBasicPrimitive(clazz.getComponentType()));
    }

    private boolean isBasicPrimitive(Class<?> clazz) {
        return clazz == String.class
                || clazz == int.class || clazz == Integer.class
                || clazz == long.class || clazz == Long.class
                || clazz == char.class || clazz == Character.class
                || clazz == float.class || clazz == Float.class
                || clazz == double.class || clazz == Double.class
                || clazz == boolean.class || clazz == Boolean.class
                || clazz == short.class || clazz == Short.class
                || clazz == byte.class || clazz == Byte.class;
    }

    private Object handlePrimitive(ConfigurationElement element, Field field, Object o, Class<?> typ) {
        Class<?> type = typ == null ? field.getType() : typ;
        if(type == String.class) {
            if(element instanceof ConfigurationPrimitive.String)
                return ((ConfigurationPrimitive.String) element).value();
            else
                throw new ConfigurationError("Element " + element.getName() + " is not a string! Class: " + o.getClass().getCanonicalName() + ", field: " + field.getName());
        } else if(type == int.class || type == Integer.class) {
            if(element instanceof ConfigurationPrimitive.Int)
                return ((ConfigurationPrimitive.Int) element).value();
            else
                throw new ConfigurationError("Element " + element.getName() + " is not an integer! Class: " + o.getClass().getCanonicalName() + ", field: " + field.getName());
        } else if(type == long.class || type == Long.class) {
            if(element instanceof ConfigurationPrimitive.Long)
                return ((ConfigurationPrimitive.Long) element).value();
            else
                throw new ConfigurationError("Element " + element.getName() + " is not a long! Class: " + o.getClass().getCanonicalName() + ", field: " + field.getName());
        } else if(type == char.class || type == Character.class) {
            if(element instanceof ConfigurationPrimitive.Char)
                return ((ConfigurationPrimitive.Char) element).value();
            else
                throw new ConfigurationError("Element " + element.getName() + " is not a char! Class: " + o.getClass().getCanonicalName() + ", field: " + field.getName());
        } else if(type == float.class || type == Float.class) {
            if(element instanceof ConfigurationPrimitive.Float)
                return ((ConfigurationPrimitive.Float) element).value();
            else
                throw new ConfigurationError("Element " + element.getName() + " is not a float! Class: " + o.getClass().getCanonicalName() + ", field: " + field.getName());
        } else if(type == double.class || type == Double.class) {
            if(element instanceof ConfigurationPrimitive.Double)
                return ((ConfigurationPrimitive.Double) element).value();
            else
                throw new ConfigurationError("Element " + element.getName() + " is not a double! Class: " + o.getClass().getCanonicalName() + ", field: " + field.getName());
        } else if(type == boolean.class || type == Boolean.class) {
            if(element instanceof ConfigurationPrimitive.Boolean)
                return ((ConfigurationPrimitive.Boolean) element).value();
            else
                throw new ConfigurationError("Element " + element.getName() + " is not a boolean! Class: " + o.getClass().getCanonicalName() + ", field: " + field.getName());
        } else if(type == short.class || type == Short.class) {
            if(element instanceof ConfigurationPrimitive.Short)
                return ((ConfigurationPrimitive.Short) element).value();
            else
                throw new ConfigurationError("Element " + element.getName() + " is not a short! Class: " + o.getClass().getCanonicalName() + ", field: " + field.getName());
        } else if(type == byte.class || type == Byte.class) {
            if(element instanceof ConfigurationPrimitive.Byte)
                return ((ConfigurationPrimitive.Byte) element).value();
            else
                throw new ConfigurationError("Element " + element.getName() + " is not a byte! Class: " + o.getClass().getCanonicalName() + ", field: " + field.getName());
        } else if(type.isArray()) {
            if(element instanceof ConfigurationObjectArray) {
                ConfigurationObjectArray element1 = (ConfigurationObjectArray) element;
                if(type == String.class)
                    return element1.toStringArray();
                else if(type == int.class)
                    return element1.toIntArray();
                else if(type == long.class)
                    return element1.toLongArray();
                else if(type == char.class)
                    return element1.toCharArray();
                else if(type == float.class)
                    return element1.toFloatArray();
                else if(type == double.class)
                    return element1.toDoubleArray();
                else if(type == boolean.class)
                    return element1.toBooleanArray();
                else if(type == short.class)
                    return element1.toShortArray();
                else if(type == boolean.class)
                    return element1.toByteArray();
                else if(ConfigurationElement.class.isAssignableFrom(type)) {
                    Object[] arr = new Object[element1.size()];
                    for(int i = 0; i < element1.size(); i++) arr[i] = type.cast(element1.get(i));
                    return arr;
                } else
                    throw new DevError("Config type for array " + type.getCanonicalName() + " is not supported!", "Config type for array " + type.getCanonicalName() + " is not supported!", o.getClass());
            } else
                throw new ConfigurationError("Element " + element.getName() + " is not an array! Class: " + o.getClass().getCanonicalName() + ", field: " + field.getName());
        } else
            throw new DevError("Primitive config type for array " + type.getCanonicalName() + " is not supported!", "Config type for array " + type.getCanonicalName() + " is not supported!", o.getClass());
    }

    private Object handleCollection(ConfigurationElement element, Field field, Object o, BeanContext context, ModuleProvider provider, Class<?> typ) {
        if(!(element instanceof ConfigurationObjectArray))
            throw new ConfigurationError("Element " + element.getName() + " is not an array! Class: " + o.getClass().getCanonicalName() + ", field: " + field.getName());
        ConfigurationObjectArray elemArr = (ConfigurationObjectArray) element;
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        Class<?> arrType = (Class<?>) type.getActualTypeArguments()[0];
        Class<?> arrr = typ == null ? field.getType() : typ;
        Object[] arr = new Object[elemArr.size()];
        for(int i = 0; i < elemArr.size(); i++) {
            if(ConfigurationElement.class.isAssignableFrom(arrType))
                arr[i] = elemArr.get(i);
            else if(isPrimitive(arrType))
                arr[i] = handlePrimitive(elemArr.get(i), field, o, arrType);
            else if(Collection.class.isAssignableFrom(arrType))
                arr[i] = handleCollection(elemArr.get(i), field, o, context, provider, Object.class);
            else
                arr[i] = handleObject(elemArr.get(i), field, o, context, provider, arrType);
        }
        if(Set.class.isAssignableFrom(arrr))
            return new HashSet<>(Arrays.asList(arr));
        else if(List.class.isAssignableFrom(arrr))
            return new ArrayList<>(Arrays.asList(arr));
        else
            throw new DevError("Collection " + arrr.getCanonicalName() + " is not supported!", "Collection " + arrr.getCanonicalName() + " is not supported!", o.getClass());
    }

    @SneakyThrows(IllegalAccessException.class)
    private Object handleObject(ConfigurationElement element, Field field, Object o, BeanContext context, ModuleProvider provider, Class<?> typ) {
        if(!(element instanceof ConfigurationObject))
            throw new ConfigurationError("Element " + element.getName() + " is not an object! Class: " + o.getClass().getCanonicalName() + ", field: " + field.getName());
        Class<?> type = typ == null ? field.getType() : typ;
        Object obj = context.createObject(type);
        if(obj == null)
            obj = ClassInstantiator.instantiate(type);

        handleFields((ConfigurationObject) element, obj, provider, context);

        return obj;
    }

    private void handleLinkModuleField(ConfigurationObject object, Object config, ModuleProvider provider, Field field) throws IllegalAccessException {
        LinkModule a = field.getAnnotation(LinkModule.class);
        String name = a.value();
        ConfigurationElement element = getElement(object, name, config.getClass(), a.optional());
        if(element == null) {
            if(!a.optional())
                throw new ConfigurationError("Element " + name + " not found! Class: " + config.getClass().getCanonicalName() + ", field: " + field.getName());
            else
                field.set(config, null);
        } else {
            if(element instanceof ConfigurationPrimitive.String) {
                String n = ((ConfigurationPrimitive.String) element).value();
                Object module = provider.provideModule(n, field.getType());
                if(module == null) {
                    if(!a.optional())
                        throw new ConfigurationError("Module with name " + n + " not found! Class: " + config.getClass().getCanonicalName() + ", field: " + field.getName());
                    else
                        field.set(config, null);
                } else field.set(config, module);
            } else
                throw new ConfigurationError("Element " + name + " is not a string! Class: " + config.getClass().getCanonicalName() + ", field: " + field.getName());
        }
    }

    private ConfigurationElement getElement(ConfigurationObject object, String name, Class<?> clazz, boolean optional) {
        if(name.isEmpty())
            throw new DevError("@LinkModule value error: value is empty string!", "@LinkModule's value MUST NOT be empty string!", clazz);
        ConfigurationElement current = object;
        while(!name.isEmpty()) {
            int objectSplitIndex = name.indexOf("::");
            int arrStartIndex = name.indexOf("[[");
            if(objectSplitIndex == -1)
                objectSplitIndex = Integer.MAX_VALUE;
            if(arrStartIndex == -1)
                arrStartIndex = Integer.MAX_VALUE;
            if(objectSplitIndex < arrStartIndex) {
                //Move to object
                String objName = name.substring(0, objectSplitIndex);
                name = name.substring(objectSplitIndex + 2);//2 - ::
                if(current instanceof ConfigurationObject) {
                    current = ((ConfigurationObject) current).getElement(objName);
                    if(name.isEmpty())
                        throw new DevError("@LinkModule value error: key name after :: cannot be empty string", "You MUST write name(object key) after ::", clazz);
                } else {
                    if(optional)
                        return null;
                    throw new ConfigurationError("Config name pointer parse error: object expected! Name: " + name);
                }
            } else if(objectSplitIndex > arrStartIndex) {
                int arrEndIndex = name.indexOf("]]");
                if(arrEndIndex == -1)
                    throw new DevError("@LinkModule value error: index selector(]]) close expected", "If you open index selector by [[, you must close it by ]]", clazz);
                if(arrEndIndex < arrStartIndex)
                    throw new DevError("@LinkModule value error: index closing before it opens!", "You MUST open any closed index selector", clazz);
                String select = name.substring(arrStartIndex + 2, arrEndIndex);
                name = name.substring(arrEndIndex + 2);
                try {
                    int sel = Integer.parseInt(select);
                    if(sel < 0)
                        throw new DevError("@LinkModule value error: integer expected in [[ and ]]", "You MUST write non-negative integer value in index selector( [[integer]] )", clazz);
                    if(current instanceof ConfigurationObjectArray) {
                        if(sel >= ((ConfigurationObjectArray) current).size())
                            throw new ConfigurationError("Config name pointer parse error: array too small! Array size: " + ((ConfigurationObjectArray) current).size() + ", requested index: " + sel + ", name: " + name);
                        current = ((ConfigurationObjectArray) current).get(sel);
                    } else {
                        if(optional)
                            return null;
                        throw new ConfigurationError("Config name pointer parse error: array expected! Name: " + name);
                    }
                } catch (NumberFormatException e) {
                    throw new DevError("@LinkModule value error: integer expected in [[ and ]]", "You MUST write non-negative integer value in index selector( [[integer]] )", clazz);
                }
                if(name.isEmpty())
                    return current;
            } else {
                //None matches
                if(current instanceof ConfigurationObject) {
                    return ((ConfigurationObject) current).getElement(name);
                } else {
                    if(optional)
                        return null;
                    throw new ConfigurationError("Config name pointer parse error: object expected! Name: " + name);
                }
            }
        }
        return null;
    }
}
