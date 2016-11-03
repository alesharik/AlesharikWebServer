package com.alesharik.webserver.api.serviceThreads;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class ServiceThreadsManagerFactoryTest {
    @Test
    public void newInstance() throws Exception {
        ServiceThreadsManager manager = ServiceThreadsManagerFactory.INSTANCE.newInstance();
        assertNotNull(manager);

        AtomicBoolean isSame = new AtomicBoolean(true);
        Thread thread = new Thread(() -> isSame.set(manager.equals(ServiceThreadsManagerFactory.INSTANCE.newInstance())));
        thread.start();
        thread.join();
        assertFalse(isSame.get());

        assertEquals(manager, ServiceThreadsManagerFactory.INSTANCE.newInstance());
    }
}