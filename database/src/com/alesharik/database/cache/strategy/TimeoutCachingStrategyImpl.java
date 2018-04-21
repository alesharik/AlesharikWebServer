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

package com.alesharik.database.cache.strategy;

import lombok.RequiredArgsConstructor;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
final class TimeoutCachingStrategyImpl implements TimeoutCachingStrategy {
    private final ScheduledExecutorService timer;
    private final Duration creation;
    private final Duration update;
    private final Duration activate;
    private final boolean updateIsActivate;
    private final boolean ignoreReset;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") //Idea bug? It will be updated in reset method
    private final List<ScheduledFuture<?>> creationTasks = new CopyOnWriteArrayList<>();
    private final Map<Object, ScheduledFuture<?>> updateTasks = new WeakHashMap<>();
    private final Map<Object, ScheduledFuture<?>> activateTasks = new WeakHashMap<>();
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private final ReentrantLock createLock = new ReentrantLock();
    private final ReentrantLock updateLock = new ReentrantLock();
    private final ReentrantLock activateLock = new ReentrantLock();

    @Override
    public void created(Object o) {
        if(creation != null) {
            createLock.lock();
            try {
                AtomicReference<ScheduledFuture<?>> future = new AtomicReference<>();
                WeakReference<Object> ref = new WeakReference<>(o);
                future.set(timer.schedule(() -> {
                    Object o1 = ref.get();
                    if(ref.isEnqueued())
                        return;
                    for(Listener listener : listeners)
                        try {
                            listener.timeout(o1);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    creationTasks.remove(future.get());
                }, creation.toMillis(), TimeUnit.MILLISECONDS));
                creationTasks.add(future.get());
            } finally {
                createLock.unlock();
            }
        }
    }

    @Override
    public void updated(Object o) {
        if(updateIsActivate) {
            activate(o);
            return;
        }

        if(update != null) {
            updateLock.lock();
            try {
                ScheduledFuture<?> scheduledFuture = updateTasks.get(o);
                if(scheduledFuture != null)
                    scheduledFuture.cancel(true);
                WeakReference<Object> ref = new WeakReference<>(o);
                AtomicReference<ScheduledFuture<?>> future = new AtomicReference<>();
                future.set(timer.schedule(() -> {
                    Object o1 = ref.get();
                    if(ref.isEnqueued())
                        return;
                    for(Listener listener : listeners)
                        try {
                            listener.timeout(o1);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    creationTasks.remove(future.get());
                }, update.toMillis(), TimeUnit.MILLISECONDS));
                updateTasks.put(o, future.get());
            } finally {
                updateLock.unlock();
            }
        }
    }

    @Override
    public void activate(Object o) {
        if(activate != null) {
            activateLock.lock();
            try {
                ScheduledFuture<?> scheduledFuture = activateTasks.get(o);
                if(scheduledFuture != null)
                    scheduledFuture.cancel(true);
                AtomicReference<ScheduledFuture<?>> future = new AtomicReference<>();
                WeakReference<Object> ref = new WeakReference<>(o);
                future.set(timer.schedule(() -> {
                    Object o1 = ref.get();
                    if(ref.isEnqueued())
                        return;
                    for(Listener listener : listeners)
                        try {
                            listener.timeout(o1);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    creationTasks.remove(future.get());
                }, activate.toMillis(), TimeUnit.MILLISECONDS));
                activateTasks.put(o, future.get());
            } finally {
                activateLock.unlock();
            }
        }
    }

    @Override
    public void reset() {
        if(ignoreReset)
            return;


        try {
            activateLock.lock();
            activateTasks.entrySet().removeIf(scheduledFuture -> scheduledFuture.getValue().isDone() || scheduledFuture.getValue().cancel(true));
            try {
                updateLock.lock();
                updateTasks.entrySet().removeIf(scheduledFuture -> scheduledFuture.getValue().isDone() || scheduledFuture.getValue().cancel(true));
                try {
                    createLock.lock();
                    creationTasks.removeIf(scheduledFuture -> scheduledFuture.isDone() || scheduledFuture.cancel(true));
                } finally {
                    createLock.unlock();
                }
            } finally {
                updateLock.unlock();
            }
        } finally {
            activateLock.unlock();
        }
    }

    @Override
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    static final class BuilderImpl implements TimeoutCachingStrategy.Builder {
        private ScheduledExecutorService timer = CachingStrategyBuilder.EXECUTOR_SERVICE;
        private Duration creation;
        private Duration update;
        private Duration activate;
        private boolean ignoreReset = false;
        private boolean updateIsActivate = true;

        @Override
        public Builder withTimer(ScheduledExecutorService executorService) {
            timer = executorService;
            return this;
        }

        @Override
        public Builder withCreationTimeout(Duration timeout) {
            creation = timeout;
            return this;
        }

        @Override
        public Builder withUpdateTimeout(Duration timeout) {
            update = timeout;
            return this;
        }

        @Override
        public Builder perceiveUpdateAsActivate(boolean perceive) {
            this.updateIsActivate = perceive;
            return this;
        }

        @Override
        public Builder withActivateTimeout(Duration duration) {
            this.activate = duration;
            return this;
        }

        @Override
        public Builder ignoreReset() {
            ignoreReset = true;
            return this;
        }

        @Override
        public TimeoutCachingStrategy build() {
            return new TimeoutCachingStrategyImpl(timer, creation, update, activate, updateIsActivate, ignoreReset);
        }
    }
}
