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