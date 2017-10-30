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

import com.alesharik.webserver.api.Utils;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;

public class KeySaverTest {
    private File file;
    private SecretKey secretKey;

    @Before
    public void setUp() throws Exception {
        file = File.createTempFile("asdsasadsasad", "asdasdasdsassa");
        secretKey = StringCipher.generateKey(Utils.getRandomString(24));
    }

    @Test
    public void testCycle() throws Exception {
        KeySaver.saveKeyToFile(secretKey, file);
        SecretKey parsed = KeySaver.loadKeyFromFile(file, "DESede");
        assertEquals(secretKey, parsed);
    }

    @Test(expected = FileNotFoundException.class)
    public void testLoadFromNotExistingFile() throws Exception {
        KeySaver.loadKeyFromFile(new File("sad"), "DESede");
    }

    @Test(expected = FileNotFoundException.class)
    public void saveKeyToNotExistingFile() throws Exception {
        KeySaver.saveKeyToFile(secretKey, new File("asd"));
    }
}