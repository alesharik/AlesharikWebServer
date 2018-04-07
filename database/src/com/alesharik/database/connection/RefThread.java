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

package com.alesharik.database.connection;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class RefThread extends Thread {
    private static final ReferenceQueue<Object> queue = new ReferenceQueue<>();
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final List<Ref> refs = new ArrayList<>();

    static {
        RefThread thread = new RefThread();
        thread.start();
    }

    private RefThread() {
        setDaemon(true);
        setName("DatabaseConnectionPool-RefThread");
        setPriority(Thread.NORM_PRIORITY - 1);
    }

    public static <T> void add(T ref, Consumer<T> consumer) {
        refs.add(new Ref<>(ref, consumer));
    }

    @Override
    public void run() {
        while(!isInterrupted()) {
            try {
                Ref ref = (Ref) queue.remove();
                try {
                    //noinspection unchecked
                    ref.consumer.accept(ref.get());
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                refs.remove(ref);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private static final class Ref<T> extends WeakReference<T> {
        private final Consumer<T> consumer;

        public Ref(T referent, Consumer<T> consumer) {
            super(referent, queue);
            this.consumer = consumer;
        }
    }
}
