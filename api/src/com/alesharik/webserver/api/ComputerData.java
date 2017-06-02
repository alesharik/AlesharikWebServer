package com.alesharik.webserver.api;

import com.alesharik.webserver.logger.Logger;
import one.nio.lock.RWLock;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class used for collect data about computer. It automatically updates every second. It use ComputerDataGatherer timer.
 */
public final class ComputerData {
    public static final ComputerData INSTANCE = new ComputerData();

    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private static final boolean isThreadCPUCollectionEnabled = threadMXBean.isThreadCpuTimeSupported() && threadMXBean.isThreadCpuTimeEnabled();

    private static final List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();

    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private static final List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();

    private static final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    private static final ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();

    private static final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

    private final int coreCount;
    private final long[][] coreLoad;
    private final long[] ram;
    private final RWLock lock;

    private ComputerData() {
        this.lock = new RWLock();
        this.coreCount = Utils.getCoresCount();
        this.coreLoad = new long[coreCount][8];
        this.ram = new long[6];

        Timer timer = new Timer("ComputerDataGatherer", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    lock.lockWrite();
                    for(int i = 0; i < coreCount; i++) {
                        long[] coreInfo = Utils.getCoreInfo(i);
                        System.arraycopy(coreInfo, 0, coreLoad[i], 0, 7);
                        long sum = coreInfo[0] + coreInfo[1] + coreInfo[2] + coreInfo[3] + coreInfo[4] + coreInfo[5] + coreInfo[6];
                        coreLoad[i][7] = sum;
                    }
                    System.arraycopy(Utils.getRAMInfo(), 0, ram, 0, 6);
                } finally {
                    lock.unlockWrite();
                }
            }
        }, 0, 1000);
    }

    /**
     * Return number of online cores(not processors!) in system
     */
    public int getCoreCount() {
        return coreCount;
    }

    /**
     * @param core number of core(min = 0, max = count - 1)
     * @param type load type
     * @return core time
     */
    public long getCoreTime(int core, @Nonnull CoreLoadType type) {
        if(core < 0 || core >= coreLoad.length) {
            throw new IllegalArgumentException("Core id must be positive and less than online core count!");
        }

        try {
            lock.lockRead();
            return coreLoad[core][type.ordinal()];
        } finally {
            lock.unlockRead();
        }
    }

    public long getRam(@Nonnull RamType type) {
        try {
            lock.lockRead();
            return ram[type.ordinal()];
        } finally {
            lock.unlockRead();
        }
    }

    /**
     * Return JSON string, what contains all data.
     * String: <code>
     * {
     * cpuCount: (cpuCount),
     * cpu0: [(cpuData)],
     * cpu1: [(cpuData)],
     * ...
     * cpu(cpuCount - 1): [(cpuData)],
     * ram: [(ram)]
     * }
     * </code>
     */
    public String stringify() {
        try {
            lock.lockRead();
            StringBuilder stringBuilder = new StringBuilder(1000);
            stringBuilder.append('{');

            stringBuilder.append("\"processorCount\": ");
            stringBuilder.append(operatingSystemMXBean.getAvailableProcessors());

            stringBuilder.append(", \"cpuCount\": ");
            stringBuilder.append(coreCount);
            for(int i = 0; i < coreCount; i++) {
                stringBuilder.append(", \"cpu");
                stringBuilder.append(i);
                stringBuilder.append("\": ");
                stringBuilder.append(Arrays.toString(coreLoad[i]));
            }

            stringBuilder.append(",\"ram\": ");
            stringBuilder.append(Arrays.toString(ram));

            stringBuilder.append(",\"partitions\": ");

            Utils.Partition[] computerPartitions = Utils.getComputerPartitions();
            stringBuilder.append('[');
            for(int i = 0; i < computerPartitions.length; i++) {
                stringBuilder.append(serializePartition(computerPartitions[i]));
                if(i == computerPartitions.length - 1) {
                    stringBuilder.append(']');
                } else {
                    stringBuilder.append(',');
                }
            }

            long[] threadIds = threadMXBean.getAllThreadIds();

            stringBuilder.append(", \"java\": {");

            stringBuilder.append("\"uptime\": ");
            stringBuilder.append(runtimeMXBean.getUptime());

            stringBuilder.append(", \"cpuUsage\": {");
            stringBuilder.append("\"isSupported\": ");
            stringBuilder.append(isThreadCPUCollectionEnabled);
            if(isThreadCPUCollectionEnabled) {
                stringBuilder.append(", \"count\": ");
                stringBuilder.append(threadIds.length);
                stringBuilder.append(", \"data\": [");
                for(int i = 0; i < threadIds.length; i++) {
                    stringBuilder.append(threadMXBean.getThreadCpuTime(threadIds[i]));
                    if(i == threadIds.length - 1) {
                        stringBuilder.append(']');
                    } else {
                        stringBuilder.append(',');
                    }
                }
            }
            stringBuilder.append('}');

            stringBuilder.append(", \"gc\": [");
            for(int i = 0; i < garbageCollectorMXBeans.size(); i++) {
                stringBuilder.append(serializeGCMXBean(garbageCollectorMXBeans.get(i)));
                if(i == garbageCollectorMXBeans.size() - 1) {
                    stringBuilder.append(']');
                } else {
                    stringBuilder.append(',');
                }
            }

            stringBuilder.append(", \"memory\": {");
            stringBuilder.append("\"heap\": ");
            stringBuilder.append(serializeMemoryUsage(memoryMXBean.getHeapMemoryUsage()));

            stringBuilder.append(", \"nonHeap\": ");
            stringBuilder.append(serializeMemoryUsage(memoryMXBean.getNonHeapMemoryUsage()));

            stringBuilder.append(", \"objectsPendingFinalizationCount\": ");
            stringBuilder.append(memoryMXBean.getObjectPendingFinalizationCount());

            stringBuilder.append(", \"isVerbose\": ");
            stringBuilder.append(memoryMXBean.isVerbose());

            stringBuilder.append(", \"memoryPools\": [");
            for(int i = 0; i < memoryPoolMXBeans.size(); i++) {
                stringBuilder.append(serializeMemoryPoolMXBean(memoryPoolMXBeans.get(i)));
                if(i == memoryPoolMXBeans.size() - 1) {
                    stringBuilder.append(']');
                } else {
                    stringBuilder.append(',');
                }
            }
            stringBuilder.append('}');

            stringBuilder.append(", \"classes\": {");
            stringBuilder.append("\"isVerbose\": ");
            stringBuilder.append(classLoadingMXBean.isVerbose());

            stringBuilder.append(", \"loadClasses\": ");
            stringBuilder.append(classLoadingMXBean.getLoadedClassCount());

            stringBuilder.append(", \"totalClasses\": ");
            stringBuilder.append(classLoadingMXBean.getTotalLoadedClassCount());

            stringBuilder.append(", \"unloadedClasses\": ");
            stringBuilder.append(classLoadingMXBean.getUnloadedClassCount());
            stringBuilder.append('}');

            stringBuilder.append(", \"threads\": {");
            stringBuilder.append("\"online\": ");
            stringBuilder.append(threadMXBean.getThreadCount());

            stringBuilder.append(", \"daemon\": ");
            stringBuilder.append(threadMXBean.getDaemonThreadCount());

            stringBuilder.append(", \"peak\": ");
            stringBuilder.append(threadMXBean.getPeakThreadCount());

            stringBuilder.append(", \"threads\": [");
            for(int i = 0; i < threadIds.length; i++) {
                stringBuilder.append(serializeThreadInfo(threadMXBean.getThreadInfo(threadIds[i])));
                if(i == threadIds.length - 1) {
                    stringBuilder.append(']');
                } else {
                    stringBuilder.append(',');
                }
            }
            stringBuilder.append('}');

            stringBuilder.append('}');

            stringBuilder.append('}');
            return stringBuilder.toString();
        } catch (IOException e) {
            Logger.log(e);
            return "";
        } finally {
            lock.unlockRead();
        }
    }

    //int - 10
    private String serializeThreadInfo(ThreadInfo threadInfo) {
        return "{" +
                "\"id\": " +
                threadInfo.getThreadId() +
                ", \"name\": \"" +
                threadInfo.getThreadName() +
                '\"' +
                ", \"state\": " +
                threadInfo.getThreadState().ordinal() +
                ", \"blockedTime\": " +
                threadInfo.getBlockedTime() +
                ", \"blockedCount\": " +
                threadInfo.getBlockedCount() +
                ", \"waitTime\": " +
                threadInfo.getWaitedTime() +
                ", \"waitCount\": " +
                threadInfo.getWaitedCount() +
                ", \"isInNative\": " +
                threadInfo.isInNative() +
                '}';
    }

    private String serializeGCMXBean(GarbageCollectorMXBean garbageCollectorMXBean) {
        return "{" +
                "\"gcTime\": " +
                garbageCollectorMXBean.getCollectionTime() +
                ", \"gcCount\": " +
                garbageCollectorMXBean.getCollectionCount() +
                ", \"isValid\": " +
                garbageCollectorMXBean.isValid() +
                ", \"name\": \"" +
                garbageCollectorMXBean.getName() +
                '\"' +
                '}';
    }

    //123 chars
    private String serializeMemoryUsage(MemoryUsage memoryUsage) {
        return "{" +
                "\"committed\": " +
                memoryUsage.getCommitted() +
                ", \"init\": " +
                memoryUsage.getInit() +
                ", \"used\": " +
                memoryUsage.getUsed() +
                ", \"max\": " +
                memoryUsage.getMax() +
                '}';
    }

    private String serializeMemoryPoolMXBean(MemoryPoolMXBean memoryPoolMXBean) {
        boolean isCollectionUsageThresholdSupported = memoryPoolMXBean.isCollectionUsageThresholdSupported();
        boolean isUsageThresholdSupported = memoryPoolMXBean.isUsageThresholdSupported();

        StringBuilder stringBuilder = new StringBuilder(745);

        stringBuilder.append('{');

        stringBuilder.append("\"name\": \"");
        stringBuilder.append(memoryPoolMXBean.getName());
        stringBuilder.append('\"');

        stringBuilder.append(", \"isValid\": ");
        stringBuilder.append(memoryPoolMXBean.isValid());

        stringBuilder.append(", \"usage\": ");
        stringBuilder.append(serializeMemoryUsage(memoryPoolMXBean.getUsage()));

        stringBuilder.append(", \"peakUsage\": ");
        stringBuilder.append(serializeMemoryUsage(memoryPoolMXBean.getPeakUsage()));

        stringBuilder.append(", \"isUsableThresholdSupported\": ");
        stringBuilder.append(isUsageThresholdSupported);
        if(isUsageThresholdSupported) {
            stringBuilder.append(", \"usableThreshold\": ");
            stringBuilder.append(memoryPoolMXBean.getUsageThreshold());

            stringBuilder.append(", \"usableThresholdCount\": ");
            stringBuilder.append(memoryPoolMXBean.getUsageThresholdCount());
        }
        stringBuilder.append(", \"isCollectionUsageThresholdSupported\": ");
        stringBuilder.append(isCollectionUsageThresholdSupported);
        if(isCollectionUsageThresholdSupported) {
            stringBuilder.append(", \"collectionUsableThreshold\": ");
            stringBuilder.append(memoryPoolMXBean.getCollectionUsageThreshold());

            stringBuilder.append(", \"getCollectionUsageThresholdCount\": ");
            stringBuilder.append(memoryPoolMXBean.getCollectionUsageThresholdCount());
        }

        if(memoryPoolMXBean.getCollectionUsage() != null) {
            stringBuilder.append(", \"collectionUsage\": ");
            stringBuilder.append(serializeMemoryUsage(memoryPoolMXBean.getCollectionUsage()));
        }

        stringBuilder.append('}');

        return stringBuilder.toString();
    }

    //String - 20 chars
    //long - 20 chars
    private String serializePartition(Utils.Partition partition) {
        return "{\"name\": \"" +
                partition.getName() +
                "\", \"addr\": \"" +
                partition.getAddress() +
                "\", \"type\": \"" +
                partition.getType() +
                "\", \"max\": " +
                partition.getMax() +
                ", \"free\": " +
                partition.getFree() +
                ", \"inodes\": " +
                partition.getInodeMax() +
                ", \"inodesFree\": " +
                partition.getInodeFree() +
                '}';
    }

    public enum RamType {
        TOTAL,
        FREE,
        SHARED,
        BUFFER,
        TOTAL_SWAP,
        FREE_SWAP
    }

    public enum CoreLoadType {
        /**
         * Normal processes executing in user mode
         */
        USER,
        /**
         * Nice processes executing in user mode
         */
        NICE,
        /**
         * Processes executing in kernel mode
         */
        SYSTEM,
        /**
         * Twiddling thumbs
         */
        IDLE,
        /**
         * Waiting for I/O to complete
         */
        IOWAIT,
        /**
         * Servicing interrupts
         */
        IRQ,
        /**
         * Servicing softirqs
         */
        SOFTIRQ,
        /**
         * All times
         */
        ALL
    }
}
