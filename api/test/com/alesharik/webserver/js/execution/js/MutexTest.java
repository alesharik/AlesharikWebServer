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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class MutexTest {
    @Test(timeout = 1000)
    public void lockUnlock() throws InterruptedException {
        List<Integer> list = new ArrayList<>();
        Mutex mutex = new Mutex();
        Thread second = new Thread(() -> {
            mutex.lock();
            list.add(2);
            mutex.unlock();
        });
        mutex.lock();
        second.start();
        list.add(1);
        mutex.unlock();
        Thread.sleep(100);
        assertArrayEquals(new Integer[]{1, 2}, list.toArray(new Integer[0]));
    }

    @Test(timeout = 1000)
    public void getters() throws InterruptedException {
        Mutex mutex = new Mutex();
        assertFalse(mutex.isLocked());
        assertFalse(mutex.isOwned());

        mutex.lock();
        assertTrue(mutex.isLocked());
        assertTrue(mutex.isOwned());

        mutex.unlock();
        Thread thread = new Thread(() -> {
            mutex.lock();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ignored) {
            }
        });
        thread.start();
        Thread.sleep(50);
        assertTrue(mutex.isLocked());
        assertFalse(mutex.isOwned());
        thread.interrupt();
    }
}