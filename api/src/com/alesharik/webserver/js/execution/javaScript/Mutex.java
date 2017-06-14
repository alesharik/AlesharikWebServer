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
