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

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.nio.ByteOrder;

@UtilityClass
public class ByteOrderUtils {
    private static final ByteOrder NATIVE_BYTE_ORDER = ByteOrder.nativeOrder();

    public static short format(short s, @Nonnull ByteOrder byteOrder) {
        if(byteOrder == NATIVE_BYTE_ORDER)
            return s;
        else
            return Short.reverseBytes(s);
    }

    public static int format(int i, @Nonnull ByteOrder byteOrder) {
        if(byteOrder == NATIVE_BYTE_ORDER)
            return i;
        else
            return Integer.reverseBytes(i);
    }

    public static long format(long l, @Nonnull ByteOrder byteOrder) {
        if(byteOrder == NATIVE_BYTE_ORDER)
            return l;
        else
            return Long.reverseBytes(l);
    }

    public static char format(char c, @Nonnull ByteOrder byteOrder) {
        if(byteOrder == NATIVE_BYTE_ORDER)
            return c;
        else
            return Character.reverseBytes(c);
    }

    public static float format(float f, @Nonnull ByteOrder byteOrder) {
        if(byteOrder == NATIVE_BYTE_ORDER)
            return f;
        else {
            int bits = Float.floatToIntBits(f);
            bits = format(bits, byteOrder);
            return Float.intBitsToFloat(bits);
        }
    }

    public static double format(double d, @Nonnull ByteOrder byteOrder) {
        if(byteOrder == NATIVE_BYTE_ORDER)
            return d;
        else {
            long bits = Double.doubleToLongBits(d);
            bits = format(bits, byteOrder);
            return Double.longBitsToDouble(bits);
        }
    }
}
