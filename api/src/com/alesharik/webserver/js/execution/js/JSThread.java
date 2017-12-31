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

import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class used ONLY in js code.
 * This class used for create, start and interrupt threads in JavaScript.
 * The thread documentation is in <code>Thread.js</code> class in this folder
 */
public final class JSThread {
    private final AbstractJSObject runnable;
    private final Object sharedStorage;

    private final AtomicBoolean interrupted = new AtomicBoolean(true);
    private Thread thread;

    public JSThread(AbstractJSObject runnable, Object sharedStorage) {
        this.runnable = runnable;
        this.sharedStorage = sharedStorage;
    }

    public JSThread(ScriptObjectMirror runnable, Object sharedStorage) {
        this.runnable = runnable;
        this.sharedStorage = sharedStorage;
    }

    synchronized void setThread(Thread thread) {
        this.thread = thread;
    }

    public synchronized void start() {
        if(interrupted.compareAndSet(true, false))
            JSThreadExecutor.execute(this);
    }

    public void run() {
        runnable.call(this);
    }

    public synchronized void interrupt() {
        if(interrupted.compareAndSet(false, true)) {
            thread.interrupt();
            JSThreadExecutor.invokeChecker();
        }
    }

    public boolean isRunning() {
        return !interrupted.get();
    }

    public synchronized Object getSharedStorage() {
        return sharedStorage;
    }

    public String getName() {
        return thread.getName();
    }

    public void setName(String name) {
        thread.setName(name);
    }

    public boolean isDaemon() {
        return thread.isDaemon();
    }

    public void setDaemon(boolean is) {
        thread.setDaemon(is);
    }
}
