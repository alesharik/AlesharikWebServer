package com.alesharik.webserver.api.loadInfo;

/**
 * This interface represent a gathered information and way to gather information. An implementation must be thread-safe!
 */
@Deprecated
public interface Info {
    /**
     * This function called every <code>getId()</code> milliseconds in other thread.In this function must be
     * code that load or update info fields
     */
    void loadInfo();

    /**
     * The id of Info class. Used as second-layer identification(first-layer is name).
     */
    default String getId() {
        return "";
    }

    /**
     * Return period in milliseconds to call <code>loadInfo()</code>
     */
    default long getUpdateMillis() {
        return 1000;
    }

    /**
     * Stop calling loadInfo()
     */
    default void stopLoadingData() {
        InfoManager.stopLoadingData(this);
    }

    /**
     * Start calling loadInfo()
     */
    default void startLoadingData() {
        InfoManager.startLoadingData(this);
    }

    /**
     * Shutdown thread. PERMANENTLY
     */
    default void shutdown() {
        InfoManager.terminateInfoLoading(this);
    }
}
