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

package com.alesharik.webserver.configuration.config.ext;

import com.alesharik.webserver.api.agent.ClassHolder;
import com.alesharik.webserver.api.agent.ClassHoldingContext;
import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenInterface;
import com.alesharik.webserver.internals.instance.ClassInstantiationException;
import com.alesharik.webserver.internals.instance.ClassInstantiator;
import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.logger.level.Level;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Level("DefineManager")
@UtilityClass
@ClassPathScanner
@ThreadSafe
public class DefineManager {
    static final ContextImpl providers = new ContextImpl();

    static {
        Logger.getLoggingLevelManager().createLoggingLevel("DefineManager");
        ClassHolder.register(providers);
    }

    @ListenInterface(DefineProvider.class)
    static void listen(Class<?> clazz) {
        if(providers.inReloadState)
            return;
        providers.put(clazz);
    }

    @Nullable
    public static String getDefinition(@Nonnull String name, @Nonnull DefineEnvironment environment) {
        DefineProvider provider = providers.get(name);
        return provider == null ? null : provider.getDefinition(environment);
    }

    @Level("DefineManager")
    static final class ContextImpl implements ClassHoldingContext {
        private final Map<String, DefineProvider> providers = new ConcurrentHashMap<>();
        private final ReentrantLock accessLock = new ReentrantLock();
        private volatile boolean inReloadState = false;

        @Override
        public void reload(@Nonnull Class<?> clazz) {
            System.out.println("Reloading class " + clazz.getCanonicalName());
            put(clazz);
        }

        @Override
        public void pause() {
            accessLock.lock();
            System.out.println("Define class context paused!");
            inReloadState = true;
            providers.clear();
        }

        @Override
        public void resume() {
            inReloadState = false;
            accessLock.unlock();
            System.out.println("Define class context resumed!");
        }

        @Override
        public void destroy() {
            providers.clear();
        }

        DefineProvider get(String name) {
            try {
                accessLock.lockInterruptibly();
                return providers.get(name);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        void put(Class<?> c) {
            try {
                accessLock.lock();
                processClass(c);
            } finally {
                accessLock.unlock();
            }
        }

        private void processClass(Class<?> clazz) {
            try {
                System.out.println("Processing DefineProvider class " + clazz.getCanonicalName());
                DefineProvider instance = (DefineProvider) ClassInstantiator.instantiate(clazz);
                if(providers.containsKey(instance.getName())) {
                    System.err.println("DefineProvider " + instance.getName() + " already exists! Ignoring...");
                    return;
                }
                providers.put(instance.getName(), instance);
            } catch (ClassInstantiationException e) {
                System.err.println("Can't instantiate class " + clazz.getCanonicalName());
                e.printStackTrace();
            }
        }
    }
}
