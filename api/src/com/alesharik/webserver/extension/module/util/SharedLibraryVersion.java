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

package com.alesharik.webserver.extension.module.util;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * This class wraps Shared Library versions. All versions are parsed according Maven spec(<code>MAJOR.MINOR.PATCH</code>)
 */
public final class SharedLibraryVersion implements Comparable<SharedLibraryVersion> {
    private final int[] version;

    public SharedLibraryVersion(@Nonnull String version) {
        String[] parts = version.split("\\.");
        if(parts.length < 1)
            throw new IllegalArgumentException("Given version has no major version");
        int[] v = new int[parts.length];
        for(int i = 0; i < parts.length; i++) v[i] = Integer.parseInt(parts[i]);
        this.version = v;
    }

    public SharedLibraryVersion(int[] version) {
        this.version = Arrays.copyOf(version, version.length);
        if(version.length < 1)
            throw new IllegalArgumentException("Given version has no major version");
    }

    /**
     * Return major library version
     *
     * @return the major version
     */
    public int getMajorVersion() {
        return version[0];
    }

    /**
     * Return minor library version
     *
     * @return the minor version. Can be <code>-1</code>
     */
    public int getMinorVersion() {
        return version.length > 1 ? version[1] : -1;
    }

    /**
     * Return patch version
     *
     * @return the patch version. Can be <code>-1</code>
     */
    public int getPatch() {
        return version.length > 2 ? version[2] : -1;
    }

    /**
     * Return all versions of the library
     *
     * @return all versions
     */
    public int[] getVersion() {
        return Arrays.copyOf(version, version.length);
    }

    @Override
    public int compareTo(@Nonnull SharedLibraryVersion o) {
        for(int i = 0; i < version.length; i++) {
            if(i >= o.version.length)
                if(version[i] != 0)
                    return 1;
            if(o.version[i] > version[i])
                return -1;
            else if(o.version[i] < version[i])
                return 1;
        }
        if(version.length < o.version.length)
            for(int i = version.length; i < o.version.length; i++)
                if(o.version[i] != 0)
                    return -1;
        return 0;
    }
}
