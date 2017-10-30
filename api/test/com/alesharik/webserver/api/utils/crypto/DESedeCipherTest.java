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
import org.junit.Test;

import javax.crypto.SecretKey;
import java.nio.charset.Charset;

import static com.alesharik.webserver.api.utils.crypto.DESedeCipher.*;
import static org.junit.Assert.assertEquals;

public class DESedeCipherTest {
    @Test
    public void testUtility() throws Exception {
        TestUtils.assertUtilityClass(DESedeCipher.class);
    }

    @Test
    public void testCycle() throws Exception {
        SecretKey secretKey = generateNewSecretKey();
        String test = "This is a test DESede string";

        byte[] encrypted = encrypt(test.getBytes(Charset.forName("UTF-8")), secretKey);
        String decrypted = new String(decrypt(encrypted, secretKey), Charset.forName("UTF-8"));
        assertEquals(test, decrypted);
    }
}