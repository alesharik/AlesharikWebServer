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

"use strict";

/**
 * This class used for send requests on WebSocket. If WebSocket is not ready then sender add message to queue and if
 * WebSocket ready send all messages in queue
 */
class WebsocketSender {
    constructor() {
        this.queue = [];
        this.websocket = undefined;
    }

    /**
     * @param {WebSocket} websocket
     */
    useWebsocket(websocket) {
        this.websocket = websocket;
        this.websocket.onmessage = (event) => {
            //noinspection JSUnresolvedFunction
            postMessage({
                cause: "parse",
                msg: event.data
            });
        };

        this.websocket.onopen = () => {
            this.queue.forEach((message) => {
                this.websocket.send(message);
            })
        };
    }

    /**
     * @param {string} message
     */
    send(message) {
        if (this.websocket.readyState === 0) {
            this.queue.push(message);
        } else if (this.websocket.readyState === 1) {
            this.websocket.send(message);
        } else {
            throw new Error("Socket closed!");
        }
    }
}

let sender = new WebsocketSender();
let ready = false;

onmessage = (e) => {
    let data = e.data;
    if (data.cause === "message") {
        if (!ready) {
            sender.useWebsocket(new WebSocket("ws" + new RegExp(":\/{2}[^\/]*").exec(self.location.href) + "/dashboard"));
            ready = true;
        }

        try {
            sender.send(data.msg);
        } catch (e) {
            ready = false;
        }
    } else {
        console.log("[Worker][WebSocket]: Unexpected message: " + e.data);
    }
};