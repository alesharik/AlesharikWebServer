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

package com.alesharik.webserver.hook;

import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenInterface;
import com.alesharik.webserver.configuration.XmlHelper;
import com.alesharik.webserver.exceptions.error.ConfigurationParseError;
import com.alesharik.webserver.internals.ClassInstantiator;
import lombok.experimental.UtilityClass;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class manages all hooks, created from configuration.
 * Hook configuration sample:
 * <pre>{@code
 * <hook>
 *     <name>test</name>
 *     <factory>javascript</factory>
 *     <configuration>
 *         <java>true</java>
 *         <isolated>true</isolated>
 *         <code>
 *             function run(sender, args) {
 *                 print("test");
 *             }
 *         </code>
 *     </configuration>
 * </hook>
 * }</pre>
 */
@UtilityClass
@ClassPathScanner
public class UserHookManager {
    static final Map<String, HookFactory> hookFactories = new ConcurrentHashMap<>();

    @ListenInterface(HookFactory.class)
    public static void listen(Class<?> clazz) {
        HookFactory instance = (HookFactory) ClassInstantiator.instantiate(clazz);
        if(hookFactories.containsKey(instance.getName()))//Map overwrite protection
            return;
        hookFactories.put(instance.getName(), instance);
    }

    /**
     * Delete all user-defined hooks
     */
    public static void cleanHooks() {
        HookManager.cleanUserDefinedHooks();
    }

    /**
     * Load user-defined hook from configuration
     *
     * @param hookElement hook element(name, factory, config and etc)
     */
    public static void parseHook(@Nonnull Element hookElement) {
        String name = XmlHelper.getString("name", hookElement, true);
        String factory = XmlHelper.getString("factory", hookElement, true);
        if(!hookFactories.containsKey(factory))
            throw new ConfigurationParseError("Can't find hook factory " + factory);

        Element config = XmlHelper.getXmlElement("configuration", hookElement, true);
        Hook hook = hookFactories.get(factory).create(config, name);
        HookManager.add(name, hook);
    }
}
