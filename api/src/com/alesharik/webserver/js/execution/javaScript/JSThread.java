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

package com.alesharik.webserver.js.execution.javaScript;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

/**
 * This class used ONLY in js code.
 * This class used for create, start and interrupt threads in JavaScript.
 * The thread documentation is in <code>Thread.js</code> class in this folder
 */
public final class JSThread {
    private final ScriptObjectMirror runnable;
    private final Object sharedStorage;
    private Thread thread;
    private boolean isInterrupted = true;

    public JSThread(ScriptObjectMirror runnable, Object sharedStorage) {
        this.runnable = runnable;
        this.sharedStorage = sharedStorage;
    }

    synchronized void setThread(Thread thread) {
        this.thread = thread;
    }

    public synchronized void start() {
        if(isInterrupted) {
            isInterrupted = false;
            JSThreadExecutor.execute(this);
        }
    }

    public void run() {
        runnable.call(this);
    }

    public synchronized void interrupt() {
        if(!isInterrupted) {
            isInterrupted = true;
            thread.interrupt();
        }
    }

    public boolean isInterrupted() {
        return isInterrupted;
    }

    public synchronized Object getSharedStorage() {
        return sharedStorage;
    }
}
