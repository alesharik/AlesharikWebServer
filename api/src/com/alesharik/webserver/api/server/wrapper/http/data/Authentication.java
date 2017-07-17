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

package com.alesharik.webserver.api.server.wrapper.http.data;

import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Getter
public class Authentication {
    protected final Type type;
    protected final String realm;

    public Authentication(@Nonnull Type type) {
        this(type, "");
    }

    public Authentication(@Nonnull Type type, @Nullable String realm) {
        this.type = type;
        this.realm = realm == null ? "" : realm;
    }

    public boolean hasRealm() {
        return !realm.isEmpty();
    }

    public enum Type {
        /**
         * <a href="https://tools.ietf.org/html/rfc7617">RFC</a>
         */
        BASIC("Basic"),
        /**
         * <a href="https://tools.ietf.org/html/rfc6750">RFC</a>
         */
        BEARER("Bearer"),
        /**
         * <a href="https://tools.ietf.org/html/rfc7616">RFC</a>
         */
        DIGEST("Digest"),
        /**
         * <a href="https://tools.ietf.org/html/rfc7486">RFC</a>
         */
        HOBA("HOBA"),
        /**
         * <a href="https://tools.ietf.org/html/rfc8120">RFC</a>
         */
        MUTUAL("Mutual"),
        /**
         * <a href="https://tools.ietf.org/html/rfc4559">RFC</a>
         */
        NEGOTIATE("Negotiate"),
        /**
         * <a href="https://tools.ietf.org/html/rfc5849">RFC</a>
         */
        OAUTH("OAuth"),
        /**
         * <a href="https://tools.ietf.org/html/rfc7804">RFC</a>
         */
        SCRAM_SHA_1("SCRAM-SHA-1"),
        /**
         * <a href="https://tools.ietf.org/html/rfc7804">RFC</a>
         */
        SCRAM_SHA_256("SCRAM-SHA-256");

        @Getter
        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Nullable
        public static Type parse(@Nonnull String s) {
            for(Type type : values()) {
                if(s.equals(type.name))
                    return type;
            }
            return null;
        }
    }
}
