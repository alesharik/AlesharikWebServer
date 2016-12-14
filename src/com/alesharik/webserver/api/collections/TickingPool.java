package com.alesharik.webserver.api.collections;

import one.nio.lock.RWLock;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class used by {@link LiveHashMap} and {@link ConcurrentLiveArrayList} for "tick".
 * The class contains all timers for map and list.
 */
@ThreadSafe
final class TickingPool {
    private static final RWLock rwLock = new RWLock();
    private static final ConcurrentHashMap<Long, Timer> timers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, CopyOnWriteArrayList<WeakReference<?>>> collections = new ConcurrentHashMap<>();

    public static void addHashMap(LiveHashMap map, long tick) {
        if(map == null || tick < 1) {
            return;
        }
        addSynchronized(map, tick);
    }

    public static void addArrayList(ConcurrentLiveArrayList arrayList, long tick) {
        if(arrayList == null || tick < 1) {
            return;
        }
        addSynchronized(arrayList, tick);
    }

    private static void addSynchronized(Object obj, long tick) {
        rwLock.lockWrite();
        try {
            if(!timers.containsKey(tick)) {
                initTimer(tick);
            }
            CopyOnWriteArrayList<WeakReference<?>> list = collections.computeIfAbsent(tick, k -> new CopyOnWriteArrayList<>());
            list.add(new WeakReference<>(obj));
        } finally {
            rwLock.unlockWrite();
        }
    }

    public static void removeHashMap(LiveHashMap map, long tick) {
        if(map == null || tick < 1) {
            return;
        }
        rwLock.lockWrite();
        try {
            CopyOnWriteArrayList<WeakReference<?>> list = collections.get(tick);
            if(list == null) {
                return;
            }

            for(WeakReference<?> weakReference : list) {
                Object reference = weakReference.get();
                if(reference == null) {
                    continue;
                }
                if(reference.equals(map)) {
                    weakReference.clear();
                    weakReference.enqueue();
                    list.remove(weakReference);
                    break;
                }
            }
            if(list.isEmpty()) {
                timers.get(tick).cancel();
                timers.remove(tick);
                collections.remove(tick);
            }
        } finally {
            rwLock.unlockWrite();
        }
    }

    public static void removeArrayList(ConcurrentLiveArrayList arrayList, long tick) {
        if(arrayList == null || tick < 1) {
            return;
        }
        rwLock.lockWrite();
        try {
            CopyOnWriteArrayList<WeakReference<?>> list = collections.get(tick);
            if(list == null) {
                return;
            }

            for(WeakReference<?> weakReference : list) {
                Object reference = weakReference.get();
                if(reference == null) {
                    continue;
                }
                if(reference.equals(arrayList)) {
                    list.remove(weakReference);
                    weakReference.clear();
                    weakReference.enqueue();
                    break;
                }
            }
            if(list.isEmpty()) {
                timers.get(tick).cancel();
                timers.remove(tick);
                collections.remove(tick);
            }
        } finally {
            rwLock.unlockWrite();
        }
    }

    /**
     * Initiate timer which update all maps and lists
     *
     * @param tick the period of update
     */
    private static void initTimer(long tick) {
        Timer liveTimer = new Timer("LiveTimer");
        timers.put(tick, liveTimer);
        liveTimer.schedule(new TimerTask() {
            @SuppressWarnings("ConstantConditions")
            // Because IDEA don't like weakReference.get() and can't see null check in first condition
            @Override
            public void run() {
                rwLock.lockRead();
                collections.get(tick).forEach(weakReference -> {
                    Object ref = weakReference.get();
                    if(weakReference.isEnqueued() || ref == null) {
                        weakReference.enqueue();
                        weakReference.clear();
                        collections.get(tick).remove(weakReference);
                    } else {
                        if(ref instanceof ConcurrentLiveArrayList) {
                            ((ConcurrentLiveArrayList) ref).updateValues(1);
                        } else if(ref instanceof LiveHashMap) {
                            ((LiveHashMap) ref).updateMap(1);
                        }
                    }
                });
                rwLock.unlockRead();
            }
        }, 0, tick);
    }
}
