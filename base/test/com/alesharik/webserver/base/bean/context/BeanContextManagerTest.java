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

import org.junit.Test;

import javax.annotation.Nonnull;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class BeanContextManagerTest {
    @Test
    public void defaults() {
        Impl impl = new Impl();

        assertNull(impl.overrideBeanClass(Class.class));
        assertNull(impl.overrideFactory(Class.class));

        impl.destroyContext(mock(BeanContext.class));//Test if this method doesn't throw any exceptions
    }

    private static final class Impl implements BeanContextManager {

        @Nonnull
        @Override
        public BeanContext createContext() {
            return null;
        }
    }
}