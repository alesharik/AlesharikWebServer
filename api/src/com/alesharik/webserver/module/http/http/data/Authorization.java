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

package com.alesharik.webserver.module.http.http.data;

import com.alesharik.webserver.api.Utils;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Getter
public class Authorization {
    protected final Authentication.Type type;
    protected final String credentials;

    public Authorization(@Nonnull Authentication.Type type, @Nonnull String credentials) {
        this.type = type;
        this.credentials = credentials == null ? "" : credentials;
    }

    /**
     * If the "Basic" authentication scheme is used, the credentials are constructed like this:<br>
     * The username and the password are combined with a colon (aladdin:opensesame).<br>
     * The resulting string is base64 encoded (YWxhZGRpbjpvcGVuc2VzYW1l).<br>
     */
    public String getCredentials() {
        return credentials;
    }

    @Getter
    public static class Credentials {
        protected final String login;
        protected final String password;

        public Credentials(String login, String password) {
            this.login = login;
            this.password = password;
        }

        @Nullable
        public static Credentials parseAuthorizationCredentials(String c) {
            return parseAuthorizationCredentials(c, StandardCharsets.UTF_8);
        }

        @Nullable
        public static Credentials parseAuthorizationCredentials(String c, Charset charset) {
            String decode = new String(Base64.getDecoder().decode(c), charset);
            int semicolonIndex = decode.indexOf(':');
            if(semicolonIndex == -1)
                return null;
            String[] divide = Utils.divideStringUnsafe(decode, semicolonIndex, 1);
            return new Credentials(divide[0], divide[1]);
        }
    }
}
