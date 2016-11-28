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
