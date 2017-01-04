"use strict";

/**
 * This class used for send requests on WebSocket. If WebSocket is not ready then sender add message to queue and if
 * WebSocket ready send all messages in queue
 */
class WebsocketSender {
    constructor() {
        this.startQueue = [];
    }

    /**
     * @param {WebSocket} websocket
     */
    useWebsocket(websocket) {
        this.websocket = websocket;
        this.websocket.onmessage = (event) => {
            if (webSocketMessageProcessor != undefined) {
                webSocketMessageProcessor.postMessage({
                    message: event.data
                });
            }
        };

        let that = this;
        this.websocket.onopen = () => {
            that.startQueue.forEach((message) => {
                that.websocket.send(message);
            })
        };
    }

    /**
     * @param {string} message
     */
    send(message) {
        if (this.websocket.readyState == 0) {
            this.startQueue.push(message);
        } else if (this.websocket.readyState == 1) {
            this.websocket.send(message);
        } else {
            throw new Error("Socket closed!");
        }
    }
}

let webSocketSender = new WebsocketSender();
let webSocketWorkerReady = false;
let webSocketMessageProcessor = undefined;

onmessage = (e) => {
    if (e.data.cause == "init") {
        /** @namespace e.data.processor */
        webSocketMessageProcessor = e.data.processor;
    } else {
        if (!webSocketWorkerReady) {
            webSocketSender.useWebsocket(new WebSocket("ws" + new RegExp("://.*/").exec(document.location.href) + "dashboard"));
            webSocketWorkerReady = true;
        } else {
            try {
                webSocketSender.send(e.data);
            } catch (e) {
                webSocketWorkerReady = false;
            }
        }
    }
};