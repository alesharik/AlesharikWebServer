package com.alesharik.webserver.api;

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * This class used for encode login and password
 */
@UtilityClass
public final class LoginPasswordCoder {
    /**
     * Encode login and password into one string. If password and login have non-equals length then decoding logPass can be impossible
     * @param login    login to encode
     * @param password password to encode
     * @return encoded string
     */
    public static String encode(@Nonnull String login, @Nonnull String password) {
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
     * Check first logPass equals second logPass. If all are <code>null</code>, then <code>true</code>. If any is <code>null</code> but not all then <code>false</code>
     * @param login    login to create first logPass
     * @param password password to create first logPass
     * @param logpass  second logPass
     * @return true if they are equals
     */
    public static boolean isEquals(@Nullable String login, @Nullable String password, @Nullable String logpass) {
        return (login == null && password == null && logpass == null) || !(login == null || password == null || logpass == null) && encode(login, password).equals(logpass);
    }
}
