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

import com.alesharik.webserver.internals.instance.Factory;
import com.alesharik.webserver.internals.instance.FactoryMethod;
import com.google.gson.annotations.Since;
import com.google.gson.annotations.Until;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

/**
 * Adapter for default @{@link Version} annotation and GSON's {@link Since} and @{@link Until} annotation
 */
@Factory
public final class GsonAnnotationAdapter implements AnnotationAdapter.Adapter {
    private static final GsonAnnotationAdapter INSTANCE = new GsonAnnotationAdapter();

    private GsonAnnotationAdapter() {
    }

    @FactoryMethod
    @Nonnull
    private static GsonAnnotationAdapter getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean hasVersionAnnotation(Class<?> clazz) {
        return true;
    }

    @Override
    public double getVersion(Class<?> clazz) {
        return clazz.isAnnotationPresent(Version.class) ? clazz.getAnnotation(Version.class).value() : -1;
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
