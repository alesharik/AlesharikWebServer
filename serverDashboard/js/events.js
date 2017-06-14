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
class Events {
    constructor() {
        this._events = new Map();
    }

    /**
     * @param {string} name
     * @param {object} params
     * @return {Event}
     */
    newEvent(name, params = {}) {
        let event = new Event(params);
        this._events.set(name, event);
        return event;
    }

    /**
     * @param {string} name
     * @param {object} params
     */
    sendEvent(name, params = {}) {
        let event = this._events.get(name);
        if (event) {
            event.onEvent(params);
        }
    }

    /**
     * @param {string} name
     * @param {function} listener
     */
    addEventListener(name, listener) {
        let event = this._events.get(name);
        if (event) {
            event.addEventListener(listener);
        }
    }

    /**
     * @param {string} name
     * @param {function} listener
     */
    addEventListenerOnce(name, listener) {
        let event = this._events.get(name);
        if (event) {
            event.addEventListener(() => {
                listener();
                event.removeEventListener(this);
            });
        }
    }

    /**
     * @param {string} name
     * @param {function} listener
     */
    removeEventListener(name, listener) {
        let event = this._events.get(name);
        if (event) {
            event.removeEventListener(listener);
        }
    }

    /**
     * @param {string} name
     * @return {Event}
     */
    getEvent(name) {
        return this._events.get(name);
    }
}

class Event {
    /**
     * @param {object} params
     */
    constructor(params) {
        this.params = params;
        this._listeners = [];
    }

    /**
     * This method used only in Events class. Do not use or set it!
     * @param {object} params
     */
    onEvent(params) {
        this._listeners.forEach((listener) => listener(params));
    }

    /**
     * @param {function} listener
     */
    addEventListener(listener) {
        this._listeners.push(listener);
    }

    /**
     * @param {function} listener
     */
    removeEventListener(listener) {
        this._listeners.splice(this._listeners.indexOf(listener), 1);
    }

    clear() {
        this._listeners = [];
    }
}

var events = new Events();