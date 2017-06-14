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

import lombok.experimental.UtilityClass;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import java.lang.management.ManagementFactory;

@UtilityClass
public class MXBeanManager {
    static {
        try {
            Object hotspotInternal = Class.forName("sun.management.HotspotInternal").newInstance();
            ManagementFactory.getPlatformMBeanServer().registerMBean(hotspotInternal, null);
        } catch (InstanceAlreadyExistsException e) {
            System.err.println("Instance of sun.management.HotspotInternal class already exists!");
        } catch (Exception e) {
            throw new Error(e);
        }

    }

    public static void registerMXBean(Object object, String name) {
        registerMXBean(object, null, name);
    }

    public static <T> void registerMXBean(T object, Class<T> mxBeanInterface, String name) {
        try {
            StandardMBean mb = new StandardMBean(object, mxBeanInterface, true);
            MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();

            ObjectName objectName = new ObjectName(name);
            if(beanServer.isRegistered(objectName)) {
                beanServer.unregisterMBean(objectName);
            }

            beanServer.registerMBean(mb, objectName);
        } catch (InstanceAlreadyExistsException | InstanceNotFoundException | MBeanRegistrationException
                | NotCompliantMBeanException | MalformedObjectNameException e) {
            e.printStackTrace();
        }
    }

    public static void unregisterMXBean(String name) {
        try {
            MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();

            ObjectName objectName = new ObjectName(name);
            if(beanServer.isRegistered(objectName)) {
                beanServer.unregisterMBean(objectName);
            }

        } catch (InstanceNotFoundException | MBeanRegistrationException | MalformedObjectNameException e) {
            e.printStackTrace();
        }
    }
}
