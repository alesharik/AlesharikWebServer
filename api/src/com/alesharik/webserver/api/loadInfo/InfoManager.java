package com.alesharik.webserver.api.loadInfo;

import com.alesharik.webserver.api.Utils;
import com.alesharik.webserver.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The {@link InfoManager} is class to provide info gathering functions.
 *
 * @see Info
 */
@Deprecated
public final class InfoManager {
    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<Info>> infoMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Info, InfoLoader> threads = new ConcurrentHashMap<>();

    private InfoManager() {
    }

    /**
     * Register new Info with specific name, but do not start it!
     * Can be more than 1 Info with one name
     *
     * @param name the name of info
     * @param info the info
     */
    public static void registerNewInfo(String name, Info info) {
        Objects.requireNonNull(info);
        Utils.requireNotNullOrEmpty(name);

        CopyOnWriteArrayList<Info> list = infoMap.computeIfAbsent(name, k -> new CopyOnWriteArrayList<>());
        list.add(info);
    }

    /**
     * Get all info with specific name
     *
     * @param name      the name
     * @param infoClass the cast class
     * @return ArrayList with all elements casted to cast class
     * @throws NoSuchElementException if nobody register info with this name
     * @throws ClassCastException     if info can't cast to passed class
     */
    public static <T extends Info> ArrayList<T> getInfo(String name, Class<T> infoClass) {
        Objects.requireNonNull(infoClass);
        Utils.requireNotNullOrEmpty(name);

        CopyOnWriteArrayList<Info> infoList = infoMap.get(name);
        if(infoList == null) {
            throw new NoSuchElementException();
        }
        return infoList.stream().map(infoClass::cast).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Return the unmodifiable list of all info with specific name
     *
     * @param name the name
     * @return unmodifiable list
     * @throws NoSuchElementException if nobody register info with this name
     */
    @SuppressWarnings("unchecked")
    public static <T extends Info> List<? extends T> getInfo(String name) {
        Utils.requireNotNullOrEmpty(name);

        CopyOnWriteArrayList<Info> infoList = infoMap.get(name);
        if(infoList == null) {
            throw new NoSuchElementException();
        }
        return Collections.unmodifiableList((List<? extends T>) infoList);
    }

    /**
     * Get the first Info form passed name
     *
     * @param name      the name
     * @param infoClass the cast class
     * @return the first info class casted to passed cast class
     * @throws NoSuchElementException if nobody register info with this name
     * @throws ClassCastException     if info can't cast to passed class
     */
    public static <T extends Info> T getFirstInfo(String name, Class<T> infoClass) {
        Objects.requireNonNull(infoClass);

        return infoClass.cast(getFirstInfo(name));
    }

    /**
     * Get the first Info form passed name
     *
     * @param name the name
     * @return the first info class
     * @throws NoSuchElementException if nobody register info with this name
     */
    @SuppressWarnings("unchecked")
    public static <T extends Info> T getFirstInfo(String name) {
        Utils.requireNotNullOrEmpty(name);

        CopyOnWriteArrayList<Info> infoList = infoMap.get(name);
        if(infoList == null) {
            throw new NoSuchElementException();
        }
        return (T) infoList.get(0);
    }

    /**
     * Unregister Info class and shutdown it's handler thread
     *
     * @param name the name of info
     * @param info the registered info
     * @throws NoSuchElementException if nobody register info with this name
     */
    public static void unregisterInfo(String name, Info info) {
        Objects.requireNonNull(info);
        Utils.requireNotNullOrEmpty(name);

        info.stopLoadingData();

        CopyOnWriteArrayList<Info> infoList = infoMap.get(name);
        if(infoList == null || !infoList.contains(info)) {
            throw new NoSuchElementException();
        }
        infoList.remove(info);
        if(infoList.size() <= 0) {
            infoMap.remove(name);
        }

        if(threads.containsKey(info)) {
            threads.remove(info);
        }
    }

    static void stopLoadingData(Info info) {
        if(threads.containsKey(info)) {
            threads.get(info).shutdown();
        }
    }

    static void startLoadingData(Info info) {
        if(!threads.containsKey(info)) {
            InfoLoader infoLoader = new InfoLoader(info);
            threads.put(info, infoLoader);
            infoLoader.start();
        } else {
            InfoLoader infoLoader = threads.get(info);
            if(!infoLoader.isRunning()) {
                infoLoader.start();
            }
        }
    }

    static void terminateInfoLoading(Info info) {
        if(threads.containsKey(info)) {
            threads.get(info).shutdown();
            threads.remove(info);
        }
    }

    /**
     * This class used for call <code>Info.loadInfo()</code> at every <code>Info.getUpdateMillis()</code> milliseconds
     */
    private static final class InfoLoader extends Thread {
        private final Info info;
        private final AtomicBoolean isRunning = new AtomicBoolean(false);

        public InfoLoader(Info info) {
            this.info = info;
        }

        @Override
        public void start() {
            isRunning.set(true);
            super.start();
        }

        @Override
        public void run() {
            try {
                while(isRunning.get()) {
                    info.loadInfo();
                    Thread.sleep(info.getUpdateMillis());
                }
            } catch (InterruptedException e) {
                Logger.log(e);
            }
        }

        public void shutdown() {
            isRunning.set(false);
        }

        public boolean isRunning() {
            return isRunning.get();
        }
    }
}
