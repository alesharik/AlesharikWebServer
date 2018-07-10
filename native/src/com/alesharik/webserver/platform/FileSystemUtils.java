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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Updates every 1 minute
 */
@UtilityClass
@Getter
public class FileSystemUtils {
    static {
        Native.staticInit();
    }

    private static final List<Device> devices = new ArrayList<>();
    private static final List<UpdateListener> listeners = new CopyOnWriteArrayList<>();
    private static Device toUpdate;

    static native void update();

    private static void startUpdate() {
        for(UpdateListener listener : listeners) {
            try {
                listener.onUpdateStarted();
            } catch (Exception e) {
                System.err.println("Exception in listener!");
                e.printStackTrace();
            }
        }
    }

    private static void startUpdateDevice(String device) {
        for(Device device1 : devices) {
            if(device1.name.equals(device)) {
                toUpdate = device1;
                return;
            }
        }
        toUpdate = new Device(device);
        devices.add(toUpdate);
    }

    private static void updatePartition(String partition, String label, String type, String mnt, long maxSize, long freeSize, long maxInodes, long freeInodes, long sectorCount, int sectorSize) {
        toUpdate.update(partition, label, type, mnt, maxSize, freeSize, maxInodes, freeInodes, sectorCount, sectorSize);
    }

    private static void endUpdateDevice() {
        toUpdate.updateEnd();
        toUpdate = null;
    }

    private static void endUpdate() {
        toUpdate = null;
        for(UpdateListener listener : listeners) {
            try {
                listener.onUpdateEnded();
            } catch (Exception e) {
                System.err.println("Exception in listener!");
                e.printStackTrace();
            }
        }
    }

    @RequiredArgsConstructor
    @Getter
    public static final class Device {
        private final String name;
        private final List<Partition> partitions = new ArrayList<>();
        private final List<Partition> upd = new ArrayList<>();

        void update(String partition, String label, String type, String mnt, long maxSize, long freeSize, long maxInodes, long freeInodes, long sectorCount, int sectorSize) {
            Iterator<Partition> iterator = partitions.iterator();
            while(iterator.hasNext()) {
                Partition next = iterator.next();
                if(next.address.equals(partition)) {
                    iterator.remove();
                    next.update(label, type, mnt, maxSize, freeSize, maxInodes, freeInodes, sectorCount, sectorSize);
                    upd.add(next);
                    return;
                }
            }
            Partition part = new Partition(partition);
            part.update(label, type, mnt, maxSize, freeSize, maxInodes, freeInodes, sectorCount, sectorSize);
            upd.add(part);
        }

        void updateEnd() {
            partitions.clear();
            partitions.addAll(upd);
        }

        public boolean isRam() {
            return name.startsWith("ram");
        }

        public boolean isLoop() {
            return name.startsWith("loop");
        }

        public boolean isDiskDrive() {
            return name.startsWith("sd");
        }
    }

    @RequiredArgsConstructor
    @Getter
    public static final class Partition {
        private final String address;
        private String label;
        private String type;
        private String mountPoint;
        private long maxSize;
        private long freeSize;
        private long maxInodes;
        private long freeInodes;
        private long sectorCount;
        private int sectorSize;

        public boolean isInodePartition() {
            return maxInodes != -1;
        }

        void update(String label, String type, String mnt, long maxSize, long freeSize, long maxInodes, long freeInodes, long sectorCount, int sectorSize) {
            this.label = label;
            this.type = type;
            this.maxSize = maxSize;
            this.freeSize = freeSize;
            this.maxInodes = maxInodes;
            this.freeInodes = freeInodes;
            this.sectorCount = sectorCount;
            this.sectorSize = sectorSize;
            this.mountPoint = mnt;
        }
    }

    public interface UpdateListener {
        void onUpdateStarted();

        void onUpdateEnded();
    }
}
