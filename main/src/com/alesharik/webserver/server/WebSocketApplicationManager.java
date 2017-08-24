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

package com.alesharik.webserver.server;

import com.alesharik.webserver.api.agent.classPath.ClassPathScanner;
import com.alesharik.webserver.api.agent.classPath.ListenAnnotation;
import com.alesharik.webserver.logger.Prefixes;
import com.alesharik.webserver.server.api.WSApplication;
import com.alesharik.webserver.server.api.WSChecker;
import lombok.experimental.UtilityClass;
import org.glassfish.grizzly.websockets.WebSocketApplication;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This class register and holds all websocket applications
 */
@ClassPathScanner
@UtilityClass
@Prefixes("[WebSocketApplicationManager]")
public class WebSocketApplicationManager {
    private static final Set<WebSocketApplication> applications = new CopyOnWriteArraySet<>();

    @ListenAnnotation(WSApplication.class)
    static void listenWebsocketApplication(Class<?> clazz) {
        if(WebSocketApplication.class.isAssignableFrom(clazz)) {
            try {
                WSApplication application = clazz.getAnnotation(WSApplication.class);
                Class<?> checker = application.checker();
                if(!WSChecker.class.isAssignableFrom(checker)) {
                    checker = WSChecker.class;
                    System.out.println("Checker of class " + clazz.toString() + " must extend WSChecker");
                }

                WSChecker wsChecker = (WSChecker) checker.newInstance();
                if(!wsChecker.enabled()) {
                    return;
                }

                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                WebSocketApplication app = (WebSocketApplication) constructor.newInstance();
                if(applications.contains(app))//Overwrite protection
                    return;
                applications.add(app);

                //TODO rewrite
            } catch (NoSuchMethodException e) {
                System.err.println("Class " + clazz.getCanonicalName() + " doesn't have empty constructor!");
            } catch (SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Class " + clazz.toString() + " use WSApplication annotation but not extend WebSocketApplication class");
        }
    }

    public static Set<WebSocketApplication> getApplications() {
        return Collections.unmodifiableSet(applications);
    }
}
