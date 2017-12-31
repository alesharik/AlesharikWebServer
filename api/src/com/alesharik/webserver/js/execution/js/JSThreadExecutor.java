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

package com.alesharik.webserver.js.execution.js;

import com.alesharik.webserver.logger.Prefixes;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.util.concurrent.CopyOnWriteArrayList;

@UtilityClass
final class JSThreadExecutor {//TODO MXBean
    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("JavaScript");
    private static final Object notifyLock = new Object();
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")//IDEA bug
    static final CopyOnWriteArrayList<Thread> threads = new CopyOnWriteArrayList<>();
    private static final CheckerThread checker = new CheckerThread();

    static {
        checker.start();
    }

    static void execute(@Nonnull JSThread t) {
        Thread thread = new Thread(THREAD_GROUP, t::run);
        threads.add(thread);
        t.setThread(thread);
        thread.start();
    }

    static void invokeChecker() {
        synchronized (notifyLock) {
            notifyLock.notifyAll();
        }
    }

    @Prefixes({"[JavaScript]", "[Thread]", "[Checker]"})
    static final class CheckerThread extends Thread {
        public CheckerThread() {
            super(THREAD_GROUP, "JSThread-Checker");
            setDaemon(true);
            setPriority(MIN_PRIORITY + 2);
        }

        @Override
        public void run() {
            System.out.println("JSThread checker successfully started!");
            synchronized (notifyLock) {
                try {
                    notifyLock.wait();
                } catch (InterruptedException e) {
                    System.err.println("JSThread checker thread is interrupted!");
                    return;
                }
            }
            threads.removeIf(Thread::isInterrupted);
        }
    }
}
