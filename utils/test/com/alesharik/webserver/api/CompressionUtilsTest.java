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

import com.alesharik.webserver.test.TestUtils;
import org.junit.Test;

import java.util.Arrays;

import static java.util.concurrent.ThreadLocalRandom.current;
import static org.junit.Assert.*;

public class CompressionUtilsTest {

    @Test
    public void compress() {
        byte[] arr = new byte[1024 * 1024 * 4];
        current().nextBytes(arr);
        byte[] compressed = CompressionUtils.deflateCompress(arr);
        assertFalse(Arrays.equals(compressed, arr));
        assertArrayEquals(CompressionUtils.deflateDecompress(compressed), arr);
    }

    @Test
    public void compressWithNoCompression() {
        byte[] arr = new byte[1024 * 1024 * 4];
        current().nextBytes(arr);
        byte[] ret = CompressionUtils.deflateCompress(arr, CompressionUtils.CompressLevel.NO_COMPRESSION);
        assertArrayEquals(CompressionUtils.deflateDecompress(ret), arr);
    }

    @Test
    public void assertWithBestSpeed() {
        byte[] arr = new byte[1024 * 1024 * 4];
        current().nextBytes(arr);
        byte[] ret = CompressionUtils.deflateCompress(arr, CompressionUtils.CompressLevel.BEST_SPEED);
        assertFalse(Arrays.equals(ret, arr));
        assertArrayEquals(CompressionUtils.deflateDecompress(ret), arr);
    }

    @Test
    public void assertWithBestCompression() {
        byte[] arr = new byte[1024 * 1024 * 4];
        current().nextBytes(arr);
        byte[] ret = CompressionUtils.deflateCompress(arr, CompressionUtils.CompressLevel.BEST_COMPRESSION);
        assertFalse(Arrays.equals(ret, arr));
        assertArrayEquals(CompressionUtils.deflateDecompress(ret), arr);
    }

    @Test
    public void assertWithDefaultCompression() {
        byte[] arr = new byte[1024 * 1024 * 4];
        current().nextBytes(arr);
        byte[] ret = CompressionUtils.deflateCompress(arr, CompressionUtils.CompressLevel.DEFAULT_COMPRESSION);
        assertFalse(Arrays.equals(ret, arr));
        assertArrayEquals(CompressionUtils.deflateDecompress(ret), arr);
    }

    @Test
    public void compressionLevelValueOfTest() {
        CompressionUtils.CompressLevel no = CompressionUtils.CompressLevel.valueOf(0);
        CompressionUtils.CompressLevel bestCompression = CompressionUtils.CompressLevel.valueOf(9);
        CompressionUtils.CompressLevel bestSpeed = CompressionUtils.CompressLevel.valueOf(1);
        CompressionUtils.CompressLevel def = CompressionUtils.CompressLevel.valueOf(-1);

        assertSame(no, CompressionUtils.CompressLevel.NO_COMPRESSION);
        assertSame(bestCompression, CompressionUtils.CompressLevel.BEST_COMPRESSION);
        assertSame(bestSpeed, CompressionUtils.CompressLevel.BEST_SPEED);
        assertSame(def, CompressionUtils.CompressLevel.DEFAULT_COMPRESSION);
    }

    @Test
    public void testUtility() {
        TestUtils.assertUtilityClass(CompressionUtils.class);
    }

    @Test
    public void testGZIP() {
        byte[] data = new byte[1024 * 1024 * 4];
        current().nextBytes(data);

        byte[] compressed = CompressionUtils.gzipCompress(data);
        byte[] decompressed = CompressionUtils.gzipDecompress(compressed);

        assertArrayEquals(data, decompressed);
    }
}