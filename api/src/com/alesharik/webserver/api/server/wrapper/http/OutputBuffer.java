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

package com.alesharik.webserver.api.server.wrapper.http;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.locks.StampedLock;

@ThreadSafe
public class OutputBuffer {
    protected final StampedLock stampedLock = new StampedLock();

    protected byte[] arr;
    protected int full;

    public OutputBuffer() {
        arr = new byte[16];
        full = 0;
    }

    public OutputBuffer(byte[] arr) {
        this.arr = arr;
    }

    public OutputBuffer(int count) {
        this.arr = new byte[count];
    }

    public void write(byte b) {
        long stamp = stampedLock.writeLock();
        try {
            checkArr(1);
            arr[full] = b;
            full++;
        } finally {
            stampedLock.unlockWrite(stamp);
        }
    }

    public void write(byte[] arr, int off, int len) {
        long stamp = stampedLock.writeLock();
        try {
            checkArr(len);
            System.arraycopy(arr, off, this.arr, full, len);
            full += len;
        } finally {
            stampedLock.unlockWrite(stamp);
        }
    }

    public void write(byte[] arr) {
        write(arr, 0, arr.length);
    }

    /**
     * Operation must be executed under lock!
     */
    protected void checkArr(int size) {
        if(full + size >= arr.length) resizeArr(Math.max(size, 16));
    }

    /**
     * Operation must be executed under lock!
     */
    protected void resizeArr(int count) {
        byte[] n = new byte[arr.length + count];
        System.arraycopy(arr, 0, n, 0, full);
        arr = n;
    }

    public byte[] toByteArray() {
        long stamp = stampedLock.tryOptimisticRead();
        byte[] cpy = new byte[full];
        System.arraycopy(arr, 0, cpy, 0, full);
        if(!stampedLock.validate(stamp)) {
            try {
                stamp = stampedLock.readLock();
                System.arraycopy(arr, 0, cpy, 0, full);
            } finally {
                stampedLock.unlockRead(stamp);
            }
        }
        return cpy;
    }
}
