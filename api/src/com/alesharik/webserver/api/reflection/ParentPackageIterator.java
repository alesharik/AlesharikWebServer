package com.alesharik.webserver.api.reflection;

import com.alesharik.webserver.api.Utils;

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
        for(; (next = PackageUtils.getPackage(Utils.sliceString(pkg, i))) == null; i--) {
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
            for(; (next = PackageUtils.getPackage(Utils.sliceString(pkg, i))) == null; i++) {
            }
            return next;
        }
    }
}
