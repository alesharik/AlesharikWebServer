package com.alesharik.webserver.api.collections;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

class TickingPool {
    private static final ConcurrentHashMap<Long, Timer> timers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, CopyOnWriteArrayList<WeakReference<?>>> collections = new ConcurrentHashMap<>();

    public synchronized static void addHashMap(LiveHashMap map, long tick) {
        CopyOnWriteArrayList<WeakReference<?>> list = collections.get(tick);
        if(list == null) {
            list = new CopyOnWriteArrayList<>();
            collections.put(tick, list);
        }
        if(!timers.containsKey(tick)) {
            initTimer(tick);
        }
        list.add(new WeakReference<>(map));
    }

    public synchronized static void addArrayList(LiveArrayList arrayList, long tick) {
        if(!timers.containsKey(tick)) {
            initTimer(tick);
        }
        CopyOnWriteArrayList<WeakReference<?>> list = collections.get(tick);
        if(list == null) {
            list = new CopyOnWriteArrayList<>();
            collections.put(tick, list);
        }
        list.add(new WeakReference<>(arrayList));
    }

    public synchronized static void removeHashMap(LiveHashMap map, long tick) {
        CopyOnWriteArrayList<WeakReference<?>> list = collections.get(tick);
        for(WeakReference<?> weakReference : list) {
            Object reference = weakReference.get();
            if(reference != null && reference.equals(map)) {
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
    }

    public synchronized static void removeArrayList(LiveArrayList arrayList, long tick) {
        CopyOnWriteArrayList<WeakReference<?>> list = collections.get(tick);
        for(WeakReference<?> weakReference : list) {
            Object reference = weakReference.get();
            if(reference != null && reference.equals(arrayList)) {
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
    }

    private static void initTimer(long tick) {
        Timer liveTimer = new Timer("LiveTimer");
        timers.put(tick, liveTimer);
        liveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                collections.get(tick).forEach(weakReference -> {
                    if(weakReference.isEnqueued() || weakReference.get() == null) {
                        weakReference.enqueue();
                        weakReference.clear();
                        collections.get(tick).remove(weakReference);
                    } else {
                        if(weakReference.get() instanceof LiveArrayList) {
                            ((LiveArrayList) weakReference.get()).updateValues(1);
                        } else {
                            ((LiveHashMap) weakReference.get()).updateMap(1);
                        }
                    }
                });
            }
        }, 0, tick);
    }
}
