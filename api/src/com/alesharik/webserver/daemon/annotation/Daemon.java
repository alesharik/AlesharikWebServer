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

package com.alesharik.webserver.daemon.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AlesharikWebServer daemons are threads, which run concurrently with server. Daemons are basically improved Daemon Threads,
 * which can be configured from xml configuration, support hooks, and can be monitored. Daemon can set it's priority by @{@link Priority} annotation.
 * Daemons support inheritance.
 * Lifecycle:<br>
 * <ol>
 * <li>{@link Parse} - parse config in main thread</li>
 * <li>{@link Setup} - setup daemon in daemon thread</li>
 * <li>{@link Run} - main daemon code, executed in daemon thread</li>
 * <li>{@link Reload} - executes iin daemon thread when daemon must be reloaded. Default realisation is: {@link Shutdown} -> {@link Parse} -> {@link Setup} -> {@link Run}</li>
 * <li>{@link Shutdown} - shutdown daemon. Executes in caller thread</li>
 * </ol>
 *
 * @see Api
 * @see ManagementBean
 * @see HookManager
 * @see EventManager
 * @see Logger
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Daemon {
    String value();
}
