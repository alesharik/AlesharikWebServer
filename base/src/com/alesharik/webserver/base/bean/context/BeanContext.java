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

package com.alesharik.webserver.base.bean.context;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * BeanContext will be cleaned when all beans with this context will be cleaned
 */
public interface BeanContext {
    /**
     * Return bean context name
     *
     * @return bean context name
     */
    @Nonnull
    String getName();

    /**
     * Set context property
     *
     * @param key   the key
     * @param value the value
     */
    void setProperty(@Nonnull String key, @Nullable Object value);

    /**
     * Return context property
     *
     * @param key the key
     * @return context property or <code>null</code> if it isn't exist
     */
    @Nullable
    Object getProperty(@Nonnull String key);

    /**
     * Return context property
     *
     * @param key   the key
     * @param clazz value class
     * @return context property or <code>null</code> if it isn't exist
     */
    @Nullable
    default <T> T getProperty(@Nonnull String key, @Nonnull Class<T> clazz) {
        Object v = getProperty(key);
        return v == null ? null : clazz.cast(v);
    }

    /**
     * Remove property
     *
     * @param key the key
     */
    void removeProperty(@Nonnull String key);
}
