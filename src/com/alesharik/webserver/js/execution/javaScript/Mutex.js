/**
 * This class represent a mutex of real threads
 * The java class is in this package named Mutex.java
 * @type {{Mutex}}
 */
var Mutex = {};

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