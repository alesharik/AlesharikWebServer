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

package com.alesharik.webserver.configuration;

import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenInterface;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class holds all modules
 */
@ClassPathScanner
public final class ModuleManager {
    private static final ConcurrentHashMap<String, Module> modules = new ConcurrentHashMap<>();

    private ModuleManager() {
    }

    public static void addModule(Module module) {
        modules.put(module.getName(), module);
    }

    @Nullable
    public static Module getModuleByName(@Nonnull String name) {
        return modules.get(name);
    }

    public static Collection<Module> getModules() {
        return modules.values();
    }

    @ListenInterface(Module.class)
    static void listenModule(Class<?> moduleClazz) {
        try {
            if(Modifier.isAbstract(moduleClazz.getModifiers())) {
                return;
            }
            if(moduleClazz.isAnnotationPresent(Ignored.class))
                return;
            Constructor<?> constructor = moduleClazz.getConstructor();
            Module instance = (Module) constructor.newInstance();
            modules.put(instance.getName(), instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Ignored {

    }
}
