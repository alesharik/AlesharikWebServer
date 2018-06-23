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

package com.alesharik.webserver.test;

import com.alesharik.webserver.api.agent.bean.Contexts;
import com.alesharik.webserver.base.bean.context.BeanContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public abstract class AbstractContextTest<T extends BeanContext> {
    protected T context;
    private final Class<?> clazz;

    protected AbstractContextTest(Class<?> context) {
        this.clazz = context;
    }

    @Before
    public void setUp() throws Exception {
        //noinspection unchecked
        context = (T) Contexts.createContext((Class<? extends BeanContext>) clazz);
    }

    @After
    public void tearDown() {
        Contexts.destroyContext(context);
    }

    @Test
    public void name() {
        assertNotNull(context.getName());
    }

    @Test
    public void props() {
        assertNull(context.getProperty("a"));
        context.setProperty("a", "b");
        assertEquals("b", context.getProperty("a"));
        assertSame(String.class, context.getProperty("a", String.class).getClass());
        assertEquals("b", context.getProperty("a", String.class));

        context.setProperty("a", null);
        assertNull(context.getProperty("a"));
        context.setProperty("a", "b");
        assertEquals("b", context.getProperty("a"));
        context.removeProperty("a");
        assertNull(context.getProperty("a"));
    }

    @Test
    public void basicSingleton() {
        SingletonA singletonA = context.getBean(SingletonA.class);
        assertEquals("a", singletonA.a.a);
        assertEquals("b", singletonA.b.b);
        SingletonA singletonA1 = context.getBean(SingletonA.class);
        assertSame(singletonA, singletonA1);
    }

    @Test
    public void basicBean() {
        BeanC beanC = context.getBean(BeanC.class);
        assertEquals("a", beanC.a.a);
        assertEquals("b", beanC.b.b);
        BeanC beanC1 = context.getBean(BeanC.class);
        assertNotSame(beanC, beanC1);
    }
}
