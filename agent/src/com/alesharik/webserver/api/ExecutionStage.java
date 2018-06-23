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

package com.alesharik.webserver.api;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This class represents the current loading state
 */
public enum ExecutionStage {
    /**
     * Server isn't started, agent isn't started
     */
    NOT_STARTED,
    /**
     * Agent started
     */
    AGENT,
    /**
     * Server does his startup work
     */
    PRE_LOAD,
    /**
     * Server loads core modules
     */
    CORE_MODULES,
    /**
     * Server reads configuration
     */
    CONFIG,
    /**
     * Server loads modules
     */
    LOAD_EXTENSIONS,
    /**
     * Server is executing configuration
     */
    START,
    /**
     * Server is starting all necessary components, all modules already started
     */
    POST_START,
    /**
     * Server is in execution state: all files are loaded, all modules started
     */
    EXECUTE;

    private static volatile ExecutionStage state = NOT_STARTED;
    private static volatile boolean enabled = false;

    @Nonnull
    public static ExecutionStage getCurrentStage() {
        return state;
    }

    public static void enable() {
        enabled = true;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setState(@Nonnull ExecutionStage state) {
        Class<?> caller = CallerClass.INSTANCE.getCaller();
        if(!caller.isAnnotationPresent(AuthorizedImpl.class) || !caller.getCanonicalName().startsWith("com.alesharik.webserver"))
            throw new SecurityException("Class " + caller + " doesn't have any rights to change the state!");
        ExecutionStage.state = state;
    }

    public static boolean valid(ExecutionStage[] stages) {
        for(ExecutionStage stage : stages) {
            if(stage == ExecutionStage.state)
                return true;
        }
        return false;
    }

    static final class CallerClass extends SecurityManager {
        static final CallerClass INSTANCE = new CallerClass();

        public Class<?> getCaller() {
            return getClassContext()[2];
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface AuthorizedImpl {
    }
}
