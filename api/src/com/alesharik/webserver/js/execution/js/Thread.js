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

/**
 * This class used for create, start and stop real threads in JavaScript code.
 * Java class located in this folder and named JSThread.java
 * @type {{Thread}}
 */
Thread = {};

/**
 * Create new thread
 * @param {function} runnable the function to run in other thread
 * @param {object} sharedStorage the shared storage: this storage is one for all threads. Used in synchronization and value exchange
 * @readonly
 */
Thread.prototype.constructor = function (runnable, sharedStorage) {
};

/**
 * Start real thread
 * @readonly
 */
Thread.prototype.start = function () {
};

/**
 * The main function. It runs in another thread.
 * @readonly
 */
Thread.prototype.run = function () {
};

/**
 * Interrupt the real thread
 * @readonly
 */
Thread.prototype.interrupt = function () {
};

/**
 * @return {boolean} is thread running
 * @readonly
 */
Thread.prototype.isRunning = function () {
};

/**
 * Return the shared storage
 * @readonly
 * @return {object}
 */
Thread.prototype.getSharedStorage = function () {
};

/**
 * Set thread name
 * @param {string} name
 */
Thread.prototype.setName = function (name) {
};

/**
 * Return thread name
 * @return {string} thread name
 */
Thread.prototype.getName = function () {
};


/**
 * Set thread daemon
 * @param {boolean} is is daemon
 */
Thread.prototype.setDaemon = function (is) {
};

/**
 * Return true if this thread is a daemon
 * @return {boolean} is this thread a daemon
 */
Thread.prototype.getDaemon = function () {
};