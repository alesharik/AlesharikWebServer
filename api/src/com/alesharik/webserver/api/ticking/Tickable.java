package com.alesharik.webserver.api.ticking;

/**
 * This class may tick! If class is mutable, you may need override {@link CacheComparator}.{@link #objectEquals(Object)} and {@link CacheComparator}.{@link #objectHashCode()} methods.
 * All ticking classes are cached. If your class must be non cached, return <code>false</code> in {@link CacheComparator}.{@link #objectEquals(Object)}
 */
@FunctionalInterface
public interface Tickable extends CacheComparator {
    /**
     * This function called in ANOTHER THREAD.
     * Do not recommended to do long work(get response form internet server, database or etc) if you are not using thread pool.
     * Main logger log all exceptions with <code>[TickingPool]</code> prefix
     */
    void tick() throws Exception;

    @Override
    default boolean objectEquals(Object other) {
        return this.equals(other);
    }

    @Override
    default int objectHashCode() {
        return this.hashCode();
    }
}
