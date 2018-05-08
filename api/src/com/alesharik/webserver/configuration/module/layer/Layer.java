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

package com.alesharik.webserver.configuration.module.layer;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Layers allow modules to be divided in submodules. They can have lifecycle methods({@link com.alesharik.webserver.configuration.module.Start},
 * {@link com.alesharik.webserver.configuration.module.Shutdown} {@link com.alesharik.webserver.configuration.module.ShutdownNow}). If ASM transformation is enabled, lifecycle functions will
 * automatically invoke lifecycle functions in submodules/sublayers. Layers are non-singleton {@link com.alesharik.webserver.base.bean.Bean}s with autowire
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Layer {
    /**
     * Return layer name
     *
     * @return layer name
     */
    @Nonnull
    String value();

    /**
     * Enable ASM transformations
     *
     * @return is ASM transformations are enabled
     */
    boolean auto() default true;

    /**
     * Enable auto-invoke option. All children's lifecycle events will be invoked automatically. DO NOT USE WITH {@link #auto()} ENABLED
     *
     * @return is auto-invoke option enabled
     */
    boolean autoInvoke() default false;
}
