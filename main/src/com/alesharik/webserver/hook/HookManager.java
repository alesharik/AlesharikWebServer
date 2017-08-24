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

import com.alesharik.webserver.api.agent.Agent;
import com.alesharik.webserver.api.agent.Rescanable;
import com.alesharik.webserver.api.agent.classPath.ListenInterface;
import com.alesharik.webserver.internals.ClassInstantiator;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public final class HookManager {//TODO mxbean, custom jar loading, tests(!)
    private static final Map<String, Hook> hooks = new ConcurrentHashMap<>();
    private static final Map<String, Hook> userDefinedHooks = new ConcurrentHashMap<>();
    private static final HookClassLoader hookClassLoader = new HookClassLoader(HookManager.class.getClassLoader());

    @ListenInterface(Hook.class)
    public static void listen(Class<?> clazz) {
        Hook instance = (Hook) ClassInstantiator.instantiate(clazz);
        if(instance.getGroup() == null) {
            System.err.println("Hook " + instance.getName() + " has no group! Ignoring...");
            return;
        }

        hooks.put(instance.getGroup() + "." + instance.getName(), instance);
    }

    public static void addHookJar(URL jarUrl) {
        hookClassLoader.addURL(jarUrl);
        Agent.tryScanClassLoader(hookClassLoader);
    }

    public static ClassLoader getClassLoader() {
        return hookClassLoader;
    }

    static void add(String name, Hook hook) {
        userDefinedHooks.put(name, hook);
    }

    static void cleanUserDefinedHooks() {
        userDefinedHooks.clear();
    }

    @Nonnull
    public static Hook getHookForName(String hook) {
        if(hooks.containsKey(hook))
            return hooks.get(hook);
        else return userDefinedHooks.getOrDefault(hook, null);
    }

    @Rescanable
    private static final class HookClassLoader extends URLClassLoader {

        public HookClassLoader(ClassLoader parent) {
            super(new URL[0], parent);
        }

        @Override
        protected void addURL(URL url) {
            super.addURL(url);
        }
    }
}
