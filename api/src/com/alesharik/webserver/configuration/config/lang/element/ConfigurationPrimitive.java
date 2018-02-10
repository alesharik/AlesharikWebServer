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

public interface ConfigurationPrimitive extends ConfigurationElement {
    interface Int extends ConfigurationPrimitive {
        int value();
    }

    interface Long extends ConfigurationPrimitive {
        long value();
    }

    interface String extends ConfigurationPrimitive {
        java.lang.String value();
    }

    interface Char extends ConfigurationPrimitive {
        char value();
    }

    interface Float extends ConfigurationPrimitive {
        float value();
    }

    interface Double extends ConfigurationPrimitive {
        double value();
    }

    interface Boolean extends ConfigurationPrimitive {
        boolean value();
    }

    interface Short extends ConfigurationPrimitive {
        short value();
    }

    interface Byte extends ConfigurationPrimitive {
        byte value();
    }
}
