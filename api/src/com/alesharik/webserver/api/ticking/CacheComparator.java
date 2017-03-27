package com.alesharik.webserver.api.ticking;

/**
 * This interface used by cache for comparing objects
 */
public interface CacheComparator {
    boolean objectEquals(Object other);

    int objectHashCode();
}
