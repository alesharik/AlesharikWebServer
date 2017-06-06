'use strict';

importScripts('js/workers/api/taskWorkerApi.js');

onmessage = (message) => {
    let data = message.data;
    if (data.type === "execute") {
        executeTask(data);
    } else if (data.type === "submit") {
        submitTask(data);
    } else {
        console.log("Unexpected TaskWorkerMessage type: " + data.type);
    }
};

/**
 *
 * @param {Object} data
 */
function executeTask(data) {
    let msg = TaskWorkerExecuteMessage.fromObject(data);
    try {
        msg.execute();
    } catch (err) {
        console.log("[TaskWorker] " + err.name + ": " + err.message);
        console.log("[TaskWorker] " + err.stack);
    }
}

/**
 *
 * @param {Object} data
 */
function submitTask(data) {
    let msg = TaskWorkerSubmitMessage.fromObject(data);
    try {
        let result = msg.execute();
        //noinspection JSUnresolvedFunction
        postMessage(new TaskWorkerSubmitSuccessMessage(msg.id, result));
    } catch (err) {
        //noinspection JSUnresolvedFunction
        postMessage(new TaskWorkerSubmitFailedMessage(msg.id, err.name + ": " + err.message + '\n' + err.stack));
    }
}