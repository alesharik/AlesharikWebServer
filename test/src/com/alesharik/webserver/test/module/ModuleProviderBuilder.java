package com.alesharik.webserver.test.module;

import com.alesharik.webserver.extension.module.meta.ModuleProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleProviderBuilder {
    private final Map<String, Object> modules = new ConcurrentHashMap<>();

    public void addModule(String name, Object o) {
        modules.put(name, o);
    }

    public ModuleProvider build() {
        return new ModuleProviderImpl(modules);
    }

    private static final class ModuleProviderImpl implements ModuleProvider {
        private final Map<String, Object> map;

        public ModuleProviderImpl(Map<String, Object> map) {
            this.map = new HashMap<>(map);
        }

        @Nullable
        @Override
        public Object provideModule(@Nonnull String name, @Nonnull Class<?> clazz) {
            return clazz.cast(map.get(name));
        }
    }
}
