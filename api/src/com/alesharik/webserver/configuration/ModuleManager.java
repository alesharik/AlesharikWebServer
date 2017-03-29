package com.alesharik.webserver.configuration;

import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenInterface;
import one.nio.util.JavaInternals;
import sun.misc.Unsafe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class holds all modules
 */
@ClassPathScanner
public final class ModuleManager {
    private static final Unsafe UNSAFE = JavaInternals.getUnsafe();

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
    public static void listenModule(Class<?> moduleClazz) {
        try {
            Module instance = (Module) UNSAFE.allocateInstance(moduleClazz);
            modules.put(instance.getName(), instance);
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
}
