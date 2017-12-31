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

package com.alesharik.webserver.api.agent.bean;

import com.alesharik.webserver.base.bean.Bean;
import com.alesharik.webserver.base.bean.DefaultConstructor;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.PostConstruct;

import static org.junit.Assert.assertEquals;

public class BeansTest {
    @BeforeClass
    public static void setupClass() {
        Beans.listenBean(A.class);
        Beans.listenBean(B.class);
        Beans.listenBean(C.class);
    }

    @Test
    public void createSimpleBean() {
        C bean = Beans.getBean(C.class);
        assertEquals("test", bean.a.a);
        assertEquals("test", bean.b.b);
    }

    @Bean
    public static final class A {
        private String a;

        @DefaultConstructor
        public A() {
            a = "test";
        }
    }

    @Bean
    public static final class B {
        private String b;

        @DefaultConstructor
        public B() {
        }

        @PostConstruct
        public void postConstruct() {
            b = "test";
        }
    }

    @Bean
    public static final class C {
        private A a;
        private B b;
    }
}