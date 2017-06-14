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

import com.alesharik.webserver.TestUtils;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.Arrays;

import static org.junit.Assert.*;

public class CompressionUtilsTest {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Test
    public void compress() throws Exception {
        byte[] arr = new byte[1024];
        SECURE_RANDOM.nextBytes(arr);
        byte[] compressed = CompressionUtils.compress(arr);
        assertFalse(Arrays.equals(compressed, arr));
        assertArrayEquals(CompressionUtils.decompress(compressed), arr);
    }

    @Test
    public void compressWithNoCompression() throws Exception {
        byte[] arr = new byte[1024];
        SECURE_RANDOM.nextBytes(arr);
        byte[] ret = CompressionUtils.compress(arr, CompressionUtils.CompressLevel.NO_COMPRESSION.getValue());
        assertArrayEquals(CompressionUtils.decompress(ret), arr);
    }

    @Test
    public void assertWithBestSpeed() throws Exception {
        byte[] arr = new byte[1024];
        SECURE_RANDOM.nextBytes(arr);
        byte[] ret = CompressionUtils.compress(arr, CompressionUtils.CompressLevel.BEST_SPEED.getValue());
        assertFalse(Arrays.equals(ret, arr));
        assertArrayEquals(CompressionUtils.decompress(ret), arr);
    }

    @Test
    public void assertWithBestCompression() throws Exception {
        byte[] arr = new byte[1024];
        SECURE_RANDOM.nextBytes(arr);
        byte[] ret = CompressionUtils.compress(arr, CompressionUtils.CompressLevel.BEST_COMPRESSION.getValue());
        assertFalse(Arrays.equals(ret, arr));
        assertArrayEquals(CompressionUtils.decompress(ret), arr);
    }

    @Test
    public void assertWithDefaultCompression() throws Exception {
        byte[] arr = new byte[1024];
        SECURE_RANDOM.nextBytes(arr);
        byte[] ret = CompressionUtils.compress(arr, CompressionUtils.CompressLevel.DEFAULT_COMPRESSION.getValue());
        assertFalse(Arrays.equals(ret, arr));
        assertArrayEquals(CompressionUtils.decompress(ret), arr);
    }

    @Test
    public void compressionLevelValueOfTest() throws Exception {
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
    public void testUtility() throws Exception {
        TestUtils.assertUtilityClass(CompressionUtils.class);
    }
}