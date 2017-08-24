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

package com.alesharik.webserver.api.mx.bean;

import com.alesharik.webserver.api.TestUtils;
import org.junit.Test;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import static org.junit.Assert.*;

public class MXBeanManagerTest {
    @Test
    public void registerUnregisterMXBean() throws Exception {
        assertFalse(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.mx.bean.test:asd=1")));

        MXBeanManager.registerMXBean(new MXBeanImpl(1), "com.alesharik.webserver.api.mx.bean.test:asd=1");
        assertTrue(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.mx.bean.test:asd=1")));
        int t1 = (int) ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName("com.alesharik.webserver.api.mx.bean.test:asd=1"), "Test");
        assertEquals(1, t1);

        MXBeanManager.registerMXBean(new MXBeanImpl(2), "com.alesharik.webserver.api.mx.bean.test:asd=1");
        assertTrue(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.mx.bean.test:asd=1")));
        int t2 = (int) ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName("com.alesharik.webserver.api.mx.bean.test:asd=1"), "Test");
        assertEquals(2, t2);

        MXBeanManager.unregisterMXBean("com.alesharik.webserver.api.mx.bean.test:asd=1");
        assertFalse(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.mx.bean.test:asd=1")));
    }

    @Test
    public void registerMXBeanWithInterface() throws Exception {
        assertFalse(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.mx.bean.test:asd=1")));

        MXBeanManager.registerMXBean(new MXBeanImpl(1), MXBean.class, "com.alesharik.webserver.api.mx.bean.test:asd=1");
        assertTrue(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.mx.bean.test:asd=1")));
        int t1 = (int) ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName("com.alesharik.webserver.api.mx.bean.test:asd=1"), "Test");
        assertEquals(1, t1);

        MXBeanManager.registerMXBean(new MXBeanImpl(2), MXBean.class, "com.alesharik.webserver.api.mx.bean.test:asd=1");
        assertTrue(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.mx.bean.test:asd=1")));
        int t2 = (int) ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName("com.alesharik.webserver.api.mx.bean.test:asd=1"), "Test");
        assertEquals(2, t2);

        MXBeanManager.unregisterMXBean("com.alesharik.webserver.api.mx.bean.test:asd=1");
        assertFalse(ManagementFactory.getPlatformMBeanServer().isRegistered(new ObjectName("com.alesharik.webserver.api.mx.bean.test:asd=1")));
    }

    @Test
    public void testUtilityClass() throws Exception {
        TestUtils.assertUtilityClass(MXBeanManager.class);
    }

    public interface MXBean {
        int getTest();
    }

    public class MXBeanImpl implements MXBean {
        private final int t;

        public MXBeanImpl(int t) {
            this.t = t;
        }

        @Override
        public int getTest() {
            return t;
        }
    }
}