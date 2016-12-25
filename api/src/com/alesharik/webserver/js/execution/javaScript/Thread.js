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
 * @return {boolean} is thread interrupted or not running
 * @readonly
 */
Thread.prototype.isInterrupted = function () {
};

/**
 * Return the shared storage
 * @readonly
 * @return {object}
 */
Thread.prototype.getSharedStorage = function () {
};