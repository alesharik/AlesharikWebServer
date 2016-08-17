package com.alesharik.webserver.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class CompressionUtils {
    private static final Deflater deflater = new Deflater();
    private static final Inflater inflater = new Inflater();

    private CompressionUtils() {
    }

    public static byte[] compress(byte[] bytes) throws IOException {
        return compress(bytes, CompressLevel.DEFAULT_COMPRESSION.getValue());
    }

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

    public enum CompressLevel {
        NO_COMPRESSION(0),
        BEST_SPEED(1),
        BEST_COMPRESSION(9),
        DEFAULT_COMPRESSION(-1);

        private final int value;

        CompressLevel(int value) {
            this.value = value;
        }

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
