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

'use strict';
class Pattern {
    constructor() {
        this.parameters = {};
        this.pattern = "";
        this.regexp = new RegExp("\{&.*?\}");
    }

    setParameters(parameters) {
        console.log(parameters);
        this.parameters = parameters;
        return this;
    }

    setPattern(pattern) {
        this.pattern = pattern;
        return this;
    }

    build() {
        let line;
        while ((line = this.regexp.exec(this.pattern)) != null) {
            let value = this.parameters[line[0].substring(2, line[0].length - 1)];
            this.pattern = this.pattern.replace(this.regexp, value);
        }
        return this.pattern;
    }
}