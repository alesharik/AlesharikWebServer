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

package com.alesharik.webserver.api;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * This class compress and decompress <code>byte</code> arrays with deflate algorithm
 */
@UtilityClass
public final class CompressionUtils {
    private static final ThreadLocal<Deflater> deflater = ThreadLocal.withInitial(Deflater::new);
    private static final ThreadLocal<Inflater> inflater = ThreadLocal.withInitial(Inflater::new);

    /**
     * Compress <code>byte</code> array with default compression level
     *
     * @param bytes <code>byte</code> array to compress
     * @return compressed <code>byte</code> array
     */
    @Nonnull
    public static byte[] deflateCompress(@Nonnull byte[] bytes) {
        return deflateCompress(bytes, CompressLevel.DEFAULT_COMPRESSION);
    }

    /**
     * Compress <code>byte</code> array
     * @param bytes <code>byte</code> array to compress
     * @param level compress level. See {@link CompressLevel}
     * @return compressed <code>byte</code> array
     */
    @Nonnull
    @SneakyThrows(IOException.class) //Will never happen
    public static byte[] deflateCompress(@Nonnull byte[] bytes, @Nonnull CompressLevel level) {
        Deflater deflater = CompressionUtils.deflater.get();
        deflater.setInput(bytes);
        deflater.setLevel(level.value);

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
     */
    @Nonnull
    @SneakyThrows(IOException.class) //Will never happen
    public static byte[] deflateDecompress(@Nonnull byte[] bytes) throws DataFormatException {
        Inflater inflater = CompressionUtils.inflater.get();
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

        public int getValue() {
            return value;
        }

        @Nonnull
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
