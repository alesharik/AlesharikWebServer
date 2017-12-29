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

package com.alesharik.webserver.api.reflection;

import com.alesharik.webserver.api.MiscUtils;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * Iterates only packages with package-info class
 */
public class ParentPackageIterator implements Iterator<Package> {
    private String[] pkg;
    private int i;

    private ParentPackageIterator(String pkg) {
        this.pkg = pkg.split(".");
        i = this.pkg.length - 1;
    }

    public static Iterator<Package> forPackage(@Nonnull String pkg) {
        return new ParentPackageIterator(pkg);
    }

    public static Iterator<Package> reversePackage(@Nonnull String pkg) {
        return new ReverseIterator(pkg);
    }

    @Override
    public boolean hasNext() {
        return i >= 0;
    }

    @Override
    public Package next() {
        Package next;
        for(; (next = PackageUtils.getPackage(MiscUtils.sliceString(pkg, i))) == null; i--) {
        }
        return next;
    }

    private static final class ReverseIterator implements Iterator<Package> {
        private String[] pkg;
        private int i;

        private ReverseIterator(String pkg) {
            this.pkg = pkg.split(".");
            i = 1;
        }

        @Override
        public boolean hasNext() {
            return i <= pkg.length;
        }

        @Override
        public Package next() {
            Package next;
            for(; (next = PackageUtils.getPackage(MiscUtils.sliceString(pkg, i))) == null; i++) {
            }
            return next;
        }
    }
}
