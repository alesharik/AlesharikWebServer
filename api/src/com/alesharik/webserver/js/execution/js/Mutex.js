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
 * This class represent a mutex of real threads
 * The java class is in this package named Mutex.java
 * @type {{Mutex}}
 */
Mutex = {};

/**
 * @readonly
 */
Mutex.prototype.constructor = function () {
};

/**
 * Lock the Mutex.
 * @throws IllegalMonitorStateException is mutex already locked by this thread
 * @readonly
 */
Mutex.prototype.lock = function () {
};

/**
 * Unlock the Mutex
 * @throws IllegalMonitorStateException if this thread not a owner thread
 * @readonly
 */
Mutex.prototype.unlock = function () {
};

/**
 * Return true if this thread own the Mutex
 * @return {boolean}
 * @readonly
 */
Mutex.prototype.isOwned = function () {
};

/**
 * Return true if mutex is locked
 * @return {boolean}
 * @readonly
 */
Mutex.prototype.isLocked = function () {
};