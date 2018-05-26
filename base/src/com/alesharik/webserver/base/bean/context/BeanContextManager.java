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

import com.alesharik.webserver.base.bean.BeanFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * ContextManager manages all {@link BeanContext} and do overrides. In will be instantiated through class instantiation, not bean
 * instantiation
 */
public interface BeanContextManager {
    /**
     * Return new context instance
     */
    @Nonnull
    BeanContext createContext();

    /**
     * Destroy context. It will be executed in reference thread
     *
     * @param context the context
     */
    default void destroyContext(@Nonnull BeanContext context) {

    }

    /**
     * Override bean classes while processing bean dependencies
     *
     * @param bean the bean
     * @return new bean class. <code>null</code> - do not override
     */
    @Nullable
    default Class<?> overrideBeanClass(@Nonnull Class<?> bean) {
        return null;
    }

    /**
     * Override bean factory
     *
     * @param bean the bean
     * @return the bean factory. <code>null</code> - do not override
     */
    @Nullable
    default BeanFactory overrideFactory(@Nonnull Class<?> bean) {
        return null;
    }
}
