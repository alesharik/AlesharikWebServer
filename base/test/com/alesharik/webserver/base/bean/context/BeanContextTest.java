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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BeanContextTest {
    @Test
    public void getProperty() {
        BeanContext context = mock(BeanContext.class);
        when(context.getProperty("test")).thenReturn("test");

        assertEquals("test", context.getProperty("test"));

        String prop = context.getProperty("test", String.class);
        assertEquals("test", prop);
    }

    @Test
    public void testNullProperty() {
        BeanContext context = mock(BeanContext.class);
        when(context.getProperty("test")).thenReturn(null);

        assertNull(context.getProperty("test"));

        String prop = context.getProperty("test", String.class);
        assertNull(prop);
    }
}