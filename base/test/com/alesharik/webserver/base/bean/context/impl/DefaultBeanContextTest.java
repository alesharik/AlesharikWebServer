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
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class DefaultBeanContextTest {
    private DefaultBeanContext.Manager manager;

    @Before
    public void setUp() throws Exception {
        manager = new DefaultBeanContext.Manager();
    }

    @Test
    public void testManager() {
        assertNull(manager.overrideBeanClass(Class.class));
        assertNull(manager.overrideFactory(Class.class));

        manager.destroyContext(mock(BeanContext.class));//Test if this method doesn't throw any exceptions
    }

    @Test
    public void testContext() {
        assertEquals("default", manager.createContext().getName());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setContextProperty() {
        manager.createContext().setProperty("test", new Object());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getContextProperty() {
        manager.createContext().getProperty("test");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeContextProperty() {
        manager.createContext().removeProperty("test");
    }
}