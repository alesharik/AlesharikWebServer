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

import com.alesharik.webserver.base.bean.Bean;
import com.alesharik.webserver.base.bean.meta.BeanObject;
import com.alesharik.webserver.base.bean.meta.BeanSingleton;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * BeanContext will be cleaned when all beans with this context will be cleaned.
 * Bean contexts are used to isolate objects and singletons from other contexts
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

    /**
     * Provides modifiable list of stored object refs
     *
     * @return the list
     */
    @Nonnull
    List<BeanObject> storedObjects();

    /**
     * Provide modifiable list with all containing singletons
     *
     * @return the list
     */
    @Nonnull
    Map<Class<?>, BeanSingleton> singletons();

    default <T> T getSingleton(Class<?> singleton) {
        return getSingleton(singleton, null);
    }

    <T> T getSingleton(Class<?> singleton, @Nullable Bean beanOverride);

    default <T> T createObject(Class<?> bean) {
        return createObject(bean, null);
    }

    <T> T createObject(Class<?> bean, @Nullable Bean beanOverride);

    default <T> T getBean(Class<?> clazz) {
        return getBean(clazz, null);
    }

    <T> T getBean(Class<?> clazz, @Nullable Bean beanOverride);

    /**
     * Return bean context statistics
     *
     * @return the statistics
     */
    @Nonnull
    BeanContextMXBean getStats();

    /**
     * Checks if one of the loaded classes or bean context itself depends on classloader
     *
     * @param classLoader the classloader
     * @return <code>true</code> -  one of the loaded classes or bean context itself depends on classloader, otherwise <code>false</code>
     */
    boolean isLoadedBy(@Nonnull ClassLoader classLoader);

    default void preDestroy() {
    }
}
