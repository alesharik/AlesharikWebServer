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

package com.alesharik.webserver.platform;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@UtilityClass
public class CoreUtils {
    private static final List<UpdateListener> listeners = new CopyOnWriteArrayList<>();
    @Getter
    private static Core[] cores = new Core[0];

    static {
        Native.staticInit();
    }

    public static int getCoreCount() {
        return cores == null ? -1 : cores.length;
    }

    public static void registerListener(@Nonnull UpdateListener listener) {
        listeners.add(listener);
    }

    public static void unregisterListener(@Nonnull UpdateListener listener) {
        listeners.remove(listener);
    }

    static native void update();

    private static void update(long[][] data) {
        for(UpdateListener listener : listeners)
            listener.onUpdateStarted();
        if(cores.length != data.length)
            cores = new Core[data.length];
        for(int i = 0; i < data.length; i++) {
            Core c = cores[i];
            if(c == null)
                cores[i] = c = new Core(i);
            long[] datum = data[i];
            c.update(datum[0], datum[1], datum[2], datum[3], datum[4], datum[5], datum[6]);
        }
        for(UpdateListener listener : listeners)
            listener.onUpdateEnded();
    }

    @RequiredArgsConstructor
    @Getter
    public static final class Core {
        private final int id;
        private long userTime;
        private long niceTime;
        private long systemTime;
        private long idleTime;
        private long iowaitTime;
        private long irqTime;
        private long softirqTime;
        private long totalTime;

        private long userLastTime;
        private long niceLastTime;
        private long systemLastTime;
        private long idleLastTime;
        private long iowaitLastTime;
        private long irqLastTime;
        private long softirqLastTime;
        private long totalLastTime;

        private long userPercent;
        private long nicePercent;
        private long systemPercent;
        private long idlePercent;
        private long iowaitPercent;
        private long irqPercent;
        private long softirqPercent;
        private long totalPercent;

        private void update(long user, long nice, long system, long idle, long iowait, long irq, long softirq) {
            moveTime();
            setTime(user, nice, system, idle, iowait, irq, softirq);
            calculatePercent();
        }

        private void setTime(long user, long nice, long system, long idle, long iowait, long irq, long softirq) {
            userTime = user;
            niceTime = nice;
            systemTime = system;
            idleTime = idle;
            iowaitTime = iowait;
            irqTime = irq;
            softirqTime = softirq;
            totalTime = user + nice + system + idle + iowait + irq + softirq;
        }

        private void moveTime() {
            userLastTime = userTime;
            niceLastTime = niceTime;
            systemLastTime = systemTime;
            idleLastTime = idleTime;
            iowaitLastTime = iowaitTime;
            irqLastTime = irqTime;
            softirqLastTime = softirqTime;
            totalLastTime = totalTime;
        }

        private void calculatePercent() {
            userPercent = wrapSubtract(userTime, userLastTime);
            nicePercent = wrapSubtract(niceTime, niceLastTime);
            systemPercent = wrapSubtract(systemTime, systemLastTime);
            idlePercent = wrapSubtract(idleTime, idleLastTime);
            iowaitPercent = wrapSubtract(iowaitTime, iowaitLastTime);
            irqPercent = wrapSubtract(irqTime, irqLastTime);
            softirqPercent = wrapSubtract(softirqTime, softirqLastTime);
            totalPercent = wrapSubtract(totalTime, totalLastTime);
        }

        private static long wrapSubtract(long a, long b) {
            return (a > b) ? a - b : 0;
        }
    }

    public interface UpdateListener {
        void onUpdateStarted();

        void onUpdateEnded();
    }
}
