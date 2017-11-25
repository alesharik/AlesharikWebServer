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

package com.alesharik.webserver.api.collections;

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

@UtilityClass
public class CollectionsMathUtils {
    public static final int MAP_MAXIMUM_CAPACITY = 1 << 30;

    /**
     * Calculate bucket for hash
     *
     * @param hash        the hash
     * @param bucketCount bucket array length
     * @return bucket number
     */
    @Nonnegative
    public static int getBucket(int hash, int bucketCount) {
        return (bucketCount - 1) & hash;
    }

    /**
     * Calculate object hash only for maps
     *
     * @param key the object
     * @return object's hash
     */
    @Nonnegative
    public static int hash(@Nullable Object key) {
        if(key == null)
            return 0;

        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    /**
     * Return nearest number from given number, which is <code>>= given number</code> and is power of 2
     *
     * @param num the given number
     * @return positive number, which is >= given number and is power of 2. If given number <code><= 1</code>, it will return 1
     */
    @Nonnegative
    public static int powerOfTwoFor(int num) {
        int n = num - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= Integer.MAX_VALUE) ? MAP_MAXIMUM_CAPACITY : n + 1;
    }
}
