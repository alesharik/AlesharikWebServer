package com.alesharik.webserver.api;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * This list used for hold elements, who can die. Default, every second decrease live period of all elements in the
 * list. If live period < 0 then element die (remove from internal {@link java.util.HashMap}) <br>
 * This list create new {@link java.util.TimerThread}, in which live period decrease. The name of the thread contains
 * "LiveTimer"
 *
 * @param <V> object, what holds in this list
 */
public class LiveArrayList<V> {
    /**
     * Default period between ticks
     */
    private static final long DEFAULT_PERIOD = 1000L;
    private Timer timer = new Timer("LiveTimer", true);
    private long period;
    private ConcurrentHashMap<V, Long> liveArrayList = new ConcurrentHashMap<>();
    /**
     * Default TimerTask
     */
    private final TimerTask TIMERTASK = new TimerTask() {
        @Override
        public void run() {
            tick();
        }
    };

    /**
     * Init {@link LiveArrayList} with default period(1000)
     */
    public LiveArrayList() {
        initTimer(DEFAULT_PERIOD);
    }

    /**
     * Init {@link LiveArrayList} with custom period
     *
     * @param period period in milliseconds between ticks
     */
    public LiveArrayList(long period) {
        initTimer(period);
    }

    private void initTimer(long period) {
        this.period = period;
        this.timer.schedule(TIMERTASK, 0, period);
    }

    private void tick() {
        liveArrayList.forEach((v, expires) -> {
            if(expires-- <= 0) {
                liveArrayList.remove(v);
            } else {
                liveArrayList.put(v, expires);
            }
        });
    }

    public void put(V object, long livePeriod) {
        liveArrayList.put(object, livePeriod);
    }

    public boolean contains(V object) {
        return liveArrayList.containsKey(object);
    }

    public void remove(V object) {
        liveArrayList.remove(object);
    }

    public long getLivePeriod(V object) {
        return liveArrayList.get(object);
    }

    public void forEach(BiConsumer<V, Long> consumer) {
        liveArrayList.forEach(consumer);
    }

    public Iterator<Map.Entry<V, Long>> iterator() {
        return liveArrayList.entrySet().iterator();
    }

    public Enumeration<V> keys() {
        return liveArrayList.keys();
    }

    public void clear() {
        liveArrayList.clear();
    }

    public void start() {
        timer.schedule(TIMERTASK, 0, this.period);
    }

    public void stop() {
        timer.cancel();
    }

    @Override
    public String toString() {
        return "LiveArrayList:" + liveArrayList.toString();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        LiveArrayList<?> that = (LiveArrayList<?>) o;

        return ((Long) period).equals(that.period) && liveArrayList.equals(that.liveArrayList);

    }

    @Override
    public int hashCode() {
        int result = (int) (period ^ (period >>> 32));
        result = 31 * result + liveArrayList.hashCode();
        return result;
    }
}
