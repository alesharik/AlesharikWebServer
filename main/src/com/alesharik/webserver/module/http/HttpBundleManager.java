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
import com.alesharik.webserver.api.server.wrapper.bundle.HttpBundle;
import com.alesharik.webserver.api.server.wrapper.bundle.HttpHandlerBundle;
import com.alesharik.webserver.logger.Prefixes;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class listen Http Bundles and hold it's classes
 */
@ClassPathScanner
@UtilityClass
@Prefixes({"[HTTP]", "[HttpBundleManager]"})
public class HttpBundleManager {
    static final Map<String, Class<?>> bundles = new ConcurrentHashMap<>();

    @ListenInterface(HttpHandlerBundle.class)
    static void listenBundle(Class<?> bundle) {
        if(!bundle.isAnnotationPresent(HttpBundle.class)) {
            System.err.println("Http bundle " + bundle.getCanonicalName() + " must have HttpBundle annotation!");
            return;
        }
        HttpBundle annotation = bundle.getAnnotation(HttpBundle.class);
        if(bundles.containsKey(annotation.value())) //Overwrite protection
            return;

        bundles.put(annotation.value(), bundle);
    }

    @Nullable
    static Class<?> getBundleClass(@Nonnull String name) {
        return bundles.get(name);
    }
}
