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

import java.lang.reflect.Field;

/**
 * Adapter for @{@link Version}, @{@link Since}, {@link Until} annotations
 */
final class DefaultAnnotationAdapter implements AnnotationAdapter.Adapter {
    static final AnnotationAdapter.Adapter INSTANCE = new DefaultAnnotationAdapter();

    @Override
    public boolean hasVersionAnnotation(Class<?> clazz) {
        return clazz.isAnnotationPresent(Version.class);
    }

    @Override
    public double getVersion(Class<?> clazz) {
        return clazz.getAnnotation(Version.class).value();
    }

    @Override
    public boolean hasSinceAnnotation(Field field) {
        return field.isAnnotationPresent(Since.class);
    }

    @Override
    public double getSince(Field field) {
        return field.getAnnotation(Since.class).value();
    }

    @Override
    public boolean hasUntilAnnotation(Field field) {
        return field.isAnnotationPresent(Until.class);
    }

    @Override
    public double getUntil(Field field) {
        return field.getAnnotation(Until.class).value();
    }
}
