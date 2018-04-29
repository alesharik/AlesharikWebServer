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

package com.alesharik.webserver.configuration.config.lang.element;

import com.alesharik.webserver.configuration.config.lang.FormatException;

public interface ConfigurationObjectArray extends ConfigurationArray, Iterable<ConfigurationElement> {
    int size();

    ConfigurationElement get(int index);

    ConfigurationElement[] getElements();

    default int[] toIntArray() {
        int[] arr = new int[size()];
        for(int i = 0; i < size(); i++) {
            ConfigurationElement element = get(i);
            if(!(element instanceof ConfigurationPrimitive.Int))
                throw new FormatException("Can't convert " + element.getClass().getCanonicalName() + " to int!");
            arr[i] = ((ConfigurationPrimitive.Int) element).value();
        }
        return arr;
    }

    default byte[] toByteArray() {
        byte[] arr = new byte[size()];
        for(int i = 0; i < size(); i++) {
            ConfigurationElement element = get(i);
            if(!(element instanceof ConfigurationPrimitive.Byte))
                throw new FormatException("Can't convert " + element.getClass().getCanonicalName() + " to byte!");
            arr[i] = ((ConfigurationPrimitive.Byte) element).value();
        }
        return arr;
    }

    default short[] toShortArray() {
        short[] arr = new short[size()];
        for(int i = 0; i < size(); i++) {
            ConfigurationElement element = get(i);
            if(!(element instanceof ConfigurationPrimitive.Short))
                throw new FormatException("Can't convert " + element.getClass().getCanonicalName() + " to short!");
            arr[i] = ((ConfigurationPrimitive.Short) element).value();
        }
        return arr;
    }

    default long[] toLongArray() {
        long[] arr = new long[size()];
        for(int i = 0; i < size(); i++) {
            ConfigurationElement element = get(i);
            if(!(element instanceof ConfigurationPrimitive.Long))
                throw new FormatException("Can't convert " + element.getClass().getCanonicalName() + " to long!");
            arr[i] = ((ConfigurationPrimitive.Long) element).value();
        }
        return arr;
    }

    default float[] toFloatArray() {
        float[] arr = new float[size()];
        for(int i = 0; i < size(); i++) {
            ConfigurationElement element = get(i);
            if(!(element instanceof ConfigurationPrimitive.Float))
                throw new FormatException("Can't convert " + element.getClass().getCanonicalName() + " to float!");
            arr[i] = ((ConfigurationPrimitive.Float) element).value();
        }
        return arr;
    }

    default double[] toDoubleArray() {
        double[] arr = new double[size()];
        for(int i = 0; i < size(); i++) {
            ConfigurationElement element = get(i);
            if(!(element instanceof ConfigurationPrimitive.Double))
                throw new FormatException("Can't convert " + element.getClass().getCanonicalName() + " to double!");
            arr[i] = ((ConfigurationPrimitive.Double) element).value();
        }
        return arr;
    }

    default char[] toCharArray() {
        char[] arr = new char[size()];
        for(int i = 0; i < size(); i++) {
            ConfigurationElement element = get(i);
            if(!(element instanceof ConfigurationPrimitive.Char))
                throw new FormatException("Can't convert " + element.getClass().getCanonicalName() + " to char!");
            arr[i] = ((ConfigurationPrimitive.Char) element).value();
        }
        return arr;
    }

    default boolean[] toBooleanArray() {
        boolean[] arr = new boolean[size()];
        for(int i = 0; i < size(); i++) {
            ConfigurationElement element = get(i);
            if(!(element instanceof ConfigurationPrimitive.Boolean))
                throw new FormatException("Can't convert " + element.getClass().getCanonicalName() + " to boolean!");
            arr[i] = ((ConfigurationPrimitive.Boolean) element).value();
        }
        return arr;
    }

    default String[] toStringArray() {
        String[] arr = new String[size()];
        for(int i = 0; i < size(); i++) {
            ConfigurationElement element = get(i);
            if(!(element instanceof ConfigurationPrimitive.String))
                throw new FormatException("Can't convert " + element.getClass().getCanonicalName() + " to string!");
            arr[i] = ((ConfigurationPrimitive.String) element).value();
        }
        return arr;
    }
}
