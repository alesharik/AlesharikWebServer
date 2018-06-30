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
import com.alesharik.webserver.extension.module.Configuration;
import com.alesharik.webserver.extension.module.ConfigurationValue;
import com.alesharik.webserver.extension.module.Configure;
import com.alesharik.webserver.extension.module.Module;
import com.alesharik.webserver.extension.module.Shutdown;
import com.alesharik.webserver.extension.module.ShutdownNow;
import com.alesharik.webserver.extension.module.Start;
import com.alesharik.webserver.extension.module.meta.ModuleAdapter;

import static org.mockito.Mockito.mock;

@Module("mock2")
public class MockModule2 {
    static final ModuleAdapter MOCK = mock(ModuleAdapter.class);
    static Config CFG = null;

    private Config config;

    @Start
    public void start() {
        MOCK.start();
    }

    @Shutdown
    public void shutdown() {
        MOCK.shutdown();
    }

    @ShutdownNow
    public void shutdownNow() {
        MOCK.shutdownNow();
    }

    @Configure
    public void configure(ConfigurationObject object) {
        CFG = config;
    }

    @Configuration
    static class Config {
        @ConfigurationValue("a")
        int a;
        @ConfigurationValue("b")
        String b;
    }
}
