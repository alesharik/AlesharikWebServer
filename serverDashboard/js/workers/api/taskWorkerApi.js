class Task {
    /**
     * This function executes in TaskWorker
     * @param {Object} args arguments from caller
     * @return {Object|undefined} return value must be an object or undefined
     */
    execute(args) {
    }
}

class TaskWorkerExecuteMessage {
    /**
     * @param {Task} task
     * @param {Object|undefined} args
     */
    constructor(task, args) {
        if (task === null) //Task is null only in fromJson function
            return;

        this.type = "execute";
        this.task = "function " + Object.getOwnPropertyNames(task.__proto__)
                .filter((elem) => elem === "execute")
                .map((elem) => task[elem])
                .map((elem) => {
                    let str = elem.toString();
                    return str.substring(str.indexOf("("), str.length);
                })[0];
        this.args = args;
    }

    /**
     * @param {Object} obj
     * @return {TaskWorkerExecuteMessage}
     */
    static fromObject(obj) {
        let msg = new TaskWorkerExecuteMessage(null, null);
        msg.task = obj.task;
        msg.args = obj.args;
        msg.type = "execute";
        return msg;
    }

    /**
     * @throws {Error} from function
     */
    execute() {
        let args = this.task.match(/function\s.*?\(([^)]*)\)/)[1];
        args.split(',')
            .map((arg) => arg.replace(/\/\*.*\*\//, '').trim())
            .filter((arg) => arg);
        let body = this.task.replace(/function \(.*\) {/, '');
        body = body.substring(0, body.length - 1);
        let func = new Function(args, body);

        this.args === undefined ? func() : func(this.args);
    }
}

class TaskWorkerSubmitMessage {
    /**
     * @param {Task} task
     * @param {int} id
     * @param {Object|undefined} args
     */
    constructor(task, id, args) {
        if (task === null) //Task is null only in fromJson function
            return;

        this.type = "submit";
        this.task = "function " + Object.getOwnPropertyNames(task.__proto__)
                .filter((elem) => elem === "execute")
                .map((elem) => task[elem])
                .map((elem) => {
                    let str = elem.toString();
                    return str.substring(str.indexOf("("), str.length);
                })[0];
        this.id = id;
        this.args = args;
    }

    /**
     * @param {Object} obj
     * @return {TaskWorkerSubmitMessage}
     */
    static fromObject(obj) {
        let msg = new TaskWorkerSubmitMessage(null, 0, null);
        msg.task = obj.task;
        msg.id = obj.id;
        msg.args = obj.args;
        msg.type = "submit";
        return msg;
    }

    /**
     * @return {Object|undefined}
     * @throws {Error} from function
     */
    execute() {
        let args = this.task.match(/function\s.*?\(([^)]*)\)/)[1];
        args.split(',')
            .map((arg) => arg.replace(/\/\*.*\*\//, '').trim())
            .filter((arg) => arg);
        let body = this.task.replace(/function \(.*\) {/, '');
        body = body.substring(0, body.length - 1);
        let func = new Function(args, body);

        let result = (this.args === undefined) ? func() : func(this.args);

        if (result === undefined || result instanceof Object)
            return result;
        else
            console.log("[TaskWorker] Unexpected result of type " + typeof(result));
    }
}

class TaskWorkerSubmitSuccessMessage {
    /**
     * @param {int} id
     * @param {Object|undefined} result
     */
    constructor(id, result) {
        this.id = id;
        this.result = result;
        this.type = "submitSuccess";
    }
}

class TaskWorkerSubmitFailedMessage {
    /**
     * @param {int} id
     * @param {string} error
     */
    constructor(id, error) {
        this.id = id;
        this.error = error;
        this.type = "submitFailed";
    }
}