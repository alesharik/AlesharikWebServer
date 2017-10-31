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

package com.alesharik.webserver.api.utils.crypto.crc;

import org.junit.After;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CRC32MessageDigestTest {
    static {
        new CRC32Provider();
    }

    private final CRC32MessageDigest digest;

    public CRC32MessageDigestTest() throws NoSuchAlgorithmException {
        this.digest = (CRC32MessageDigest) MessageDigest.getInstance("CRC32");
    }

    @After
    public void tearDown() throws Exception {
        digest.reset();
    }

    @Test
    public void simpleTest() throws Exception {
        digest.update((byte) 0x1);
        digest.update((byte) 0x2);
        digest.update((byte) 0x3);
        digest.update((byte) 0x4);

        long val = ByteBuffer.wrap(digest.digest()).getLong();

        digest.reset();

        digest.update(new byte[]{0x1, 0x2, 0x3, 0x4});

        assertEquals(val, digest.getDigest());
    }

    @Test
    public void performanceTest() throws Exception {
        byte[] data = new byte[1024];
        for(int i = 0; i < 1024 * 1024 * 8; i++) {//8GB
            ThreadLocalRandom.current().nextBytes(data);
            digest.update(data);
        }

        assertTrue(digest.getDigest() > 0);
    }
}