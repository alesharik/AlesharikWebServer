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

import com.alesharik.webserver.configuration.config.ext.ScriptEngineProvider;
import com.alesharik.webserver.configuration.module.ConfigurationScriptExecutionError;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.annotation.Nonnull;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

public final class JavaScriptEngineProvider implements ScriptEngineProvider {
    private static final ScriptEngineFactory factory = new NashornScriptEngineFactory();
    private static final Helper helper = new HelperImpl();

    @Nonnull
    @Override
    public ScriptEngineFactory getEngineFactory() {
        return factory;
    }

    @Nonnull
    @Override
    public String getName() {
        return "javascript";
    }

    @Nonnull
    @Override
    public Helper getHelper() {
        return helper;
    }

    private static final class HelperImpl implements Helper {
        private static final ThreadLocal<ScriptEngine> engines = ThreadLocal.withInitial(factory::getScriptEngine);

        @Override
        public boolean hasFunction(String name, ScriptEngine engine) {
            Invocable invocable = (Invocable) engine;
            try {
                invocable.invokeFunction(name);
                return true;
            } catch (ScriptException e) {
                throw new ConfigurationScriptExecutionError(e);
            } catch (NoSuchMethodException e) {
                return false;
            }
        }

        @Override
        public boolean hasFunction(String name, String code) {
            ScriptEngine engine = engines.get();
            engine.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
            return hasFunction(name, engine);
        }

        @Override
        public Object executeFunction(String name, ScriptEngine engine) {
            Invocable invocable = (Invocable) engine;
            try {
                return invocable.invokeFunction(name);
            } catch (ScriptException e) {
                throw new ConfigurationScriptExecutionError(e);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Function " + name + " not found!");
            }
        }
    }
}
