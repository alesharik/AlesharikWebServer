package com.alesharik.webserver.api;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class used for collect data about computer. It automatically updates every second. It use ComputerDataGatherer timer.
 */
public final class ComputerData {
    public static final ComputerData INSTANCE = new ComputerData();

    private final int coreCount;
    private final long[][] coreLoad;
    private final long[] ram;

    private ComputerData() {
        this.coreCount = Utils.getCoresCount();
        this.coreLoad = new long[coreCount][7];
        this.ram = new long[6];

        Timer timer = new Timer("ComputerDataGatherer", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for(int i = 0; i < coreCount; i++) {
                    coreLoad[i] = Utils.getCoreInfo(i);
                }
                System.arraycopy(Utils.getRAMInfo(), 0, ram, 0, 6);
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
    private long getCoreTime(int core, CoreLoadType type) {
        if(core < 0 || core >= coreLoad.length) {
            throw new IllegalArgumentException("Core id must be positive and less than online core count!");
        }

        return coreLoad[core][type.ordinal()]; //Throw NullPointerException on type.ordinal()
    }

    public long getRam(RamType type) {
        return ram[type.ordinal()]; //Throw NullPointerException on type.ordinal()
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
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('{');
        stringBuilder.append("\"cpuCount\": ");
        stringBuilder.append(coreCount);
        for(int i = 0; i < coreCount; i++) {
            stringBuilder.append(", \"cpu");
            stringBuilder.append(i);
            stringBuilder.append("\": ");
            stringBuilder.append(Arrays.toString(coreLoad[i]));
        }

        stringBuilder.append(",\"ram\": ");
        stringBuilder.append(Arrays.toString(ram));

        stringBuilder.append('}');
        return stringBuilder.toString();
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
        SOFTIRQ;
    }
}