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

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.fail;

public class CryptoUtilsTest {
    @Test
    public void testCreateNewStrongRandom() {
        int[][] nums = new int[100][100];
        for(int i = 0; i < 100; i++) {
            SecureRandom random = CryptoUtils.newStrongSecureRandom();
            for(int j = 0; j < 100; j++) {
                nums[i][j] = random.nextInt();
            }
        }
        boolean ok = true;
        int tryI = 0;
        while(!ok) {
            System.out.println("Try " + tryI);
            tryI++;

            List<Integer> integers = new ArrayList<>();
            for(int[] num : nums) {
                for(int i : num) {
                    if(integers.contains(i)) {
                        ok = false;
                        break;
                    }
                    integers.add(i);
                }
            }
        }
    }

    @Test
    public void testCreateNewRandomString() {
        List<String> strings = new ArrayList<>();
        for(int i = 0; i < 1000; i++) {
            String s = CryptoUtils.generateRandomString(512);
            if(strings.contains(s))
                fail("Duplicated string detected!");
            strings.add(s);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateIllegalRandomString() {
        CryptoUtils.generateRandomString(0);
    }

    @Test
    public void testCreateNewSalt() {
        byte[][] salts = new byte[100][8];
        for(int i = 0; i < 100; i++) {
            byte[] salt = CryptoUtils.createSalt();
            salts[i] = salt;
        }
        for(int i = 0; i < salts.length; i++) {
            for(int j = 0; j < salts.length; j++) {
                if(i == j)
                    continue;
                if(Arrays.equals(salts[i], salts[j]))
                    fail("Duplicated salts detected!");
            }
        }
    }


    @Test
    public void testGenerateBytes() {
        byte[][] bytes = new byte[100][100];
        for(int i = 0; i < 100; i++) {
            byte[] byteArr = CryptoUtils.generateRandomBytes(100);
            bytes[i] = byteArr;
        }
        for(int i = 0; i < bytes.length; i++) {
            for(int j = 0; j < bytes.length; j++) {
                if(i == j)
                    continue;
                if(Arrays.equals(bytes[i], bytes[j]))
                    fail("Duplicated byte arrays detected!");
            }
        }
    }

    @Test
    public void testUtils() {
        TestUtils.assertUtilityClass(CryptoUtils.class);
    }
}