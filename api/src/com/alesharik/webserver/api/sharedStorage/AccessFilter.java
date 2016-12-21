package com.alesharik.webserver.api.sharedStorage;

/**
 * This interface used for filter shared storage access
 */
public interface AccessFilter {
    /**
     * @param clazz     caller class
     * @param type      type of action
     * @param fieldName the name of field code tries to access to. Can be <code>null</code>
     * @return true if access granted
     */
    boolean canAccess(Class<?> clazz, Type type, String fieldName);

    enum Type {
        /**
         * Get form class
         */
        GET,
        /**
         * Get form {@link SharedStorageManager}
         */
        GET_EXTERNAL,
        /**
         * Set form class
         */
        SET,
        /**
         * Set from {@link SharedStorageManager}
         */
        SET_EXTERNAL,
        /**
         * Adding new filter
         */
        ADD_FILTER,
        /**
         * Clear all data(call from <code>SharedStorageManager.unregisterSharedStorage(name)</code>)
         */
        CLEAR
    }
}
