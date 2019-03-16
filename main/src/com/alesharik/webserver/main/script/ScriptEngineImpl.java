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

import com.alesharik.webserver.api.collections.TripleHashMap;
import com.alesharik.webserver.configuration.config.ext.ScriptEngineProvider;
import com.alesharik.webserver.configuration.config.ext.ScriptExecutionError;
import com.alesharik.webserver.configuration.config.ext.ScriptManager;
import com.alesharik.webserver.configuration.config.lang.ConfigurationEndpoint;
import com.alesharik.webserver.configuration.config.lang.ConfigurationModule;
import com.alesharik.webserver.configuration.config.lang.ExecutionContext;
import com.alesharik.webserver.configuration.config.lang.ExternalLanguageHelper;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationCodeElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationFunctionElement;
import com.alesharik.webserver.configuration.config.lang.parser.FileReader;
import com.alesharik.webserver.exception.error.UnexpectedBehaviorError;
import com.alesharik.webserver.extension.module.ConfigurationError;
import com.alesharik.webserver.extension.module.meta.ScriptElementConverter;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.logger.level.Level;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Level("script-engine")
@Prefixes("[ScriptEngine]")
public final class ScriptEngineImpl implements ScriptElementConverter, ScriptManager {
    static {
        Logger.getLoggingLevelManager().createLoggingLevel("script-engine");
    }

    private final FileReader fileReader;

    private final ConfigurationEndpoint endpoint;
    private final TripleHashMap<ExternalLanguageHelper, String, ConfigurationModule> codeCache = new TripleHashMap<>();
    private final List<ExternalLanguageHelper> helpers = new ArrayList<>();

    private final Map<String, ScriptEngine> globals = new HashMap<>();
    private final Map<Pair<String, ConfigurationModule>, ScriptEngine> moduleEngines = new HashMap<>();

