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

package com.alesharik.webserver.daemon.impl;

import com.alesharik.webserver.daemon.Daemon;
import com.alesharik.webserver.logger.Prefixes;
import lombok.Setter;
import org.w3c.dom.Element;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

@Prefixes("[DaemonThread]")
@Deprecated
final class DaemonThread extends Thread {
    private static final byte NOTHING_STATE = 0;
    private static final byte SHUTDOWN_STATE = 1;
    private static final byte RELOAD_STATE = 2;

    private static final AtomicIntegerFieldUpdater<DaemonThread> stateUpdater = AtomicIntegerFieldUpdater.newUpdater(DaemonThread.class, "state");
    private static final AtomicReferenceFieldUpdater<DaemonThread, Element> configUpdater = AtomicReferenceFieldUpdater.newUpdater(DaemonThread.class, Element.class, "config");

    private volatile int state = NOTHING_STATE;
    private volatile Element config = null;

    @Setter
    private volatile boolean autoRestart;

    public DaemonThread(ThreadGroup threadGroup, Daemon daemon) {
        super(threadGroup, daemon.getName());
        setContextClassLoader(DaemonClassLoader.getClassLoader(daemon));
        setDaemon(true);
        setPriority(daemon.getPriority());
        setUncaughtExceptionHandler(UncaughtExceptionHandlerImpl.INSTANCE);
    }

    public void start(Element config) {
        Daemon daemon = ((DaemonClassLoader) getContextClassLoader()).getDaemon();
        if(daemon == null) {
            System.err.println("Illegal classloader state in daemon thread " + getName() + ": no daemon found");
            return;
        }

        daemon.parseConfig(config);
        start();
    }

    @Override
    public void run() {
        System.out.println("Starting daemon " + getDaemon().getName());

        if(!startDaemon() && autoRestart)
            do {
                System.err.println("Restarting daemon in initialization stage " + getDaemon().getType());
            } while(!startDaemon());

        boolean result;
        do {
            result = runDaemon();
            int state;
            do {
                state = this.state;
                if(state == SHUTDOWN_STATE) {
                    shutdownDaemon();
                    return;
                } else if(state == RELOAD_STATE) {
                    Element element;
                    do {
                        element = this.config;
                        if(element == null)
                            break;
                        reloadDaemon(element);
                    }
                    while(!configUpdater.compareAndSet(this, element, null));
                }
            }
            while(!stateUpdater.compareAndSet(this, state, NOTHING_STATE));
        } while(autoRestart || result);
    }

    public synchronized void shutdown() {
        state = SHUTDOWN_STATE;
        getDaemon().shutdown();
    }

    private boolean startDaemon() {
        try {
            getDaemon().setup();
            return true;
        } catch (Exception e) {
            System.err.println("Exception in initialization stage!");
            e.printStackTrace();
            return false;
        }
    }

    private boolean runDaemon() {
        try {
            getDaemon().run();
            return true;
        } catch (Exception e) {
            System.err.println("Exception in running stage!");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actual daemon shutdown executes in shutdown requester thread
     */
    private void shutdownDaemon() {
        System.out.println("Stopping daemon " + getDaemon().getName());
    }

    private void reloadDaemon(Element config) {
        Daemon daemon = getDaemon();
        System.out.println("Reloading daemon " + daemon.getName());
        if(!reloadDaemon0(daemon, config) && autoRestart)
            do {
                System.err.println("Restarting daemon in reload stage " + getDaemon().getName());
            } while(!reloadDaemon0(daemon, config));
    }

    private boolean reloadDaemon0(Daemon daemon, Element config) {
        try {
            daemon.reload(config);
            return true;
        } catch (Exception e) {
            System.err.println("Exception in running stage!");
            e.printStackTrace();
            return false;
        }
    }

    private Daemon getDaemon() {
        Daemon daemon = ((DaemonClassLoader) getContextClassLoader()).getDaemon();
        if(daemon == null)
            throw new IllegalStateException("Illegal classloader state in daemon thread " + getName() + ": no daemon found");
        return daemon;
    }

    public void reload(Element element) {
        int state;
        do {
            state = stateUpdater.get(this);
            if(state == SHUTDOWN_STATE)
                return;
            config = element;
        } while(!stateUpdater.compareAndSet(this, state, RELOAD_STATE));
    }

    @Override
    public void setContextClassLoader(ClassLoader cl) {
        if(cl instanceof DaemonClassLoader)
            super.setContextClassLoader(cl);
        else
            throw new IllegalArgumentException("This thread must use only DaemonClassLoader!");
    }

    static final class UncaughtExceptionHandlerImpl implements UncaughtExceptionHandler {
        static final UncaughtExceptionHandler INSTANCE = new UncaughtExceptionHandlerImpl();

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            Daemon daemon = ((DaemonClassLoader) t.getContextClassLoader()).getDaemon();
            if(daemon == null) {
                System.err.println("Uncaught exception in daemon thread " + t.getName());
                e.printStackTrace();
            } else {
                daemon.getNamedLogger().log('[' + daemon.getType() + ']', e);
            }
        }
    }
}
