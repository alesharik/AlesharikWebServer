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
import lombok.ToString;

@ToString
public enum ReferrerPolicy {
    NO_REFERRER("no-referrer"),
    NO_REFERRER_WHEN_DOWNGRADE("no-referrer-when-downgrade"),
    ORIGIN("origin"),
    ORIGIN_WHEN_CROSS_ORIGIN("origin-when-cross-origin"),
    SAME_ORIGIN("same-origin"),
    STRICT_ORIGIN("strict-origin"),
    STRICT_ORIGIN_WHEN_CROSS_ORIGIN("strict-origin-when-cross-origin"),
    UNSAFE_URL("unsafe-url");

    @Getter
    private final String name;

    ReferrerPolicy(String name) {
        this.name = name;
    }

    /**
     * Default value is {@link ReferrerPolicy#NO_REFERRER}
     */
    public static ReferrerPolicy parseString(String s) {
        for(ReferrerPolicy referrerPolicy : values()) {
            if(referrerPolicy.name.equals(s))
                return referrerPolicy;
        }
        return ReferrerPolicy.NO_REFERRER;
    }
}
