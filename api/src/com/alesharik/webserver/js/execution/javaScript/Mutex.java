package com.alesharik.webserver.js.execution.javaScript;

/**
 * This class used ONLY in js code.
 * This class is representation of a mutex. Used for synchronize threads in JavaScript.
 * The docs located in this folder in file named <code>Mutex.js</code>
 */
public final class Mutex {
    private boolean isLocked;
    private Thread owner;

    public Mutex() {
        isLocked = false;
    }

    public synchronized void lock() {
        if(isLocked && Thread.currentThread().equals(owner)) {
            throw new IllegalMonitorStateException();
        }
        do {
            if(isLocked) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    //Ok :(
                }
            } else {
                isLocked = true;
                owner = Thread.currentThread();
            }
        } while(Thread.currentThread() != owner);
    }

    public synchronized void unlock() {
        if(Thread.currentThread() != owner) {
            throw new IllegalMonitorStateException();
        } else {
            owner = null;
            isLocked = false;
            notify();
        }
    }

    public boolean isOwned() {
        return Thread.currentThread().equals(owner);
    }

    public boolean isLocked() {
        return isLocked;
    }
}
