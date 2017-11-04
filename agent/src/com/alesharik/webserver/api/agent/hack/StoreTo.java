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

package com.alesharik.webserver.api.agent.hack;

import javax.annotation.meta.TypeQualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation binds local variable to specified superclass field. Variable is binding in write-only mode
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE})
@TypeQualifier
public @interface StoreTo {
    /**
     * Variable name
     *
     * @return variable name or empty string for disabling replacement
     */
    String value();

    /**
     * Superclass name
     *
     * @return superclass's(where field is located) internal name or empty string for disabling replacement
     */
    String owner();

    /**
     * Field descriptor
     *
     * @return field descriptor or empty string for disabling replacement
     */
    String descriptor();

    /**
     * Return true if field is static, overwise false
     */
    boolean isStatic() default false;
}
