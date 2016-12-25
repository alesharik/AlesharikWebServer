package com.alesharik.webserver.api;

import java.util.Objects;

/**
 * This class used for encode login and password
 */
public final class LoginPasswordCoder {
    private LoginPasswordCoder() {
    }

    /**
     * Encode login and password into one encoded string.
     * NOTE!If password and login have non-equals length then decode is difficult or impossible
     *
     * @param login    login to encode
     * @param password password to encode
     * @return encoded string
     */
    public static String encode(String login, String password) {
        Objects.requireNonNull(login);
        Objects.requireNonNull(password);

        StringBuilder sb = new StringBuilder();
        char[] log = login.toCharArray();
        char[] pass = password.toCharArray();
        for(int i = 0; i < Math.min(log.length, pass.length); i++) {
            sb.append(log[i]);
            sb.append(pass[i]);
        }

        char[] max;
        if(Integer.compare(Math.max(log.length, pass.length), log.length) == 0) {
            max = log;
        } else {
            max = pass;
        }

        for(int i = Math.min(log.length, pass.length); i < Math.max(log.length, pass.length); i++) {
            sb.append(max[i]);
        }
        return sb.toString();
    }

    /**
     * Check first logPass equals second logPass
     *
     * @param login    login to create first logPass
     * @param password password to create first logPass
     * @param logpass  second logPass
     * @return true if they are equals
     */
    public static boolean isEquals(String login, String password, String logpass) {
        return (login == null && password == null && logpass == null) || !(login == null || password == null || logpass == null) && encode(login, password).equals(logpass);
    }
}
