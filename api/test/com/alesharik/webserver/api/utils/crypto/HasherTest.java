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

package com.alesharik.webserver.api.utils.crypto;

import com.alesharik.webserver.api.TestUtils;
import org.glassfish.grizzly.utils.Charsets;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HasherTest {
    private final byte[] test = "sdfdfggjhgm,;'iol,fbhsdfsd;fd,gl;sdfsdflsdfl;;dldsf;l;sd;gllsd;lflsdlfl;fg;df,ld;mglsnhc;xoisecmfp,aogizrx,hk ,uthm g".getBytes(Charsets.UTF8_CHARSET);
    private final byte[] salt = CryptoUtils.createSalt();
    private File temp;

    @Before
    public void setUp() throws Exception {
        temp = File.createTempFile("AlesharikWebServer", "dsfadfsdfsdf");
        temp.deleteOnExit();
        Files.write(temp.toPath(), test, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Test
    public void testSHA512() throws Exception {
        if(!Hasher.isSha512Supported())
            return;

        byte[] hash1 = Hasher.securedSHA512(test, salt);
        byte[] hash2 = Hasher.SHA512(test, salt);

        assertTrue(Arrays.equals(hash1, hash2));
    }

    @Test
    public void testMD5() throws Exception {
        if(!Hasher.isMd5Supported())
            return;

        byte[] hash1 = Hasher.MD5(test, salt);
        byte[] hash2 = Hasher.securedMD5(test, salt);
        byte[] hash3 = Hasher.MD5(temp, salt);
        byte[] hash4 = Hasher.MD5(new ByteArrayInputStream(test), salt);

        byte[] hash5 = Hasher.MD5Async(temp, salt).get();
        byte[] hash6 = Hasher.MD5Async(new ByteArrayInputStream(test), salt).get();

        assertTrue(Arrays.equals(hash1, hash2));
        assertTrue(Arrays.equals(hash1, hash3));
        assertTrue(Arrays.equals(hash1, hash4));
        assertTrue(Arrays.equals(hash1, hash5));
        assertTrue(Arrays.equals(hash1, hash6));
    }

    @Test
    public void testCRC() throws Exception {
        long hash1 = Hasher.securedCRC32(test, salt);
        long hash2 = Hasher.CRC32(test, salt);
        long hash3 = Hasher.CRC32(temp, salt);
        long hash4 = Hasher.CRC32(new ByteArrayInputStream(test), salt);

        long hash5 = Hasher.CRC32Async(temp, salt).get();
        long hash6 = Hasher.CRC32Async(new ByteArrayInputStream(test), salt).get();

        assertEquals(hash1, hash2);
        assertEquals(hash1, hash3);
        assertEquals(hash1, hash4);
        assertEquals(hash1, hash5);
        assertEquals(hash1, hash6);
    }

    @Test
    public void testUtils() throws Exception {
        TestUtils.assertUtilityClass(Hasher.class);
    }
}