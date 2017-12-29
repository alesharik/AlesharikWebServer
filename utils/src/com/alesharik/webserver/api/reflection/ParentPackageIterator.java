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
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.alesharik.webserver.api.reflection.PackageUtils.getPackage;

/**
 * Iterates only packages with package-info class
 */
public final class ParentPackageIterator {
    public static Iterator<Package> forPackage(@Nonnull String pkg) {
        return new IteratorImpl(getPackages(pkg));
    }

    public static Iterator<Package> forPackageReverse(@Nonnull String pkg) {
        return new ReverseIterator(getPackages(pkg));
    }

    private static Package[] getPackages(String pkg) {
        String[] p = pkg.split("\\.");
        int index = 0;
        Package[] arr = new Package[p.length];
        for(int i = 0; i <= p.length; i++) {
            String pk = MiscUtils.sliceString(p, i, ".");
            Package pack = getPackage(pk);
            if(pack != null) {
                arr[index] = pack;
                index++;
            }
        }
        return Arrays.copyOf(arr, index);
    }

    private static final class IteratorImpl implements Iterator<Package> {
        private final Package[] packages;
        private int i;

        public IteratorImpl(Package[] packages) {
            this.packages = packages;
            this.i = 0;
        }

        @Override
        public boolean hasNext() {
            return i < packages.length;
        }

        @Override
        public Package next() {
            if(i >= packages.length)
                throw new NoSuchElementException();
            Package p = packages[i];
            i++;
            return p;
        }
    }

    private static final class ReverseIterator implements Iterator<Package> {
        private final Package[] packages;
        private int i;

        public ReverseIterator(Package[] packages) {
            this.packages = packages;
            this.i = packages.length - 1;
        }

        @Override
        public boolean hasNext() {
            return i >= 0;
        }

        @Override
        public Package next() {
            if(i < 0)
                throw new NoSuchElementException();
            Package p = packages[i];
            i--;
            return p;
        }
    }
}
