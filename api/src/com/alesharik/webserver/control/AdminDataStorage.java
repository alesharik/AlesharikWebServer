package com.alesharik.webserver.control;

import com.alesharik.webserver.configuration.Module;

/**
 * The AdminDataStorage used for hold dashboard login, password and data.
 * The data must be encrypted!
 */
public interface AdminDataStorage extends Module {
    /**
     * Return <code>true</code> if the login and password are correct
     *
     * @param login    the login to check
     * @param password the password to check
     */
    boolean check(String login, String password);

    /**
     * Return <code>true</code> if the login and password are correct
     *
     * @param logPass the login and password, encrypted by {@link com.alesharik.webserver.api.LoginPasswordCoder}
     */
    boolean check(String logPass);

    /**
     * Update login and password. Need old login and old password
     *
     * @param oldLogin    the old login
     * @param oldPassword the old password
     * @param newLogin    the new login
     * @param newPassword the new password
     */
    void updateLoginPassword(String oldLogin, String oldPassword, String newLogin, String newPassword);

    /**
     * Put an object into encrypted storage
     *
     * @param key   the key
     * @param value the object
     */
    void put(String key, Object value);

    /**
     * Return an object from encrypted storage
     *
     * @param key the key of object
     * @return the object or <code>null</code>
     */
    Object get(String key);

    /**
     * Remove the object from encrypted storage
     *
     * @param key the key
     */
    void remove(String key);

    /**
     * Return <code>true</code> if key exists in encrypted storage
     *
     * @param key the key
     */
    boolean contains(String key);
}
