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

package com.alesharik.webserver.configuration.module;

import com.alesharik.webserver.configuration.module.meta.ConfigurationLinker;
import com.alesharik.webserver.configuration.module.meta.impl.ConfigurationLinkerImpl;

import javax.annotation.Nonnull;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates that this class is a module.
 * Modules are non-singleton {@link com.alesharik.webserver.base.bean.Bean}s with autowire. Every module ans it's parts
 * will be run in it's own bean context.<br>
 * Module can reloaded in 2 ways: <ul>
 *     <li>Soft way - call {@link Reload} method if it exists - happens only when new config is received or on user request</li>
 *     <li>Hard update (default reload strategy) - {@link Shutdown}, {@link Configure} and {@link Start} again - happens when module shot down with error
 *      and auto-restart option is enabled</li>
 * </ul>.
 * All exceptions in {@link Configure} method will be threated as configuration exceptions(as well as missing required config fields)
 * and will throw configuration error to stop the server. All exceptions in {@link Start}, {@link Shutdown}, {@link ShutdownNow} or {@link Reload}
 * method will be threated as module errors, the server will try to restart the module or ignore it(depends on auto-restart option); but
 * {@link ConfigurationError} error will be processed as configuration error
 *
 * @see Configuration
 * @see Configure
 * @see Shutdown
 * @see Start
 * @see ShutdownNow
 * @see Reload
 * @see ConfigurationError
 * @see com.alesharik.webserver.configuration.module.layer.Layer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Module {
    /**
     * Return module's name
     *
     * @return module's name
     */
    @Nonnull
    String value();

    /**
     * Enable auto-invoke option. All children's lifecycle events will be invoked automatically before module's event
     *
     * @return is auto-invoke option enabled
     */
    boolean autoInvoke() default true;

    Class<? extends ConfigurationLinker> linker() default ConfigurationLinkerImpl.class;
}
