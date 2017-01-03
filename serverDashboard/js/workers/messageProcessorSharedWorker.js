"use strict";
let messagingData = new Map();
onconnect = (e) => {
    /** @namespace e.ports */
    let port = e.ports[0];

    port.addEventListener('message', (e) => {
        if (e.data.cause != undefined) {
            if (e.data.cause == "add") {
                addParser(e.data.message, e.data.parser);
            } else if (e.data.cause == "remove") {
                removeParser(e.data.message, e.data.parser);
            }
        } else {
            parse(e.data.message, port);
        }
    });

    port.start();
};

function addParser(message, parser) {
    let v = messagingManagerData.get(message);
    if (v == undefined) {
        v = [];
        messagingData.set(message, v);
    }
    v.push(parser);
}

/**
 * @param {string} message
 * @param {Parser} parser
 */
function removeParser(message, parser) {
    let v = messagingData.get(message);
    if (v == undefined) {
        return;
    }
    v.splice(v.indexOf(parser), 1);
}

function parse(message, port) {
    let v = messagingData.get(message);
    if (v == undefined || v.length == 0) {
        return;
    }
    let results = [];
    v.forEach((parser) => {
        results.push(parser.parse(message));
    });
    port.postMessage([message, results]);
}