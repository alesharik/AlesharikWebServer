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
import com.alesharik.webserver.configuration.config.lang.ConfigurationEndpoint;
import com.alesharik.webserver.configuration.module.ModuleBeanContext;
import com.alesharik.webserver.configuration.module.meta.ScriptElementConverter;
import com.alesharik.webserver.configuration.utils.CoreModuleManager;
import com.alesharik.webserver.main.script.ScriptEngineImpl;
import com.alesharik.webserver.test.AbstractContextTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ModuleBeanContextTest extends AbstractContextTest<ModuleBeanContext> {
    public ModuleBeanContextTest() {
        super(ModuleBeanContext.class);
    }

    @BeforeClass
    public static void before() {
        Main.coreModuleManager = new CoreModuleManagerImpl(new File(""));
        ConfigurationEndpoint mock = mock(ConfigurationEndpoint.class);
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