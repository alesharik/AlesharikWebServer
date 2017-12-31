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

package com.alesharik.webserver.api.agent.bean;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation enables/disables wiring for field/constructor args.
 * Wiring ignore all final fields.
 * Special wiring conditions:<br>
 * 1. {@link com.alesharik.webserver.api.agent.bean.context.BeanContext} or it's children will be wired to current bean context<br>
 * 2. {@link com.alesharik.webserver.api.agent.bean.context.BeanContextManager} or it's children be wired to current bean context manager<br>
 * 3. Field with name <code>me</code> will be wired to current class instance
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface Wire {
    /**
     * <code>true</code> - enable, <code>false</code> - disable
     */
    boolean value();
}
