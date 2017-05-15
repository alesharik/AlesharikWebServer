package com.alesharik.webserver.api;

import com.alesharik.webserver.logger.Logger;
import one.nio.util.ByteArrayBuilder;
import one.nio.util.Hex;
import org.apache.commons.lang3.RandomStringUtils;

import javax.annotation.concurrent.Immutable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.zip.CRC32;

public final class Utils {
    private static final Utils INSTANCE = new Utils();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static boolean isSupportedOs() {
        return System.getProperty("os.name").toLowerCase().contains("linux") &&
                System.getProperty("os.arch").contains("64");
    }

    private Utils() {
    }

    static {
        if(isSupportedOs()) {
            try {
                InputStream in = Utils.class.getResourceAsStream("/libalesharikwebserver.so");

                if(in != null) {
                    ByteArrayBuilder libData = readStream(in);
                    in.close();

                    String tmpDir = System.getProperty("java.io.tmpdir", "/tmp");
                    File dll = new File(tmpDir, "libalesharikwebserver." + crc32(libData) + ".so");
                    if(!dll.exists()) {
                        try (OutputStream out = new FileOutputStream(dll)) {
                            out.write(libData.buffer(), 0, libData.length());
                            out.close();
                        }
                    }

                    String libraryPath = dll.getAbsolutePath();
                    System.load(libraryPath);
                } else {
                    System.out.println("Cannot find native IO library");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private native String getExternalIp0();

    private static ByteArrayBuilder readStream(InputStream in) throws IOException {
        byte[] buffer = new byte[64000];
        ByteArrayBuilder builder = new ByteArrayBuilder(buffer.length);
        for(int bytes; (bytes = in.read(buffer)) > 0; ) {
            builder.append(buffer, 0, bytes);
        }
        return builder;
    }

    private static String crc32(ByteArrayBuilder builder) {
        CRC32 crc32 = new CRC32();
        crc32.update(builder.buffer(), 0, builder.length());
        return Hex.toHex((int) crc32.getValue());
    }

    /**
     * This function return external ip of first work network(network, in with program can connect to google server).
     * You can getIpForMicroservice this ip by <code>ping www.google.com</code> in first line. If function can't receive response as long
     * of 30 seconds, it will return 127.0.0.1
     *
     * @return dot-splitted ip address
     */
    public static String getExternalIp() {
        if(isSupportedOs()) {
            return INSTANCE.getExternalIp0();
        } else {
            return "127.0.0.1";
        }
    }

    /**
     * Check if string is null or empty
     *
     * @throws NullPointerException     if string is <code>null</code>
     * @throws IllegalArgumentException if string is empty
     */
    public static void requireNotNullOrEmpty(String string) {
        Objects.requireNonNull(string);
        if(string.isEmpty()) {
            throw new IllegalArgumentException();
        }
    }

    public static String sliceString(String[] arr, int count) {
        if(count > arr.length)
            throw new ArrayIndexOutOfBoundsException();
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < count; i++) {
            stringBuilder.append(arr[i]);
        }
        return stringBuilder.toString();
    }

    public static String sliceString(String[] arr, int count, boolean reverse) {
        if(count > arr.length)
            throw new ArrayIndexOutOfBoundsException();
        if(!reverse)
            return sliceString(arr, count);

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = arr.length - 1; i < (arr.length - count); i--) {
            stringBuilder.append(arr[i]);
        }
        return stringBuilder.toString();
    }

    public static int getCoresCount() {
        if(isSupportedOs()) {
            return getCoresCount0();
        } else {
            return 0;
        }
    }

    private static native int getCoresCount0();

    private static native long[] getCoreInfo0(int core);

    private static native long[] getRAMInfo0();

    static long[] getCoreInfo(int core) {
        if(isSupportedOs()) {
            return getCoreInfo0(core);
        } else {
            return new long[7];
        }
    }

    static long[] getRAMInfo() {
        if(isSupportedOs()) {
            return getRAMInfo0();
        } else {
            return new long[6];
        }
    }

    public static byte[] getURLAsByteArray(URL url) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InputStream is;
        try {
            is = url.openStream();
            byte[] byteChunk = new byte[4096];
            int n;

            while((n = is.read(byteChunk)) > 0) {
                byteArrayOutputStream.write(byteChunk, 0, n);
            }
            is.close();
        } catch (IOException e) {
            Logger.log(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Check if string null or empty
     *
     * @param str string to check
     * @return good string
     */
    public static String notNullAndEmpty(String str) {
        Objects.requireNonNull(str);
        if(str.isEmpty()) {
            throw new IllegalArgumentException("String can't be empty");
        }
        return str;
    }

    private static native Partition[] getPartitions() throws IOException;

    /**
     * Return ALL(mounted and not) partitions, which is currently installed
     *
     * @return Partition
     * @throws IOException then bad things happens
     */
    public static Partition[] getComputerPartitions() throws IOException {
        if(isSupportedOs()) {
            return getPartitions();
        } else {
            return new Partition[0];
        }
    }

    @Immutable
    public static final class Partition {
        private final String name;
        private final String address;
        private final String type;
        private final long max;
        private final long free;
        private final long inode;
        private final long inodeFree;

        Partition(String address, String name, String type, long max, long free, long inode, long inodeFree) {
            this.name = name;
            this.address = address;
            this.type = type;
            this.max = max;
            this.free = free;
            this.inode = inode;
            this.inodeFree = inodeFree;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public String getType() {
            return type;
        }

        public long getMax() {
            return max;
        }

        public long getFree() {
            return free;
        }

        public long getInodeMax() {
            return inode;
        }

        public long getInodeFree() {
            return inodeFree;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(!(o instanceof Partition)) return false;

            Partition partition = (Partition) o;

            if(getMax() != partition.getMax()) return false;
            if(getFree() != partition.getFree()) return false;
            if(inode != partition.inode) return false;
            if(getInodeFree() != partition.getInodeFree()) return false;
            if(getName() != null ? !getName().equals(partition.getName()) : partition.getName() != null) return false;
            if(getAddress() != null ? !getAddress().equals(partition.getAddress()) : partition.getAddress() != null)
                return false;
            return getType() != null ? getType().equals(partition.getType()) : partition.getType() == null;
        }

        @Override
        public int hashCode() {
            int result = getName() != null ? getName().hashCode() : 0;
            result = 31 * result + (getAddress() != null ? getAddress().hashCode() : 0);
            result = 31 * result + (getType() != null ? getType().hashCode() : 0);
            result = 31 * result + (int) (getMax() ^ (getMax() >>> 32));
            result = 31 * result + (int) (getFree() ^ (getFree() >>> 32));
            result = 31 * result + (int) (inode ^ (inode >>> 32));
            result = 31 * result + (int) (getInodeFree() ^ (getInodeFree() >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "Partition{" +
                    "name='" + name + '\'' +
                    ", address='" + address + '\'' +
                    ", type='" + type + '\'' +
                    ", max=" + max +
                    ", free=" + free +
                    ", inode=" + inode +
                    ", inodeFree=" + inodeFree +
                    '}';
        }
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
}
