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

package com.alesharik.webserver.api.serial;

/**
 * This management bean exposes some internals of {@link Serial}
 */
public interface SerialMXBean {
    /**
     * Return total serializer count
     * @return total serializer count
     */
    long getSerializerCount();

    /**
     * Return total conversion count
     * @return total conversion count
     */
    long getConversionCount();

    /**
     * Return class name for conversion
     * @param id conversion id
     * @return class name, or <code>""</code>(empty line) if conversion cannot be found
     */
    String getConversionFor(long id);

    /**
     * Return total failed id/class mapping count.
     * In other words, return count of {@link SerializationMappingNotFoundException} thrown
     * @return total failed id/class mapping count
     */
    long getFailedMappingCount();

    /**
     * Return total size mismatch count({@link #getUnderflowCount()} + {@link #getOverflowCount()})
     * @return total size mismatch count
     */
    long getSizeMismatchCount();

    /**
     * Return total underflow count.
     * In other words, return count of {@link DataUnderflowException} thrown
     * @return total underflow count
     */
    long getUnderflowCount();

    /**
     * Return total overflow count.
     * In other words, return count of {@link DataOverflowException} thrown
     * @return total overflow count
     */
    long getOverflowCount();
}
