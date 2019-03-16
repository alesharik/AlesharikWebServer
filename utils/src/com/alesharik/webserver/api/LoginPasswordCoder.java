/*
 *  This file is part of AlesharikWebServer.
 *
 *     AlesharikWebServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AlesharikWebServer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AlesharikWebServer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.alesharik.webserver.api;

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    @Nonnull
    public static String encode(@Nonnull String login, @Nonnull String password) {
        StringBuilder sb = new StringBuilder();
        char[] log = login.toCharArray();
        char[] pass = password.toCharArray();
        for(int i = 0; i < Math.min(log.length, pass.length); i++) {
            sb.append(log[i]);
            sb.append(pass[i]);
        }

        char[] max;
        if(Math.max(log.length, pass.length) == log.length)
            max = log;
        else
            max = pass;

        for(int i = Math.min(log.length, pass.length); i < Math.max(log.length, pass.length); i++)
            sb.append(max[i]);
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
        return (login == null && password == null && logpass == null)
                || (!(login == null || password == null || logpass == null)
                    && encode(login, password).equals(logpass));
    }
}
