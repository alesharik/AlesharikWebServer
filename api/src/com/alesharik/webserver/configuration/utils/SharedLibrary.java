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

package com.alesharik.webserver.configuration.utils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.function.Predicate;

/**
 * Shared Library is a JAR library, that loads with server as a module dependency. All dependent module's classloaders
 * will be linked with Shared Library classloader automatically. All transitive dependencies will be resolved automatically
 */
public interface SharedLibrary {
    /**
     * Return library name
     *
     * @return the library name
     */
    @Nonnull
    String getName();

    /**
     * Return library version
     *
     * @return the library version
     */
    @Nonnull
    SharedLibraryVersion getVersion();

    /**
     * Return shared library JAR file
     *
     * @return the shared library JAR file
     */
    @Nonnull
    File getFile();

    final class SharedLibraryPredicate implements Predicate<SharedLibrary> {
        private final SharedLibraryVersion version;
        private final String name;
        private final VersionMode versionMode;

        public SharedLibraryPredicate(String spec) {
            boolean hasVersion = false;
            String actual = spec;
            if(spec.startsWith("<=")) {
                versionMode = VersionMode.LESS_EQUALS;
                hasVersion = true;
                actual = spec.substring(2);
            } else if(spec.startsWith(">=")) {
                versionMode = VersionMode.MORE_EQUALS;
                hasVersion = true;
                actual = spec.substring(2);
            } else if(spec.startsWith("<")) {
                versionMode = VersionMode.LESS;
                hasVersion = true;
                actual = spec.substring(1);
            } else if(spec.startsWith(">")) {
                versionMode = VersionMode.MORE;
                hasVersion = true;
                actual = spec.substring(1);
            } else if(spec.startsWith("=")) {
                versionMode = VersionMode.EQUALS;
                hasVersion = true;
                actual = spec.substring(1);
            } else
                versionMode = VersionMode.ALL;

            int vSplit = actual.lastIndexOf("-");
            if(hasVersion && vSplit == -1)
                throw new IllegalArgumentException("Spec doesn't have version, but it is required!");
            String v = vSplit == -1 ? null : actual.substring(vSplit + 1);
            name = vSplit == -1 ? actual : actual.substring(0, vSplit);
            if(hasVersion)
                version = new SharedLibraryVersion(v);
            else
                version = null;
        }

        @Override
        public boolean test(SharedLibrary sharedLibraryVersion) {
            if(!sharedLibraryVersion.getName().equals(name))
                return false;
            switch (versionMode) {
                case ALL:
                    return true;
                case LESS:
                    return version.compareTo(sharedLibraryVersion.getVersion()) > 0;
                case LESS_EQUALS:
                    return version.compareTo(sharedLibraryVersion.getVersion()) >= 0;
                case MORE:
                    return version.compareTo(sharedLibraryVersion.getVersion()) < 0;
                case MORE_EQUALS:
                    return version.compareTo(sharedLibraryVersion.getVersion()) <= 0;
                case EQUALS:
                    return version.compareTo(sharedLibraryVersion.getVersion()) == 0;
                default:
                    return false;
            }
        }

        private enum VersionMode {
            LESS,
            MORE,
            LESS_EQUALS,
            MORE_EQUALS,
            EQUALS,
            ALL
        }
    }
}
