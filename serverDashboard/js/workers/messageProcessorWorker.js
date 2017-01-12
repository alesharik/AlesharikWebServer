"use strict";
let messagingData = new Map();
onmessage = (e) => {
    let data = e.data;
    switch (data.cause) {
        case "add":
            /** @namespace data.msgType */
            addParser(data.msgType, data.parser);
            break;
        case "remove":
            removeParser(data.msgType, data.parser);
            break;
        case "parse":
            parse(data.msg);
            break;
        default:
            console.log("[Worker][MessagingProcessor]: Unexpected message: " + e.data);
    }
};

function addParser(message, parser) {
    let v = messagingData.get(message);
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
//Use type
function parse(message) {
    let parts = message.split(":");
    let v = messagingData.get(parts[0]);
    if (v == undefined || v.length == 0) {
        return;
    }
    let results = [];
    v.forEach((parser) => {
        // let func = eval(parser);
        let args = parser.match(/function\s.*?\(([^)]*)\)/)[1];
        args.split(',')
            .map((arg) => arg.replace(/\/\*.*\*\//, '').trim())
            .filter((arg) => arg);
        let body = parser.replace(/function \(.*\) {/, '');
        body = body.substring(0, body.length - 1);
        let func = new Function(args, body);
        results.push(func(message));
    });
    //noinspection JSUnresolvedFunction
    postMessage({
        cause: "postParse",
        results: results,
        msg: message
    });
}