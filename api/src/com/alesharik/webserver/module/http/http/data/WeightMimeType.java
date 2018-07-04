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

/**
 * Default weight is 1.0
 */
@Getter
public class WeightMimeType extends MimeType implements Weight<MimeType> {
    protected final float weight;

    public WeightMimeType(String type, String subType, float weight) {
        super(type, subType);
        this.weight = weight;
    }

    public WeightMimeType(String type, String subType) {
        super(type, subType);
        this.weight = 1.0F;
    }

    @Override
    public String toMimeType() {
        return super.toMimeType() + ((weight == 1.0) ? "" : ";q=" + weight);
    }

    @Nonnull
    public static WeightMimeType parseType(String str) {
        int slashPos = str.indexOf('/');
        if(slashPos == -1)
            throw new IllegalArgumentException(str + " is not a MIME type!");

        int semicolonPos = str.indexOf(';');
        if(semicolonPos == -1) {
            String[] parts = Utils.divideStringUnsafe(str, slashPos, 1);
            return new WeightMimeType(parts[0], parts[1], 1.0F);
        } else {
            String[] parts1 = Utils.divideStringUnsafe(str, slashPos, 1);
            String[] parts2 = Utils.divideStringUnsafe(parts1[1], semicolonPos - slashPos - 1, 3);
            return new WeightMimeType(parts1[0], parts2[0], Float.parseFloat(parts2[1]));
        }
    }

    @Nullable
    public static WeightMimeType parseTypeNullUnsafe(String str) {
        int slashPos = str.indexOf('/');
        if(slashPos == -1)
            return null;

        int semicolonPos = str.indexOf(';');
        if(semicolonPos == -1) {
            String[] parts = Utils.divideStringUnsafe(str, slashPos, 1);
            return new WeightMimeType(parts[0], parts[1], 1.0F);
        } else {
            String[] parts1 = Utils.divideStringUnsafe(str, slashPos, 1);
            String[] parts2 = Utils.divideStringUnsafe(parts1[1], semicolonPos - slashPos - 1, 3);
            return new WeightMimeType(parts1[0], parts2[0], Float.parseFloat(parts2[1]));
        }
    }
}
