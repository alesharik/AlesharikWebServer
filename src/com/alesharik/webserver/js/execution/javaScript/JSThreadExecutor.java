package com.alesharik.webserver.js.execution.javaScript;

import com.alesharik.webserver.logger.Logger;

import java.util.concurrent.CopyOnWriteArrayList;

final class JSThreadExecutor {
    private static final CopyOnWriteArrayList<Thread> threads = new CopyOnWriteArrayList<>();
    private static final Thread checker = new Thread(() -> {
        try {
            while(true) {
                Thread.sleep(1000);
                threads.stream().filter(Thread::isInterrupted).forEach(threads::remove);
            }
        } catch (InterruptedException e) {
            Logger.log(e);
        }
    });

    static {
        checker.start();
    }

    private JSThreadExecutor() {
    }

    static void execute(JSThread thread) {
        Thread thread1 = new Thread(thread::run);
        thread1.start();
        threads.add(thread1);
        thread.setThread(thread1);
    }
}
