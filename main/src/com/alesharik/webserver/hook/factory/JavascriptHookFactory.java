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

package com.alesharik.webserver.hook.factory;

import com.alesharik.webserver.api.agent.Ignored;
import com.alesharik.webserver.configuration.XmlHelper;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import com.alesharik.webserver.hook.Hook;
import com.alesharik.webserver.hook.HookFactory;
import com.alesharik.webserver.hook.HookManager;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public final class JavascriptHookFactory implements HookFactory {
    private static final NashornScriptEngineFactory NASHORN_SCRIPT_ENGINE_FACTORY = new NashornScriptEngineFactory();

    private final ScriptEngine publicScriptEngine;

    private JavascriptHookFactory() {
        publicScriptEngine = NASHORN_SCRIPT_ENGINE_FACTORY.getScriptEngine(new String[]{"-strict"}, HookManager.getClassLoader());
    }

    @Nonnull
    @Override
    public Hook create(@Nonnull Element config, String name) {
        boolean isolated = Boolean.parseBoolean(XmlHelper.getString("isolated", config, true));
        boolean java = Boolean.parseBoolean(XmlHelper.getString("java", config, true));
        String code = XmlHelper.getString("code", config, true);
        ScriptEngine engine;
        if(java && !isolated) {
            engine = publicScriptEngine;
        } else {
            String[] engineConfig;
            if(!java)
                engineConfig = new String[]{"-strict", "--no-java"};
            else
                engineConfig = new String[]{"-strict"};

            engine = NASHORN_SCRIPT_ENGINE_FACTORY.getScriptEngine(engineConfig, HookManager.getClassLoader());
        }
        return new JSHook(name, code, engine);
    }

    @Nonnull
    @Override
    public String getName() {
        return "javascript";
    }

    @Ignored
    private static final class JSHook implements Hook {
        private final String name;
        private final Invocable invocable;

        public JSHook(String name, String code, ScriptEngine nashornScriptEngine) {
            this.name = name;
            try {
                nashornScriptEngine.eval(code);
            } catch (ScriptException e) {
                throw new ConfigurationParseError(e);
            }
            invocable = ((Invocable) nashornScriptEngine);
        }

        @Nonnull
        @Override
        public String getName() {
            return name;
        }

        @Nullable
        @Override
        public String getGroup() {
            return null;
        }

        @Override
        public void listen(@Nullable Object sender, @Nullable Object[] args) {
            try {
                invocable.invokeFunction("run", sender, args);
            } catch (ScriptException e) {
                throw new ConfigurationParseError(e);
            } catch (NoSuchMethodException e) {
                throw new ConfigurationParseError("No such method run(sender, args) in javascript hook " + name);
            }
        }
    }
}
