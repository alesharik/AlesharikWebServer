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

package com.alesharik.webserver.extension.module.platform;

import com.alesharik.webserver.configuration.config.lang.element.ConfigurationObject;
import com.alesharik.webserver.configuration.config.lang.element.ConfigurationTypedObject;
import com.alesharik.webserver.extension.module.Configuration;
import com.alesharik.webserver.extension.module.ConfigurationValue;
import com.alesharik.webserver.extension.module.Module;
import com.alesharik.webserver.extension.module.Reload;
import com.alesharik.webserver.extension.module.Shutdown;
import com.alesharik.webserver.extension.module.ShutdownNow;
import com.alesharik.webserver.extension.module.Start;
import com.alesharik.webserver.extension.module.layer.Layer;
import com.alesharik.webserver.extension.module.layer.SubModule;
import com.alesharik.webserver.extension.module.layer.meta.LayerAdapter;
import com.alesharik.webserver.extension.module.layer.meta.SubModuleAdapter;
import com.alesharik.webserver.extension.module.meta.ModuleAdapter;

import static org.mockito.Mockito.mock;

@Module("mock1")
@Configuration
public class MockModule1 {
    static final ModuleAdapter MOCK = mock(ModuleAdapter.class);
    static final LayerAdapter LAYER_MOCK = mock(LayerAdapter.class);
    static final SubModuleAdapter SUB_MODULE_MOCK = mock(SubModuleAdapter.class);

    static String A = "";

    @ConfigurationValue("a")
    private String a;
    private final LayerImpl layer = new LayerImpl();

    @Start
    public void start() {
        MOCK.start();
        A = a;
    }

    @Shutdown
    public void shutdown() {
        MOCK.shutdown();
    }

    @ShutdownNow
    public void shutdownNow() {
        MOCK.shutdownNow();
    }

    @Reload
    public void reload(ConfigurationObject object) {
        MOCK.reload((ConfigurationTypedObject) object, null);
    }

    @Layer("main")
    private static final class LayerImpl {
        private final SubModuleImpl subModule = new SubModuleImpl();

        @Start
        public void start() {
            LAYER_MOCK.start();
        }

        @Shutdown
        public void shutdown() {
            LAYER_MOCK.shutdown();
        }

        @ShutdownNow
        public void shutdownNow() {
            LAYER_MOCK.shutdownNow();
        }

        @SubModule("s")
        private static final class SubModuleImpl {
            @Start
            public void start() {
                SUB_MODULE_MOCK.start();
            }

            @Shutdown
            public void shutdown() {
                SUB_MODULE_MOCK.shutdown();
            }

            @ShutdownNow
            public void shutdownNow() {
                SUB_MODULE_MOCK.shutdownNow();
            }
        }
    }
}
