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
