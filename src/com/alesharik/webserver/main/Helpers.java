package com.alesharik.webserver.main;

import com.alesharik.webserver.logger.Logger;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.security.SecureRandom;
import java.text.NumberFormat;

public final class Helpers {
    private static final Runtime RUNTIME = Runtime.getRuntime();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private Helpers() {
    }

    public static String getProcessCpuLoad() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long upTime = runtimeMXBean.getUptime();
        double processCpuTime = operatingSystemMXBean.getSystemLoadAverage();
        StringBuilder sb = new StringBuilder();
        sb.append("Uptime: " + upTime + "\n");
        sb.append("System load average: " + processCpuTime + "\n");
        return sb.toString();
    }

    public static String getMemoryInfo() {
        NumberFormat format = NumberFormat.getInstance();

        StringBuilder sb = new StringBuilder();
        long maxMemory = RUNTIME.maxMemory();
        long allocatedMemory = RUNTIME.totalMemory();
        long freeMemory = RUNTIME.freeMemory();

        sb.append("free memory: " + format.format(freeMemory / 1024) + "\n");
        sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "\n");
        sb.append("max memory: " + format.format(maxMemory / 1024) + "\n");
        sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "\n");
        return sb.toString();
    }

    public static String getOSInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("OS: " + System.getProperty("os.name") + "\n");
        sb.append("OS version: " + System.getProperty("os.version") + "\n");
        sb.append("OS architecture: " + System.getProperty("os.arch") + "\n");
        return sb.toString();
    }

    public static String getCompInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(getOSInfo());
        sb.append("Memory:\n");
        sb.append(getMemoryInfo());
        sb.append("CPU:\n");
        sb.append("Available cores: " + RUNTIME.availableProcessors() + "\n");
        sb.append(getProcessCpuLoad());
        sb.append("Disks:\n");
        sb.append(getDiskInfo());
        return sb.toString();
    }

    public static String getDiskInfo() {
        File[] root = File.listRoots();
        StringBuilder sb = new StringBuilder();

        for(File rootFile : root) {
            sb.append("File system root: " + rootFile.getAbsolutePath() + "\n");
            sb.append("Total space(bytes): " + rootFile.getTotalSpace() + "\n");
            sb.append("Free space (bytes): " + rootFile.getFreeSpace() + "\n");
            sb.append("Usable space (bytes): " + rootFile.getUsableSpace() + "\n");
        }
        return sb.toString();
    }

    /**
     * Generate random string with fixed or random length
     *
     * @param length if length equals 0 then generate string with random length
     * @return generated string
     */
    public static String getRandomString(int length) {
        if(length <= 0) {
            length = SECURE_RANDOM.nextInt();
        }

        return RandomStringUtils.random(length, true, true);
    }

    /**
     * Get real ip of current machine. <br>
     * WARNING!If machine has no internet or checkip.amazonaws.com banned, this method will wait for response infinite
     * time
     *
     * @return string with ip or 127.0.0.1
     */
    public static String getMachineExternalIP() {
        try {
            InputStreamReader getIPStreamReader = new InputStreamReader(new URL("http://checkip.amazonaws.com").openStream());
            BufferedReader inReader = new BufferedReader(getIPStreamReader);

            String ip = inReader.readLine();

            inReader.close();
            getIPStreamReader.close();
            return ip;
        } catch (IOException e) {
            Logger.log(e);
            return "127.0.0.1";
        }
    }
}
