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
