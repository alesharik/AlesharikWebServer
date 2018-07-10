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
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@UtilityClass
public class MemoryUtils {
    private static final List<UpdateListener> listeners = new CopyOnWriteArrayList<>();
    @Getter
    private static long totalRam;
    @Getter
    private static long freeRam;
    @Getter
    private static long sharedRam;
    @Getter
    private static long bufferRam;
    @Getter
    private static long totalSwap;
    @Getter
    private static long freeSwap;

    static {
        Native.staticInit();
    }

    static native void update();

    public static void registerListener(UpdateListener listener) {
        listeners.add(listener);
    }

    public static void unregisterListener(UpdateListener listener) {
        listeners.remove(listener);
    }

    private static void update(long totalRam, long freeRam, long sharedRam, long bufferRam, long totalSwap, long freeSwap) {
        for(UpdateListener listener : listeners)
            listener.onUpdateStarted();
        MemoryUtils.totalRam = totalRam;
        MemoryUtils.freeRam = freeRam;
        MemoryUtils.sharedRam = sharedRam;
        MemoryUtils.bufferRam = bufferRam;
        MemoryUtils.totalSwap = totalSwap;
        MemoryUtils.freeSwap = freeSwap;
        for(UpdateListener listener : listeners)
            listener.onUpdateEnded();
    }

    public interface UpdateListener {
        void onUpdateStarted();

        void onUpdateEnded();
    }
}
