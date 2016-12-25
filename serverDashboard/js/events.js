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