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

package com.alesharik.webserver.api.serial;

import javax.annotation.Nonnull;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

/**
 * This annotations defines annotation adapter for class. Annotation adapter is used for reading meta from classes/fields
 * by serializer<br>. If annotation is not present, default annotation adapter will be used for default
 * Known implementations: {@link GsonAnnotationAdapter}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface AnnotationAdapter {
    /**
     * The annotation adapter class
     * @return annotation adapter class
     */
    @Nonnull
    Class<? extends Adapter> value();

    /**
     * Annotation adapter class
     */
    interface Adapter {
        /**
         * Checks if class has version meta
         * @param clazz the class to check
         * @return <code>true</code> - class has version, otherwise <code>false</code>
         * @see Version
         */
        boolean hasVersionAnnotation(@Nonnull Class<?> clazz);

        /**
         * Return version from class
         * @param clazz the class
         * @return class version or <code>-1</code> if requested meta not found
         * @see Version
         */
        double getVersion(@Nonnull Class<?> clazz);

        /**
         * Check if field has 'version when it was created' meta
         * @param field the field
         * @return <code>true</code> - field has requested version, otherwise <code>false</code>
         * @see Since
         */
        boolean hasSinceAnnotation(@Nonnull Field field);

        /**
         * Return 'version when it was created'
         * @param field the field
         * @return 'version when it was created' or -1 if requested meta not found
         * @see Since
         */
        double getSince(@Nonnull Field field);

        /**
         * Check if field has 'version when it was removed - 1' meta
         * @param field the field
         * @return <code>true</code> - field has requested version, otherwise <code>false</code>
         * @see Until
         */
        boolean hasUntilAnnotation(@Nonnull Field field);

        /**
         * Return 'version when it was removed - 1'
         * @param field the field
         * @return 'version when it was removed - 1' or -1 if requested meta not found
         * @see Until
         */
        double getUntil(@Nonnull Field field);
    }
}
