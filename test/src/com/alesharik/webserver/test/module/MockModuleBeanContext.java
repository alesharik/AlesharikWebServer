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

package com.alesharik.webserver.test.module;

import com.alesharik.webserver.api.agent.bean.AbstractBeanContext;
import com.alesharik.webserver.base.bean.Bean;
import com.alesharik.webserver.base.bean.context.BeanContext;
import com.alesharik.webserver.base.bean.context.BeanContextManager;
import com.alesharik.webserver.base.bean.context.Manager;

import javax.annotation.Nonnull;

@Manager(MockModuleBeanContext.Manager.class)
public class MockModuleBeanContext extends AbstractBeanContext {
    private MockModuleBeanContext() {
        super("mock");
    }

    @Override
    public <T> void linkSingleton(Class<T> clazz, T instance) {
        super.linkSingleton(clazz, instance);
    }

    @Override
    public <T> void linkSingleton(Class<T> clazz, T instance, Bean override) {
        super.linkSingleton(clazz, instance, override);
    }

    public static final class Manager implements BeanContextManager {

        @Nonnull
        @Override
        public BeanContext createContext() {
            return new MockModuleBeanContext();
        }
    }
}
