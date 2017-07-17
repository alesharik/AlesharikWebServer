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

import com.alesharik.webserver.api.server.wrapper.http.header.AcceptCharsetHeader;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.nio.charset.Charset;

/**
 * Used in {@link AcceptCharsetHeader}. Default weight is 1
 */
@ToString
@EqualsAndHashCode
@Getter
@Immutable
public class WeightCharset implements Weight<Charset> {
    protected final Charset charset;
    protected final float weight;

    public WeightCharset(Charset charset, float weight) {
        this.charset = charset;
        this.weight = weight;
    }

    public WeightCharset(Charset charset) {
        this.charset = charset;
        this.weight = 1;
    }

    /**
     * If charset equals null, then this {@link WeightCharset} is AnyCharset
     */
    @Nullable
    public Charset getCharset() {
        return charset;
    }

    @Nonnull
    public static WeightCharset anyCharset(float weight) {
        return new WeightCharset(null, weight);
    }

    public static boolean isAnyCharset(WeightCharset charset) {
        return charset.getCharset() == null;
    }
}
