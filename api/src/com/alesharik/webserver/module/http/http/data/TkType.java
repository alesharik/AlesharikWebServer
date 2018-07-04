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

import lombok.Getter;

import javax.annotation.Nullable;

public enum TkType {
    UNDER_CONSTRUCTION('!'),
    DYNAMIC('?'),
    GATEWAY('G'),
    NOT_TRACK('N'),
    TRACK('T'),
    TRACK_WITH_CONSENT('C'),
    POTENTIAL_CONSENT('P'),
    DISREGARDING_DNT('D'),
    UPDATED('U');

    @Getter
    private final char symbol;

    TkType(char symbol) {
        this.symbol = symbol;
    }

    @Nullable
    public static TkType parseType(char c) {
        for(TkType tkType : values()) {
            if(tkType.symbol == c)
                return tkType;
        }
        return null;
    }
}
