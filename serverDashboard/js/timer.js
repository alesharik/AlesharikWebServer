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
class Timer {
    constructor() {
        setInterval(() => {
            document.querySelectorAll('[data-timer="1"]').forEach((element) => {
                element.innerHTML = moment(element.getAttribute("data-time"), "YYYYMMDD-hh:mm").fromNow()
            });
        }, 10000);
        setInterval(() => {
            document.querySelectorAll("[data-clock='true']").forEach((element) => {
                element.innerHTML = moment().format("hh:mm:ss");
            })
        }, 1000);
    }

    update() {
        document.querySelectorAll('[data-timer="1"]').forEach((element) => {
            element.innerHTML = moment(element.getAttribute("data-time"), "YYYYMMDD-hh:mm").fromNow()
        });
        document.querySelectorAll("[data-clock='true']").forEach((element) => {
            element.innerHTML = moment().format("hh:mm:ss");
        });
    }
}