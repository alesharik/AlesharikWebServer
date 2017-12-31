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

package com.alesharik.webserver.base.bean.context.impl;

import com.alesharik.webserver.base.bean.context.BeanContext;
import com.alesharik.webserver.base.bean.context.BeanContextManager;
import com.alesharik.webserver.base.bean.context.Manager;
import com.alesharik.webserver.base.bean.context.SuppressMemoryLeakSafety;

import javax.annotation.Nonnull;

/**
 * This class represents default bean context
 */
@Manager(DefaultBeanContext.Manager.class)
@SuppressMemoryLeakSafety(warning = false)
public final class DefaultBeanContext implements BeanContext {
    DefaultBeanContext() {
    }

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public void setProperty(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getProperty(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeProperty(String name) {
        throw new UnsupportedOperationException();
    }

    public static final class Manager implements BeanContextManager {
        Manager() {
        }

        @Nonnull
        @Override
        public BeanContext createContext() {
            return new DefaultBeanContext();
        }
    }
}
