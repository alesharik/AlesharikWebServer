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

package com.alesharik.webserver.configuration.config;

import com.alesharik.webserver.extension.module.Configuration;
import com.alesharik.webserver.extension.module.ConfigurationValue;
import com.alesharik.webserver.extension.module.Module;
import com.alesharik.webserver.extension.module.Shutdown;
import com.alesharik.webserver.extension.module.Start;
import com.alesharik.webserver.extension.module.layer.Layer;
import com.alesharik.webserver.extension.module.layer.SubModule;

@Module("test")
@Configuration
public class TestModule {
    @ConfigurationValue("a")
    private String test;
    private MainLayer mainLayer;

    private TestModule me; //autowired

    @Start
    private void start() {
        System.out.println("start");
    }

    @Shutdown
    private void shutdown() {
        System.out.println("Shutdown");
    }

    @Layer("main")
    private static final class MainLayer {
        private SubModuleA subModuleA;

        @Start
        public void start() {

        }
    }

    @SubModule("a")
    private static final class SubModuleA {
        @Start
        public void start() {

        }
    }
}
