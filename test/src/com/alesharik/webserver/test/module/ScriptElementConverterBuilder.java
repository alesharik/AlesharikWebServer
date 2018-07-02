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

package com.alesharik.webserver.test.module;

import com.alesharik.webserver.configuration.config.lang.element.ConfigurationCodeElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationElement;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationFunctionElement;
import com.alesharik.webserver.extension.module.meta.ScriptElementConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScriptElementConverterBuilder {
    private final Map<String, ScriptEngine> engines = new ConcurrentHashMap<>();

    public void addEngine(String language, ScriptEngine engine) {
        engines.put(language, engine);
    }

    public ScriptElementConverter build() {
        return new ScriptElementConverterImpl(engines);
    }

    private static final class ScriptElementConverterImpl implements ScriptElementConverter {
        private final Map<String, ScriptEngine> engines;

        public ScriptElementConverterImpl(Map<String, ScriptEngine> engines) {
            this.engines = new HashMap<>(engines);
        }

        @Override
        public boolean isExecutable(@Nonnull ConfigurationElement element) {
            return element instanceof ConfigurationFunctionElement || element instanceof ConfigurationCodeElement;
        }

        @Nullable
        @Override
        public <T> T execute(@Nonnull ConfigurationElement element, @Nonnull Class<T> expected) {
            if(element instanceof ConfigurationCodeElement) {
                ConfigurationCodeElement elem = (ConfigurationCodeElement) element;
                assert engines.containsKey(elem.getLanguageName()) : "Language " + elem.getLanguageName() + " not found!";
                try {
                    //noinspection unchecked
                    return (T) engines.get(elem.getLanguageName()).eval(elem.getCode());
                } catch (ScriptException e) {
                    throw new AssertionError(e);
                }
            } else if(element instanceof ConfigurationFunctionElement) {
                ConfigurationFunctionElement elem = (ConfigurationFunctionElement) element;
                List<ScriptException> exceptions = new ArrayList<>();
                for(ScriptEngine engine : engines.values()) {
                    try {
                        return (T) engine.eval(elem.getCodeInstruction());
                    } catch (ScriptException e) {
                        exceptions.add(e);
                    }
                }
                AssertionError error = new AssertionError();
                for(ScriptException exception : exceptions) error.addSuppressed(exception);
                throw error;
            }
            throw new AssertionError("Can't exec " + element + " element!");
        }
    }
}
