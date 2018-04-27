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

package com.alesharik.webserver.configuration.config.lang.parser.elements;

import com.alesharik.webserver.configuration.config.lang.element.ConfigurationPrimitive;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

@Getter
@RequiredArgsConstructor
public abstract class PrimitiveImpl implements ConfigurationPrimitive {
    private final java.lang.String name;

    /**
     * @param line parse only primitive types! Do not try to parse functions, code blocks, objects, arrays or strings/chars here!
     * @return null - can't resolve the type
     * @throws NumberFormatException if can't parse number
     */
    @Nullable
    public static ConfigurationPrimitive parseNotString(java.lang.String name, java.lang.String line) {
        java.lang.String preParse = line.replace(" ", "");
        if("true".equals(preParse))
            return new BooleanImpl(name, true);
        else if("false".equals(preParse))
            return new BooleanImpl(name, false);
        else if(preParse.endsWith("B"))
            return new ByteImpl(name, java.lang.Byte.decode(preParse.substring(0, preParse.length() - 1)));
        else if(preParse.endsWith("S"))
            return new ShortImpl(name, java.lang.Short.decode(preParse.substring(0, preParse.length() - 1)));
        else if(preParse.endsWith("L"))
            return new LongImpl(name, java.lang.Long.decode(preParse.substring(0, preParse.length() - 1)));
        else if(preParse.endsWith("D"))
            return new DoubleImpl(name, java.lang.Double.parseDouble(preParse.substring(0, preParse.length() - 1)));
        else if(preParse.contains(".") || preParse.contains(","))
            return new FloatImpl(name, java.lang.Float.parseFloat(preParse));
        else
            return new IntImpl(name, Integer.decode(preParse));
    }

    public static ConfigurationPrimitive wrap(java.lang.String name, java.lang.String line) {
        return new StringImpl(name, line);
    }

    public static ConfigurationPrimitive wrap(java.lang.String name, char c) {
        return new CharImpl(name, c);
    }

    private static final class IntImpl extends PrimitiveImpl implements ConfigurationPrimitive.Int {
        private final int value;

        public IntImpl(java.lang.String name, int value) {
            super(name);
            this.value = value;
        }

        @Override
        public int value() {
            return value;
        }
    }

    private static final class ShortImpl extends PrimitiveImpl implements ConfigurationPrimitive.Short {
        private final short value;

        public ShortImpl(java.lang.String name, short value) {
            super(name);
            this.value = value;
        }

        @Override
        public short value() {
            return value;
        }
    }

    private static final class ByteImpl extends PrimitiveImpl implements ConfigurationPrimitive.Byte {
        private final byte value;

        public ByteImpl(java.lang.String name, byte value) {
            super(name);
            this.value = value;
        }

        @Override
        public byte value() {
            return value;
        }
    }

    private static final class FloatImpl extends PrimitiveImpl implements ConfigurationPrimitive.Float {
        private final float value;

        public FloatImpl(java.lang.String name, float value) {
            super(name);
            this.value = value;
        }

        @Override
        public float value() {
            return value;
        }
    }

    private static final class DoubleImpl extends PrimitiveImpl implements ConfigurationPrimitive.Double {
        private final double value;

        public DoubleImpl(java.lang.String name, double value) {
            super(name);
            this.value = value;
        }

        @Override
        public double value() {
            return value;
        }
    }

    private static final class LongImpl extends PrimitiveImpl implements ConfigurationPrimitive.Long {
        private final long value;

        public LongImpl(java.lang.String name, long value) {
            super(name);
            this.value = value;
        }

        @Override
        public long value() {
            return value;
        }
    }

    private static final class BooleanImpl extends PrimitiveImpl implements ConfigurationPrimitive.Boolean {
        private final boolean value;

        public BooleanImpl(java.lang.String name, boolean value) {
            super(name);
            this.value = value;
        }

        @Override
        public boolean value() {
            return value;
        }
    }

    private static final class CharImpl extends PrimitiveImpl implements ConfigurationPrimitive.Char {
        private final char c;

        public CharImpl(java.lang.String name, char c) {
            super(name);
            this.c = c;
        }

        @Override
        public char value() {
            return c;
        }
    }

    private static final class StringImpl extends PrimitiveImpl implements ConfigurationPrimitive.String {
        private final java.lang.String value;

        public StringImpl(java.lang.String name, java.lang.String value) {
            super(name);
            this.value = value;
        }

        @Override
        public java.lang.String value() {
            return value;
        }
    }
}
