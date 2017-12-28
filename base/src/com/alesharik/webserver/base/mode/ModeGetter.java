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

package com.alesharik.webserver.base.mode;

import javax.annotation.Nonnull;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates {@link Mode} getter
 *
 * @see ModeClient
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ModeGetters.class)
@Documented
public @interface ModeGetter {
    /**
     * Return the mode
     *
     * @return the mode
     */
    @Nonnull
    Mode value();

    /**
     * Return the condition when this annotation tells truth
     *
     * @return the condition
     */
    @Nonnull
    GetterMode when();

    /**
     * Annotation's true condition
     */
    enum GetterMode {
        /**
         * Annotation will be true if method returns true
         */
        TRUE,
        /**
         * Annotation will be true if method returns false
         */
        FALSE,
        /**
         * Annotation will be true if method returns something(excluding <code>null</code>)
         */
        VALUE,
        /**
         * Annotation will be true if method returns <code>null</code>
         */
        NULL,
        /**
         * Annotation will be true if method throws {@link Exception}
         */
        EXCEPTION
    }
}
