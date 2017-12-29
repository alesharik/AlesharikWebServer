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

package com.alesharik.webserver.api.cache.object;

import com.alesharik.webserver.internals.ShutdownState;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This thread refills all {@link CachedObjectFactory} with dead instances
 */
final class CachedObjectFactoryReFillerThread extends Thread {//TODO move to java.lang.ref.Cleaner in Java 9
    private static final ReferenceQueue<Object> queue = new ReferenceQueue<>();
    private static final List<Ref> refs = new CopyOnWriteArrayList<>();
    static final AtomicReference<CachedObjectFactoryReFillerThread> thread = new AtomicReference<>();

    static {
        thread.set(new CachedObjectFactoryReFillerThread());
        thread.get().start();
    }

    public CachedObjectFactoryReFillerThread() {
        setDaemon(true);
        setPriority(Thread.MIN_PRIORITY + 1);
        setName("CachedObjectFactory-ReFiller");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            while(isAlive()) {
                Ref ref = (Ref) queue.remove();
                refs.remove(ref);
                try {
                    Recyclable obj = (Recyclable) ref.get();
                    if(obj == null)
                        continue;

                    ref.factory.putInstance(obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            System.err.println("CachedObjectFactory-ReFiller was stopped!");
            if(ShutdownState.getCurrentState().isRunning()) {
                System.err.println("Restarting CachedObjectFactory-ReFiller...");
                thread.set(new CachedObjectFactoryReFillerThread());
                thread.get().start();
            }
        }
    }

    static final class Ref<T> extends WeakReference<T> {
        private final CachedObjectFactory factory;

        public Ref(T referent, CachedObjectFactory factory) {
            super(referent, CachedObjectFactoryReFillerThread.queue);
            this.factory = factory;
            refs.add(this);
        }
    }
}
