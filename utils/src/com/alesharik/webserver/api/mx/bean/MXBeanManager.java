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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import java.lang.management.ManagementFactory;

/**
 * This class allow register/unregister MX Beans
 *
 * @see ObjectName
 * @see MBeanServer
 * @see javax.management.MBeanRegistration
 */
@UtilityClass
public class MXBeanManager {
    static {
        try {
            Object hotspotInternal = Class.forName("sun.management.HotspotInternal").newInstance();
            ManagementFactory.getPlatformMBeanServer().registerMBean(hotspotInternal, null);
        } catch (InstanceAlreadyExistsException e) {
            //Ok, no problem
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * Register MXBean with specific object name({@link ObjectName}). If bean is already registered, it will be replaced by new bean
     *
     * @param object MXBean implementation
     * @param name   MXBean object name
     */
    public static void registerMXBean(@Nonnull Object object, @Nonnull String name) {
        registerMXBean(object, null, name);
    }

    /**
     * Register MXBean with specific object name({@link ObjectName}). If bean is already registered, it will be replaced by new bean
     *
     * @param object          MXBean implementation
     * @param name            MXBean object name
     * @param mxBeanInterface MXBean implementation interface
     * @throws IllegalArgumentException if object name is malformed or object is not JMX compliant MBean
     * @throws IllegalStateException    if instance already registered and server can't unregister it
     * @throws RuntimeException         if exception has thrown in registering stage
     */
    public static <T, R extends T> void registerMXBean(@Nonnull R object, @Nullable Class<T> mxBeanInterface, @Nonnull String name) {
        try {
            StandardMBean mb = new StandardMBean(object, mxBeanInterface, true);
            MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();

            ObjectName objectName = new ObjectName(name);
            if(beanServer.isRegistered(objectName))
                beanServer.unregisterMBean(objectName);

            beanServer.registerMBean(mb, objectName);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Object name is malformed!", e);
        } catch (InstanceAlreadyExistsException e) {
            throw new IllegalStateException("Bean instance already registered!(P.S. This should never happened)", e);
        } catch (MBeanRegistrationException e) {
            throw new RuntimeException("Exception in registration!", e.getCause());
        } catch (NotCompliantMBeanException e) {
            throw new IllegalArgumentException("Object is not JMX compliant MBean");
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unregister MXBean
     *
     * @param name MXBean name
     * @throws IllegalArgumentException if object name is malformed or object is not JMX compliant MBean
     * @throws RuntimeException         if exception has thrown in registering stage
     */
    public static void unregisterMXBean(@Nonnull String name) {
        try {
            MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();

            ObjectName objectName = new ObjectName(name);
            if(beanServer.isRegistered(objectName))
                beanServer.unregisterMBean(objectName);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Object name is malformed!", e);
        } catch (MBeanRegistrationException e) {
            throw new RuntimeException("Exception in registration!", e);
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }
    }
}
