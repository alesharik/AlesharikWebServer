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

package com.alesharik.webserver.base.bean;

import com.alesharik.webserver.base.bean.context.BeanContext;
import com.alesharik.webserver.base.bean.context.BeanContextManager;

import javax.annotation.Nonnull;

/**
 * InvocationContext represents current context for {@link javax.annotation.PostConstruct} and {@link javax.annotation.PreDestroy} methods
 */
public interface InvocationContext {
    /**
     * Return current bean context
     */
    @Nonnull
    BeanContext getContext();

    /**
     * Return current bean cotext manager
     */
    @Nonnull
    BeanContextManager getManager();
}
