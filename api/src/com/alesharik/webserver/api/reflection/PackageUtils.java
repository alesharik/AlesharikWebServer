package com.alesharik.webserver.api.reflection;

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@UtilityClass
public final class PackageUtils {

    /**
     * Return package only if requested package contains package-info class, overwise return <code>null</code>
     *
     * @param pkg the package name
     * @return requested package or null
     */
    @Nullable
    public static Package getPackage(@Nonnull String pkg) {
        try {
            Class.forName(pkg + ".package-info", false, PackageUtils.class.getClassLoader());
            return Package.getPackage(pkg);
        } catch (ClassNotFoundException e) {
            return null; //package-info not found
        }
    }
}
