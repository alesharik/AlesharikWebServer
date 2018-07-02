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

import com.alesharik.webserver.api.agent.bean.Contexts;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationTypedObject;
import com.alesharik.webserver.extension.module.meta.ModuleAdapter;
import com.alesharik.webserver.extension.module.meta.ModuleMetaFactory;
import com.alesharik.webserver.extension.module.meta.ModuleProvider;
import com.alesharik.webserver.extension.module.meta.ScriptElementConverter;
import lombok.RequiredArgsConstructor;
import org.junit.Before;

import java.util.function.Consumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
public abstract class ModuleTest<T> {
    private final Supplier<T> supplier;
    private final ModuleProvider moduleProvider;
    private final Consumer<MockModuleBeanContext> contextConsumer;
    private ModuleAdapter adapter;
    protected T module;

    @Before
    void before() {
        module = supplier.get();
        MockModuleBeanContext context = (MockModuleBeanContext) Contexts.createContext(MockModuleBeanContext.class);
        contextConsumer.accept(context);
        adapter = ModuleMetaFactory.create(module, moduleProvider, context);
    }

    public void start() {
        adapter.start();
    }

    public void shutdown() {
        adapter.shutdown();
    }

    public void shutdownNow() {
        adapter.shutdownNow();
    }

    public void configure(ConfigurationTypedObject object, ScriptElementConverter converter) {
        adapter.configure(object, converter);
    }

    public void reload(ConfigurationTypedObject object, ScriptElementConverter converter) {
        adapter.reload(object, converter);
    }
}
