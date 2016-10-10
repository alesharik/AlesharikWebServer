package com.alesharik.webserver.api.serviceThreads;

import java.util.HashMap;
import java.util.concurrent.ThreadFactory;

public final class ServiceThreads {
    private static HashMap<Thread, ServiceThreadsManager> managers = new HashMap<>();

    private ServiceThreads() {

    }

    public static ServiceThreadsManager create() {
        return create(Thread.currentThread(), Thread::new);
    }

    public static ServiceThreadsManager create(Thread thread) {
        return create(thread, Thread::new);
    }

    public static ServiceThreadsManager create(Thread thread, ThreadFactory threadFactory) {
        if(managers.containsKey(thread)) {
            return managers.get(thread);
        }
        ServiceThreadsManager serviceThreadsManager = new ServiceThreadsManager(thread, threadFactory);
        managers.put(thread, serviceThreadsManager);
        return serviceThreadsManager;
    }

    public static void destroy() {
        destroy(Thread.currentThread());
    }

    public static void destroy(Thread thread) {
        managers.remove(thread).shutdown();
    }

    public static ServiceThreadsManager get(Thread thread) {
        return managers.get(thread);
    }
}
