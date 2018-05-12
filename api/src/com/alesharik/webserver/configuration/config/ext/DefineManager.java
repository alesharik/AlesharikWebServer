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

package com.alesharik.webserver.configuration.config.ext;

import com.alesharik.webserver.api.ExecutionStage;
import com.alesharik.webserver.api.agent.Stages;
import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenInterface;
import com.alesharik.webserver.api.agent.classPath.reload.UnloadClassLoaderHandler;
import com.alesharik.webserver.internals.instance.ClassInstantiationException;
import com.alesharik.webserver.internals.instance.ClassInstantiator;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.level.Level;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Level("define-manager")
@UtilityClass
@ClassPathScanner
@ThreadSafe
public class DefineManager {
    static final Map<String, DefineProvider> providers = new ConcurrentHashMap<>();

    static {
        Logger.getLoggingLevelManager().createLoggingLevel("define-manager");
    }

    @ListenInterface(DefineProvider.class)
    @Stages({ExecutionStage.AGENT, ExecutionStage.PRE_LOAD, ExecutionStage.CORE_MODULES})
    static void listen(Class<?> clazz) {
        try {
            System.out.println("Processing DefineProvider class " + clazz.getCanonicalName());
            DefineProvider instance = (DefineProvider) ClassInstantiator.instantiate(clazz);
            if(providers.containsKey(instance.getName())) {
                System.err.println("DefineProvider " + instance.getName() + " already exists! Ignoring...");
                return;
            }
            providers.put(instance.getName(), instance);
        } catch (ClassInstantiationException e) {
            System.err.println("Can't instantiate class " + clazz.getCanonicalName());
            e.printStackTrace();
        }
    }

    @UnloadClassLoaderHandler
    static void clearClassLoader(ClassLoader classLoader) {
        for(Map.Entry<String, DefineProvider> stringDefineProviderEntry : providers.entrySet()) {
            if(stringDefineProviderEntry.getValue().getClass().getClassLoader() == classLoader)
                providers.remove(stringDefineProviderEntry.getKey(), stringDefineProviderEntry.getValue());
        }
    }

    @Nullable
    public static String getDefinition(@Nonnull String name, @Nonnull DefineEnvironment environment) {
        DefineProvider provider = providers.get(name);
        return provider == null ? null : provider.getDefinition(environment);
    }

    public static boolean isDefined(@Nonnull String name, @Nonnull DefineEnvironment environment) {
        return providers.containsKey(name) && providers.get(name).getDefinition(environment) != null;
    }

    public static Map<String, String> getAllDefines(@Nonnull DefineEnvironment environment) {
        Map<String, String> ret = new HashMap<>();
        providers.forEach((s, defineProvider) -> {
            String r = defineProvider.getDefinition(environment);
            if(r != null)
                ret.put(s, r);
        });
        return ret;
    }

}