    public void prepare() {
        long start = System.nanoTime();

        buildCodeCache();
        buildGlobalEngines();
        buildGlobals();

        System.out.println("Elapsed time: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + "ms");
    }

    private void buildGlobalEngines() {
        for(String s : ScriptEngineManager.getLanguages()) {
            ScriptEngine engine = ScriptEngineManager.getProvider(s).getEngineFactory().getScriptEngine();
            globals.put(s, engine);
        }
    }

    private void buildCodeCache() {
        for(ExternalLanguageHelper externalLanguageHelper : endpoint.getHelpers())
            buildHelperCache(externalLanguageHelper, null);
        for(ConfigurationModule configurationModule : endpoint.getModules())
            buildModuleCodeCache(configurationModule);
    }

    private void buildHelperCache(ExternalLanguageHelper externalLanguageHelper, ConfigurationModule module) {
        helpers.add(externalLanguageHelper);
        Path path = externalLanguageHelper.getHelperFile().getAbsoluteFile().toPath();
        if(!fileReader.canRead(externalLanguageHelper.getHelperFile()))
            throw new ConfigurationError("Can't find helper file " + path);

        String code = fileReader.readFile(path).stream()
                .reduce((s, s2) -> s + '\n' + s2)
                .get();
        codeCache.put(externalLanguageHelper, code, module);
    }

    private void buildModuleCodeCache(ConfigurationModule module) {
        for(ExternalLanguageHelper externalLanguageHelper : module.getHelpers())
            buildHelperCache(externalLanguageHelper, module);
        for(ConfigurationModule configurationModule : module.getModules())
            buildModuleCodeCache(configurationModule);
    }

    private void buildGlobals() {
        for(ExternalLanguageHelper helper : helpers) {
            if(!ScriptEngineManager.languageExists(helper.getLanguage()))
                throw new ConfigurationError("Language " + helper.getLanguage() + " not found!");

            if(helper.getContext() != ExecutionContext.GLOBAL)
                continue;

            ScriptEngine engine = globals.get(helper.getLanguage());
            try {
                engine.eval(codeCache.get(helper));
            } catch (ScriptException e) {
                throw new ScriptExecutionError(helper, e);
            }
        }
    }

    @Override
    public boolean isExecutable(@Nonnull ConfigurationElement element) {
        return element instanceof ConfigurationCodeElement || element instanceof ConfigurationFunctionElement;
    }

    @Nullable
    @Override
    public <T> T execute(@Nonnull ConfigurationElement element, @Nonnull Class<T> expected) {
        Object ret;
        if(element instanceof ConfigurationCodeElement)
            ret = executeCodeElement((ConfigurationCodeElement) element);
        else if(element instanceof ConfigurationFunctionElement)
            ret = executeFunctionElement((ConfigurationFunctionElement) element);
        else
            throw new IllegalArgumentException("Element " + element.getClass().getCanonicalName() + " is not an executable!");
        Class<?> clazz = ret.getClass();
        if(expected.isAssignableFrom(clazz))
            //noinspection unchecked
            return (T) ret;
        else
            throw new ConfigurationError("Returned object with class " + clazz.getCanonicalName() + " doesn't match expected class " + expected.getCanonicalName() + " !");
    }

    private Object executeCodeElement(ConfigurationCodeElement element) {
        ScriptEngine engine = globals.get(element.getLanguageName());
        if(engine == null)
            throw new ConfigurationError("Language " + element.getLanguageName() + " not found!");
        ScriptEngineProvider provider = ScriptEngineManager.getProvider(engine);
        try {
            return provider.getHelper().executeCode(element.getCode(), engine);
        } catch (ScriptExecutionError e) {
            throw new ScriptExecutionError(element, e.getCause());
        }
    }

    private Object executeFunctionElement(ConfigurationFunctionElement element) {
        ScriptEngine engine = getEngine(element);
        ScriptEngineProvider provider = ScriptEngineManager.getProvider(engine);
        if(provider == null)
            throw new UnexpectedBehaviorError("Provider for engine " + engine + " not found!");
        ScriptEngineProvider.Helper helper = provider.getHelper();
        return helper.executeFunction(element.getName(), engine);
    }

    private ScriptEngine getEngine(ConfigurationFunctionElement element) {
        for(Map.Entry<String, ScriptEngine> stringScriptEngineEntry : globals.entrySet()) {
            ScriptEngineProvider.Helper helper = ScriptEngineManager.getProvider(stringScriptEngineEntry.getKey()).getHelper();
            if(helper.hasFunction(element.getName(), stringScriptEngineEntry.getValue()))
                return stringScriptEngineEntry.getValue();
        }
        for(ExternalLanguageHelper helper : helpers) {
            if(helper.getContext() == ExecutionContext.GLOBAL)
                continue;

            ScriptEngineProvider provider = ScriptEngineManager.getProvider(helper.getLanguage());
            ScriptEngineProvider.Helper h = provider.getHelper();
            if(h.hasFunction(element.getName(), codeCache.get(helper))) {
                if(helper.getContext() == ExecutionContext.CALL)
                    return provider.getEngineFactory().getScriptEngine();
                else {//context == Module
                    ConfigurationModule module = codeCache.getAddition(helper);
                    Pair<String, ConfigurationModule> key;
                    if(module == null) //endpoint context
                        key = Pair.of(helper.getLanguage(), null);
                    else
                        key = Pair.of(helper.getLanguage(), module);
                    ScriptEngine engine = moduleEngines.get(key);
                    if(engine == null) {
                        engine = provider.getEngineFactory().getScriptEngine();
                        moduleEngines.put(key, engine);
                    }
                    return engine;
                }
            }
        }
        throw new ConfigurationError("Function error: function " + element.getName() + " not found! Element: " + element);
    }

    @Override
    public ScriptEngineFactory getEngineFactory(String name) {
        return ScriptEngineManager.getProvider(name).getEngineFactory();
    }

    @Override
    public boolean hasLanguage(String name) {
        return ScriptEngineManager.languageExists(name);
    }

    @Override
    public Set<String> getLanguages() {
        return ScriptEngineManager.getLanguages();
    }

    @Nullable
    @Override
    public ScriptEngineProvider.Helper getHelper(@Nonnull String name) {
        return ScriptEngineManager.languageExists(name) ? ScriptEngineManager.getProvider(name).getHelper() : null;
    }
}
