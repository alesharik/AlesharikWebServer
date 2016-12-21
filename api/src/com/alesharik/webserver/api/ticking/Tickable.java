package com.alesharik.webserver.api.ticking;

/**
 * This class may tick!
 */
@FunctionalInterface
public interface Tickable {
    /**
     * This function called in ANOTHER THREAD.
     * Do not recommended to do long work(get response form internet server, database or etc) if you are not using thread pools
     * All exceptions will be logged with main logger and prefix <code>[TickingPool]</code>
     */
    void tick() throws Exception;
}
