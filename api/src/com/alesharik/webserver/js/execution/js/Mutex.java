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

import java.util.concurrent.locks.ReentrantLock;

/**
 * This class used ONLY in js code.
 * This class is representation of a mutex. Used for synchronize threads in JavaScript.
 * The docs located in this folder in file named <code>Mutex.js</code>
 */
public final class Mutex {
    private final ReentrantLock lock;

    public Mutex() {
        lock = new ReentrantLock();
    }

    public void lock() {
        lock.lock();
    }

    public synchronized void unlock() {
        lock.unlock();
    }

    public boolean isOwned() {
        return lock.isHeldByCurrentThread();
    }

    public boolean isLocked() {
        return lock.isLocked();
    }
}
