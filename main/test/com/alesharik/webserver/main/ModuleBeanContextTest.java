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

package com.alesharik.webserver.main;

import com.alesharik.webserver.configuration.config.ext.ScriptManager;
import com.alesharik.webserver.configuration.config.lang.ApiEndpointSection;
import com.alesharik.webserver.configuration.config.lang.ConfigurationEndpoint;
import com.alesharik.webserver.configuration.config.lang.ConfigurationModule;
import com.alesharik.webserver.configuration.config.lang.CustomEndpointSection;
import com.alesharik.webserver.configuration.config.lang.ExternalLanguageHelper;
import com.alesharik.webserver.configuration.config.lang.ScriptEndpointSection;
import com.alesharik.webserver.configuration.module.ModuleBeanContext;
import com.alesharik.webserver.configuration.module.meta.ScriptElementConverter;
import com.alesharik.webserver.configuration.utils.CoreModuleManager;
import com.alesharik.webserver.main.script.ScriptEngineImpl;
import com.alesharik.webserver.test.AbstractContextTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ModuleBeanContextTest extends AbstractContextTest<ModuleBeanContext> {
    public ModuleBeanContextTest() {
        super(ModuleBeanContext.class);
    }

    @BeforeClass
    public static void before() {
        Main.coreModuleManager = new CoreModuleManagerImpl(new File(""));
        ConfigurationEndpoint mock = new ConfigurationEndpoint() {
            @Override
            public List<ConfigurationModule> getModules() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public CustomEndpointSection getCustomSection(String name) {
                return null;
            }

            @Override
            public ApiEndpointSection getApiSection() {
                return null;
            }

            @Override
            public ScriptEndpointSection getScriptSection() {
                return null;
            }

            @Override
            public List<ExternalLanguageHelper> getHelpers() {
                return null;
            }
        };
        Main.scriptEngine = new ScriptEngineImpl(path -> Collections.emptyList(), mock);
    }

    @AfterClass
    public static void after() {
        Main.coreModuleManager = null;
        Main.scriptEngine = null;
    }

    @Test
    public void singletons() {
        assertEquals(Main.coreModuleManager, context.getSingleton(CoreModuleManager.class));
        assertEquals(Main.scriptEngine, context.getSingleton(ScriptManager.class));
        assertEquals(Main.scriptEngine, context.getSingleton(ScriptElementConverter.class));
    }
}