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

package com.alesharik.webserver.module.http;

import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenInterface;
import com.alesharik.webserver.api.server.wrapper.addon.AddOn;
import com.alesharik.webserver.logger.Debug;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.logger.level.Level;
import com.alesharik.webserver.logger.level.LoggingLevel;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
@ClassPathScanner
@Level("http/addon/manager")
@Prefixes({"[HTTP]", "[AddOn]", "[AddOnManager]"})
class AddOnManager {
    private static final Map<String, AddOn> addOns = new ConcurrentHashMap<>();

    static {
        LoggingLevel loggingLevel = Logger.getLoggingLevelManager().createLoggingLevel("http/addon/manager");
        loggingLevel.enable();
    }

    @ListenInterface(AddOn.class)
    static void listen(Class<?> clazz) {
        Debug.log("New AddOn found: " + clazz.getCanonicalName());
        try {
            AddOn addOn = (AddOn) clazz.newInstance();
            addOns.put(addOn.getName(), addOn);
            Debug.log("New AddOn registered: " + clazz.getCanonicalName());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    static AddOn getAddOn(String addon) {
        return addOns.get(addon);
    }
}
