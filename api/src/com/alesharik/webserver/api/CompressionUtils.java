package com.alesharik.webserver.api;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * This class compress and decompress <code>byte</code> arrays
 */
@UtilityClass
public final class CompressionUtils {
    private static final Deflater deflater = new Deflater();
    private static final Inflater inflater = new Inflater();

    /**
     * Compress <code>byte</code> array with default compression level
     *
     * @param bytes <code>byte</code> array to compress
     * @return compressed <code>byte</code> array
     * @throws IOException if anything happens
     */
    public static byte[] compress(byte[] bytes) throws IOException {
        return compress(bytes, CompressLevel.DEFAULT_COMPRESSION.getValue());
    }

    /**
     * Compress <code>byte</code> array
     * @param bytes <code>byte</code> array to compress
     * @param level compress level. See {@link CompressLevel}
     * @return compressed <code>byte</code> array
     * @throws IOException if anything happens
     */
    public static byte[] compress(byte[] bytes, int level) throws IOException {
        deflater.setInput(bytes);
        deflater.setLevel(level);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bytes.length);

        deflater.finish();
        byte[] buffer = new byte[1024];
        while(!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();

        byte[] output = outputStream.toByteArray();
        deflater.reset();
        return output;
    }

    /**
     * Decompress <code>byte</code> array
     * @param bytes <code>byte</code> array to decompress
     * @return decompressed <code>byte</code> array
     * @throws DataFormatException if anything happens
     * @throws IOException if anything happens
     */
    public static byte[] decompress(byte[] bytes) throws DataFormatException, IOException {
        inflater.setInput(bytes);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bytes.length);

        byte[] buffer = new byte[1024];
        while(!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();

        byte[] output = outputStream.toByteArray();
        inflater.reset();
        return output;
    }

    /**
     * Compression level enum
     */
    public enum CompressLevel {
        /**
         * Do not compress. This does not mean what same array return after compression!
         */
        NO_COMPRESSION(0),
        /**
         * Compress with best speed
         */
        BEST_SPEED(1),
        /**
         * Compress with best compression
         */
        BEST_COMPRESSION(9),
        /**
         * Default value
         */
        DEFAULT_COMPRESSION(-1);

        private final int value;

        CompressLevel(int value) {
            this.value = value;
        }

        /**
         * Return int value for {@link #compress(byte[], int)} or {@link Deflater}
         */
        public int getValue() {
            return value;
        }

        public static CompressLevel valueOf(int level) {
            switch (level) {
                case 0:
                    return NO_COMPRESSION;
                case 1:
                    return BEST_SPEED;
                case 9:
                    return BEST_COMPRESSION;
                case -1:
                default:
                    return DEFAULT_COMPRESSION;
            }
        }
    }
}
