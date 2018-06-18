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

package com.alesharik.webserver.main.script;

import com.alesharik.webserver.api.ExecutionStage;
import com.alesharik.webserver.api.agent.Stages;
import com.alesharik.webserver.api.agent.bean.Beans;
import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenInterface;
import com.alesharik.webserver.api.agent.classPath.SuppressClassLoaderUnloadWarning;
import com.alesharik.webserver.configuration.config.ext.ScriptEngineProvider;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.logger.level.Level;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptEngine;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
@ClassPathScanner
@SuppressClassLoaderUnloadWarning
@Level("script-engine")
@Prefixes("[ScriptEngineManager]")
public class ScriptEngineManager {
    private static final Map<String, ScriptEngineProvider> providers = new ConcurrentHashMap<>();

    static {
        Logger.getLoggingLevelManager().createLoggingLevel("script-engine");
    }

    @ListenInterface(ScriptEngineProvider.class)
    @Stages({ExecutionStage.AGENT, ExecutionStage.PRE_LOAD, ExecutionStage.CORE_MODULES})
    static void listen(Class<?> clazz) {
        System.out.println("Listening " + clazz.getCanonicalName());
        ScriptEngineProvider provider = (ScriptEngineProvider) Beans.create(clazz);
        if(providers.containsKey(provider.getName()))
            System.out.println("Ignoring " + provider.getName() + " because it already exists");
        else {
            System.out.println("Added provider " + provider.getName());
            providers.put(provider.getName(), provider);
        }
    }

    public static boolean languageExists(@Nonnull String lang) {
        return providers.containsKey(lang);
    }

    @Nonnull
    public static ScriptEngineProvider getProvider(@Nonnull String lang) {
        ScriptEngineProvider provider = providers.get(lang);
        if(provider == null)
            throw new IllegalStateException("Provider not found!");
        return provider;
    }

    @Nullable
    public static ScriptEngineProvider getProvider(ScriptEngine engine) {
        for(ScriptEngineProvider provider : providers.values()) {
            if(provider.getEngineFactory().equals(engine.getFactory()))
                return provider;
        }
        return null;
    }

    public static Set<String> getLanguages() {
        return providers.keySet();
    }
}
