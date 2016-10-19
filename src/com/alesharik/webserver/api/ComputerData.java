package com.alesharik.webserver.api;

import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public final class ComputerData {
    public static final ComputerData INSTANCE = new ComputerData();

    private final Timer timer;
    private final int coreCount;
    private final long[][] coreLoad;
    private final long[] ram;

    private ComputerData() {
        this.coreCount = Utils.getCoresCount();
        this.coreLoad = new long[coreCount][7];
        this.ram = new long[6];

        timer = new Timer("ComputerDataGatherer", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for(int i = 0; i < coreCount; i++) {
                    coreLoad[i] = Utils.getCoreInfo(i);
                }
                updateRam();
            }
        }, 0, 1000);
    }

    public int getCoreCount() {
        return coreCount;
    }

    private long getCoreLoad(int core, CoreLoadType type) {
        if(core >= coreLoad.length) {
            throw new IllegalArgumentException();
        }
        Objects.requireNonNull(type);

        return coreLoad[core][type.id];
    }

    private void updateRam() {
        System.arraycopy(Utils.getRAMInfo(), 0, ram, 0, 6);
    }

    public long getRam(RamType type) {
        return ram[Objects.requireNonNull(type.id)];
    }

    public String stringify() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('{');
        stringBuilder.append("\"cpuCount\": ");
        stringBuilder.append(coreCount);
        for(int i = 0; i < coreCount; i++) {
            stringBuilder.append(", \"cpu");
            stringBuilder.append(i);
            stringBuilder.append("\": [");
            stringBuilder.append(Arrays.toString(coreLoad[i]));
            stringBuilder.append("]");
        }

        stringBuilder.append(",\"ram\": ");
        stringBuilder.append(Arrays.toString(ram));

        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    public enum RamType {
        TOTAL(0),
        FREE(1),
        SHARED(2),
        BUFFER(3),
        TOTAL_SWAP(4),
        FREE_SWAP(5);

        int id;

        RamType(int id) {
            this.id = id;
        }
    }

    public enum CoreLoadType {
        /**
         * Normal processes executing in user mode
         */
        USER(0),
        /**
         * Nice processes executing in user mode
         */
        NICE(1),
        /**
         * Processes executing in kernel mode
         */
        SYSTEM(2),
        /**
         * Twiddling thumbs
         */
        IDLE(3),
        /**
         * Waiting for I/O to complete
         */
        IOWAIT(4),
        /**
         * Servicing interrupts
         */
        IRQ(5),
        /**
         * servicing softirqs
         */
        SOFTIRQ(6);

        int id;

        CoreLoadType(int id) {
            this.id = id;
        }
    }
}
