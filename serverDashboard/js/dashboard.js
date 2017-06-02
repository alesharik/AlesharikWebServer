'use strict';
//====================Message api====================\\

/**<pre>
 *                                                       ┌────────────MessagingManager──────────────┐
 *                                                       ∨                   │                      │
 *                                                 send(message)             ∨                      ∨
 *                                                       │            add/remove parser       postMessage(result)────────┐
 *                                                       ∨                      │                             ∧          │
 * (receive the message)<═══════════════════(send the message)                  │                             └────┐     │
 *      Backend                               WebSocketWorker                   │ (add/remove parser message)      │     │
 *  (send message)════════════════════════>(receive the message)───────┐        │                                  │     │
 *                                                                     │        │                                  │     │
 *                                                                     ∨        ∨                                  │     │
 *                                                               MessageProcessor                 the result       │     │
 *                                                                   (Worker)           ┌──────────────────────────┘     │
 *                                                         (find parser for message)    │                                │
 *                                                                     │                │                                │
 *                                                                     │                │                                │
 *                                                                     ∨                |                                ∨
 *                                                                   Parser.parse ──────┘                          Parser.postParse
 *                                                   (Parse the message in MessageProcessor thread)           (executes in main thread)
 *                                                           (send result to main thread)
 * </pre>
 */
/**
 * Websocket send message(msg - message to send): <code>
 * {
 *      cause: "message",
 *      msg: "msg"
 * }
 * </code>
 *
 * Message processor worker add parser message(parser - Parser to add, msgType - msg type of parser): <code>
 * {
 *      cause: "add",
 *      msgType: msgType,
 *      parser: parser
 * }
 * </code>
 *
 * Message processor worker add parser message(parser - Parser to add, msgType - msg type of parser): <code>
 * {
 *      cause: "remove",
 *      msgType: msgType,
 *      parser: parser
 * }
 * </code>
 *
 * Message processor worker parse msg message(msg - message): <code>
 * {
 *      cause: "parse",
 *      msg: msg
 * }
 * </code>
 *
 * Message processor worker postParse msg message(results - the results of parse stage, msg - message): <code>
 * {
 *      cause: "postParse",
 *      results: results,
 *      msg: msg
 * }
 * </code>
 */

let messagingManagerData = new Map();

let messageProcessorWorker = new Worker("js/workers/messageProcessorWorker.js");
let webSocketWorker = new Worker("js/workers/webSocketWorker.js");

webSocketWorker.addEventListener("message", (e) => {
    messageProcessorWorker.postMessage(e.data);
});

messageProcessorWorker.addEventListener("message", (e) => {
    let data = e.data;
    if (data.cause == "postParse") {
        MessagingManager.postMessage(data.msg, data.results);
    } else {
        console.log("[MessagingManager] Unexpected message: " + e.data);
    }
});

// messageProcessorWorker.port.start();
// messageProcessorWorker.port.addEventListener("message", e => {
//     let data = JSON.parse(e.data);
//     // MessagingManager.postMessage(e.data[0], e.data[1]);
//     if(data.cause == "postParse") {
//         MessagingManager.postMessage(data.msg, data.results);
//     } else {
//         console.log("[MessagingManager] Unexpected message: " + e.data);
//     }
// }, false);
//
// webSocketWorker.postMessage({
//     cause: "init",
//     processor: messageProcessorWorker.port
// }, [messageProcessorWorker.port]);
//
// messageProcessorWorker.port.postMessage(JSON.stringify({
//     cause: "none"
// }));

class MessagingManager {
    /**
     * @param {string} messageType the message type
     * @param {Parser} parser
     */
    static addParser(messageType, parser) {
        let v = messagingManagerData.get(messageType);
        if (v == undefined) {
            v = [];
            messagingManagerData.set(messageType, v);
        }
        v.push(parser);

        messageProcessorWorker.postMessage({
            cause: "add",
            msgType: messageType,
            parser: "function " + Object.getOwnPropertyNames(parser.__proto__)
                .filter((elem) => elem == "parse")
                .map((elem) => parser[elem])
                .map((elem) => {
                    let str = elem.toString();
                    return str.substring(str.indexOf("("), str.length);
                })[0]
        });
    }

    /**
     * @param {string} messageType the message type
     * @param {Parser} parser
     */
    static removeParser(messageType, parser) {
        let v = messagingManagerData.get(messageType);
        if (v == undefined) {
            return;
        }
        v.splice(v.indexOf(parser), 1);

        messageProcessorWorker.postMessage({
            cause: "remove",
            msgType: messageType,
            parser: "function " + Object.getOwnPropertyNames(parser.__proto__)
                .filter((elem) => elem == "parse")
                .map((elem) => parser[elem])
                .map((elem) => {
                    let str = elem.toString();
                    return str.substring(str.indexOf("("), str.length);
                })[0]
        });
    }

    static sendMessage(message) {
        webSocketWorker.postMessage({
            cause: "message",
            msg: message
        });
    }

    /**
     * DO NOT USE THIS METHOD
     * @param {string} message
     * @param result
     */
    static postMessage(message, result) {
        let v = messagingManagerData.get(message.split(":")[0]);
        if (v == undefined || v.length == 0) {
            return;
        }
        v.forEach((parser, i) => {
            parser.postParse(result[i]);
        })
    }
}

/**
 * This class used for parse message
 */
class Parser {
    /**
     * This method parse message in MessageProcessor WORKER THREAD and return result of parsing.
     * YOU CANNOT USE DOM, CANVAS, ETC.
     * YOU CANNOT SEND LINKS IN RESULT.
     * YOU CANNOT USE EXTERNAL FUNCTIONS, VARS, CLASSES, ETC
     * @param {string} result result of preParse
     */
    parse(result) {
    }

    /**
     * This method executes after parse() in MessageProcessor. It executes in main thread.
     * @param result
     */
    postParse(result) {
    }
}

let computerInfoEnabled = false;

class MessagingUtils {
    static updateMenu() {
        MessagingManager.sendMessage("menu:update");
    }

    static updateMenuPlugins() {
        MessagingManager.sendMessage("menuPlugins:get");
    }

    static disableRecievengComputerInfo() {
        if (computerInfoEnabled) {
            MessagingManager.sendMessage("currentCompInfo:stop");
            computerInfoEnabled = false;
        }
    }


    static enableReceivingComputerInfo() {
        if (!computerInfoEnabled) {
            MessagingManager.sendMessage("currentCompInfo:start");
            computerInfoEnabled = true;
        }
    }

    static performGC() {
        MessagingManager.sendMessage("system:gc");
    }
}

//====================Message api end====================\\

//====================Computer info====================\\

class ComputerInfo extends Parser {
    constructor() {
        super();

        this.cpuCount = 0;
        this.cpuLoad = [];
        this.ram = [];
        this.partitions = [];
        this.java = {};
        this.processorCount = 0;
        this.ok = false;
    }

    /**
     * @param {string} string
     */
    parse(string) {
        let split = string.split(":");

        let parts = [];
        parts.push(split[0]);
        parts.push(split[1]);
        parts.push(split.slice(2, split.length).join(":"));

        if (parts[1] == "set") {
            let json = JSON.parse(parts[2]);
            let ret = {};
            ret.processorCount = json.processorCount;
            ret.cpuCount = json.cpuCount;
            ret.cpuLoad = [];
            for (let i = 0; i < ret.cpuCount; i++) {
                ret.cpuLoad[i] = json["cpu" + i];
            }
            ret.ram = json.ram;
            ret.partitions = json.partitions;
            // json.partitions.forEach((part) => {
            //     this.partitions.push(Partition.parse(part));
            // });
            ret.java = json.java;
            return ret;
        } else {
            console.log("[ComputerInfo] Unexpected message: " + message);
            return {};
        }
    }

    postParse(result) {
        this.ok = true;

        this.processorCount = result.processorCount;
        this.cpuCount = result.cpuCount;
        this.cpuLoad = result.cpuLoad;
        this.ram = result.ram;
        this.partitions = [];
        result.partitions.forEach((part) => {
            this.partitions.push(Partition.parse(part));
        });
        this.java = result.java;
    }

    /**
     * @param {int} cpu
     * @param {string} type:
     * user: normal processes executing in user mode
     * nice: nice processes executing in user mode
     * system: processes executing in kernel mode
     * idle: twiddling thumbs
     * iowait: waiting for I/O to complete
     * irq: servicing interrupts
     * softirq: servicing softirqs
     */
    getCoreLoad(cpu, type) {
        switch (type) {
            case "user":
                return this.cpuLoad[cpu][0];
            case "nice":
                return this.cpuLoad[cpu][1];
            case "system":
                return this.cpuLoad[cpu][2];
            case "idle":
                return this.cpuLoad[cpu][3];
            case "iowait":
                return this.cpuLoad[cpu][4];
            case "irq":
                return this.cpuLoad[cpu][5];
            case "softirq":
                return this.cpuLoad[cpu][6];
            case "all":
                return this.cpuLoad[cpu][7];
        }
    }
}

class Partition {
    constructor(name, addr, type, max, free, inodes, inodesFree) {
        this.name = name;
        this.address = addr;
        this.type = type;
        this.maxSize = max;
        this.usedSize = max - free;
        this.freeSize = free;
        this.inodes = inodes;
        this.inodesFree = inodesFree;
    }

    /**
     * @param {object} json
     * @return {Partition}
     */
    static parse(json) {
        return new Partition(json.name, json.addr, json.type, json.max, json.free, json.inodes, json.inodesFree);
    }
}

//====================Computer info end====================\\

//====================Side menu====================\\

/**
 * This is a basic menu item, you need to extend from it to create new Menu Item types
 */
class MenuItem {
    constructor(fa, text) {
        this.fa = fa;
        this.text = text;
        this.type = "none";
    }

    /**
     * Serialize this object
     * @return {string}
     */
    serialize() {
        return JSON.stringify(this);
    }

    /**
     * Deserialize the object.
     * @param {object} json
     * @return {MenuItem} new instance
     */
    deserialize(json) {
        let item = new MenuItem(json.fa, json.text);
        item.type = json.type;
        return item;
    }

    /**
     * This function return html code, which will be inserted into menu in item container
     * @return {string}
     */
    toHtml() {
        return "";
    }

    /**
     * Handle muse click
     * @param {MouseEvent} event
     */
    handleClick(event) {
    }
}

/**
 * This class store all MenuItems. Used for get the plugin for type.
 */
let itemManagerItems = new Map();
class MenuItemManager {

    constructor() {
        throw new Error("Can't initialize this class")
    }

    /**
     * @param {MenuItem} item
     */
    static registerNewItem(item) {
        itemManagerItems.set(item.type, item);
    }

    /**
     * @param {MenuItem} item
     */
    static unregisterItem(item) {
        itemManagerItems.delete(item.type);
    }

    /**
     * @param {string} type
     * @return {MenuItem}
     */
    static getItemForType(type) {
        return itemManagerItems.get(type);
    }
}


/**
 * This class is a abstraction on MenuTextItem in backend and used in Navigator.
 * Better use this class to parse json from backend.
 * Json sample code:
 * {
 *      "type": "text",
  *     "fa": "dashboard",
  *     "text": "Dashboard",
  *     "contentId": "dashboard"
 * }
 */
class TextMenuItem extends MenuItem {
    /**
     * @param {string} contentId
     * @param {string} fa
     * @param {string} text
     */
    constructor(contentId, fa, text) {
        super(fa, text);
        this.type = "text";
        this.contentId = contentId;
    }

    /**
     * Deserialize the object.
     * @param {object} json
     * @return {MenuItem} new instance
     */
    deserialize(json) {
        return new TextMenuItem(json.contentId, json.fa, json.text);
    }

    /**
     * @return {string}
     */
    toHTML() {
        let pattern = "<li>" +
            "<a data-contentID='{&contentId}' onclick='handleMenuItemClick(event, \"text\")' role='button'>" +
            "<i class='fa fa-{&fa} fa-fw'></i>" +
            "{&text}</a>" +
            "</li>";
        return new Pattern().setPattern(pattern).setParameters(this).build();
    }

    /**
     * @return {TextMenuItem} empty item
     */
    static empty() {
        return new TextMenuItem("", "", "");
    }

    handleClick(event) {
        dashboard.navigator.showContent(event.target || event.srcElement);
    }
}

MenuItemManager.registerNewItem(TextMenuItem.empty());

/**
 * This class is a abstraction on MenuDropdown in backend and used in Navigator.
 * Better use this class to parse json from backend.
 * Sample json code:
 * {
 *     "type": "dropdown",
 *     "fa": "servers",
 *     "text": "Servers",
 *     "items": [
 *          {
 *              "type": "text",
 *              "fa": "add",
 *              "text": "Add server",
 *              "contentId": "addServer"
 *          }
 *     ]
 * }
 */
class MenuDropdown extends MenuItem {
    /**
     * @param {string} fa
     * @param {string} text
     * @param {TextMenuItem[]} items
     */
    constructor(fa, text, items) {
        super(fa, text);
        this.type = "dropdown";
        this.items = items;
    }

    /**
     * Deserialize the object.
     * @param {object} json
     * @return {MenuItem} new instance
     */
    deserialize(json) {
        let items = [];
        json.items.forEach((item) => {
            items.push(MenuItemManager.getItemForType(item.type).deserialize(item));
        });
        return new MenuDropdown(json.fa, json.text, items);
    }

    /**
     * @param {int} depth
     * @return {string}
     */
    toHTML(depth) {
        //Because max depth is 3 it break on 3 depth(0, 1, 2)
        if (depth > 1) {
            return "";
        }
        depth++;

        let pattern = "<li>" +
            "<a href='#'>" +
            "<i class='fa fa-{&fa} fa-fw'></i>" +
            "{&text}<span class='fa arrow'></span></a>" +
            "<ul class='nav " + ((depth == 0) ? "nav-second-level" : "nav-third-level") + "'>";

        this.items.forEach((item) => {
            if (item.type == "item") {
                pattern += item.toHTML();
            } else {
                pattern += item.toHTML(depth);
            }
        });

        pattern += "</ul>" +
            "</li>";
        return new Pattern().setPattern(pattern).setParameters(this).build();
    }

    /**
     * Search element or dropdown
     * @param {string} text
     * @return {Array} search result
     */
    search(text) {
        let result = [];

        this.items.forEach((item) => {
            if (item.text != undefined) {
                if (item.text.indexOf(text) != -1) {
                    result.push(item);
                }
            }
            if (item.type == "dropdown") {
                result = result.concat(item.search(text));
            }
        });

        return result;
    }

    /**
     * Return new empty dropdown
     * @return {MenuDropdown}
     */
    static empty() {
        return new MenuDropdown("", "", []);
    }
}

MenuItemManager.registerNewItem(MenuDropdown.empty());

/**
 * This class used for works with side menu.
 * The instance of item created by item.deserialize
 */
class Navigator extends Parser {
    /**
     * @param {ContentLoader} contentLoader
     */
    constructor(contentLoader) {
        super();
        this.contentLoader = contentLoader;
        this.items = [];
        this.hasSearchModeOn = false;

        this.navigator = document.querySelector("#navigator");
        this.searchElement = document.querySelector("#navigatorSearch");
        this.searchList = this.searchElement.querySelector("ul.list-group");

        this.searchElementCode = "<li class=\'sidebar-search\'>" +
            "<div class=\'input-group custom-search-form\'>" +
            "<input type=\'text\' class=\'form-control menuSearchInputElement\' placeholder=\'Search...\' onkeydown=\'handleMenuSearchInput(event)\'>" +
            "<span class=\'input-group-btn\'>" +
            "<button class=\'btn btn-default\' type=\'button\' onclick=\'searchInMenu(document.querySelectorAll(\".menuSearchInputElement\")[1])\'>" +
            "<i class=\'fa fa-search\'></i>" +
            "</button>" +
            "</span>" +
            "</div>" +
            "</li>";
        this.didNotFindCode = "<div class='text-center'>" +
            "<strong>Your search did not match any items.</strong>" +
            "</div>";
        this.searchItemCode = "<li class='list-group-item'>" +
            "<a data-contentID='{&contentId}' onclick='handleMenuItemClick(event)' role='button' style='cursor: pointer'>" +
            "<i class='fa fa-{&fa} fa-fw'></i>" +
            "{&text}</a>" +
            "</li>";
    }

    // /**
    //  * @param {string} jsonString
    //  */
    // parse(jsonString) {
    //     let json = JSON.parse(jsonString);
    //     let items = this.items;
    //     json.items.forEach((item) => {
    //         items.push(MenuItemManager.getItemForType(item.type).deserialize(item));
    //     });
    //     this.renderMenu();
    // }


    parse(message) {
        let split = message.split(":");

        let parts = [];
        parts.push(split[0]);
        parts.push(split[1]);
        parts.push(split.slice(2, split.length).join(":"));
        if (parts[1] == "set") {
            let json = JSON.parse(parts[2]);
            return {cause: "set", items: json.items};
        } else if (parts[1] == "render") {
            return {cause: "render"};
        } else {
            console.log("[Navigator] Unexpected message:" + message);
            return {};
        }
    }

    postParse(result) {
        if (result.cause == "set") {
            this.items = [];
            result.items.forEach((item) => {
                this.items.push(MenuItemManager.getItemForType(item.type).deserialize(item));
            });

            this.renderMenu();
        } else if (result.cause == "render") {
            this.renderMenu();
        }
    }

    /**
     * Clear menu on call!
     */
    renderMenu() {
        this.navigator.innerHTML = this.searchElementCode;
        this.items.forEach((item) => {
            this.navigator.innerHTML += item.toHTML(0);
        });

        $('#navigator').metisMenu();
    }

    /**
     * Search text in elements, switch to search mode and render result
     * @param {string} text
     */
    search(text) {
        if (!this.hasSearchModeOn) {
            this.toggleSearchMode();
        }

        //Set text of all search elements
        document.querySelectorAll(".menuSearchInputElement").forEach((element) => {
            element.value = text;
        });

        let result = [];
        //noinspection JSCheckFunctionSignatures
        this.items.forEach((item) => {
            if (item.text != undefined) {
                if (item.text.indexOf(text) != -1) {
                    result.push(item);
                }
            }
            if (item.type == "dropdown") {
                result = result.concat(item.search(text));
            }
        });

        if (result.length > 0) {
            this.renderSearchList(result);
        } else {
            this.searchList.innerHTML = this.didNotFindCode;
        }
    }

    /**
     * Render search list. Do not enable search mode!
     * @param list
     */
    renderSearchList(list) {
        let that = this;
        that.searchList.innerHTML = "";
        list.forEach((item) => {
            that.searchList.innerHTML += new Pattern().setPattern(that.searchItemCode).setParameters(item).build();
        });
    }


    toggleSearchMode() {
        if (!this.hasSearchModeOn) {
            this.navigator.parentNode.style.height = "0";
            this.searchElement.style.height = "";
            this.hasSearchModeOn = true;
        } else {
            this.navigator.parentNode.style.height = "";
            this.searchElement.style.height = "0";
            this.hasSearchModeOn = false;
        }
    }

    /**
     * Try to load content with ContentLoader and select element
     * @param {*} target
     */
    showContent(target) {
        if (this.hasSearchModeOn) {
            this.toggleSearchMode();
        }
        target.parentNode.parentNode.parentNode.querySelectorAll(".active").forEach((element) => element.classList.remove("active"));

        this.contentLoader.load(target.getAttribute("data-contentid"));

        let parent = target.parentNode.parentNode;
        parent.classList.add("in");
        target.classList.add("active");
        let element = parent.parentNode;
        if (element.tagName == "LI") {
            element.classList.add("active");
        }
    }

    /**
     * Remove all elements form menu and render it
     */
    clear() {
        this.items = [];
        this.renderMenu();
    }
}
//====================Side menu end====================\\
//====================Content loading====================\\

/**
 * This class used for load content form server to #contentHolder
 * To load CSS or JS you need to add <meta type="extension" href="href-to-your-file"> in head of loaded html.
 * This code load href-to-your-file with dashboard.loadCSSFile() or dashboard.loadJSFile(). All css is auto selected, all
 * js is auto started!
 * There is 2 events:
 * 1. loadingContentEnded - dispatch on loader finish loading data
 * 2. finalizeContentScript - dispatch on loader remove all resources
 */
class ContentLoader {
    constructor() {
        this.contentHolder = document.querySelector("#contentHolder");
        this.header = document.querySelector("#contentHeader");

        /**
         * Loaded head elements
         * @type {Array}
         */
        this.elements = [];

        events.newEvent("finalizeContent");
        events.newEvent("loadingContentEnded");
    }

    /**
     * @param {string} id
     */
    load(id) {
        events.sendEvent("finalizeContent"); //Request content finalization

        //Remove head elements
        this.elements.forEach((element) => {
            element.parentNode.removeChild(element);
        });
        this.elements = [];

        //Request new page
        let request = new XMLHttpRequest();
        request.open("GET", "/content/" + id + ".html");
        request.onreadystatechange = () => {
            if (request.readyState === 4) {
                if (request.status !== 200) { //Can't load page
                    this.loadError(request.status);
                } else { //Page loaded

                    let headers = new RegExp("<title>(.*?)</title>").exec(request.responseText); //Extract title
                    if (headers === null) { //Page doesn't have <title> tag
                        this.header.innerHTML = id;
                    } else { //Page has <title> tag
                        this.header.innerHTML = headers[1]; //Load first <title> tag
                    }

                    this.contentHolder.innerHTML = request.responseText.replace(/<link rel="stylesheet" .*/, ""); //Delete all invalid linked stylesheets
                    //and put result in content holder

                    /**
                     * <meta type="extension"> tags
                     * @type {Array}
                     */
                    let extensions = [];
                    /**
                     *
                     * @type {string}
                     */
                    let text = this.contentHolder.innerHTML;

                    /**
                     * Temporary storage for extension tag extraction
                     * @type {Array}
                     */
                    let ext;
                    while ((ext = /<meta type="extension".*>/.exec(text)) !== null) { //While document has <meta type="extension"> tags
                        extensions.push(ext[0]); //Add tag to array
                        text = text.replace(ext[0], ""); //Remove tag from text
                    }

                    if (extensions.length > 0) { //Document has extensions to load
                        /**
                         * Extensions, which require step loading
                         * @type {Array}
                         */
                        let stepExtensions = [];
                        extensions.forEach((extension) => {
                            /**
                             * @type {string}
                             */
                            let href = extension.substring(extension.indexOf("href=\"") + 6, extension.lastIndexOf("\">")); //Get href of extension

                            //Is extension require step loading
                            /**
                             * @type {boolean}
                             */
                            let isStepLoading = (extension.indexOf("steploading=\"true\"") !== -1 || extension.indexOf("stepLoading=\"true\"") !== -1);
                            /**
                             * @type {boolean}
                             */
                            let isJavaScript = href.lastIndexOf(".css") === -1;
                            if (isJavaScript) {
                                if (isStepLoading) {
                                    stepExtensions.push(href);
                                } else {
                                    dashboard.loadJSFile(href).then((e) => {
                                        this.elements.push(e);
                                        if (extensions.length === this.elements.length) {
                                            events.sendEvent("loadingContentEnded");
                                        }
                                    });
                                }
                            } else { //Do not support step loading for css files
                                dashboard.loadCSSFile(href).then((e) => {
                                    this.elements.push(e);
                                    if (extensions.length === this.elements.length) {
                                        events.sendEvent("loadingContentEnded");
                                    }
                                });
                            }
                        });

                        if (stepExtensions.length > 0) {
                            let loader = (element) => {
                                this.elements.push(element);
                                let elem = stepExtensions.shift();
                                if (elem !== undefined) {
                                    dashboard.loadJSFile(elem).then(loader);
                                } else if (extensions.length === this.elements.length) { //All extensions loaded
                                    events.sendEvent("loadingContentEnded");
                                }
                            };
                            dashboard.loadJSFile(stepExtensions.shift()).then(loader);
                        }
                    } else { //Document doesn't have eny extensions
                        events.sendEvent("loadingContentEnded"); //Fire "document load complete" event
                    }
                }
            }
        };
        request.send();
    }

    /**
     * Load error and display it
     * @param {int} errorCode
     * @param {ContentLoader} contentLoader
     */
    static staticLoadError(errorCode, contentLoader) {
        let request = new XMLHttpRequest();
        request.open("GET", "/errors/" + errorCode);
        request.onreadystatechange = function () {
            if (request.readyState === 4) {
                if (request.status !== 200) {
                    contentLoader.header.innerHTML = request.status;
                    contentLoader.contentHolder.innerHTML = "Oops! You have network issues or crashed server!";
                } else {
                    contentLoader.header.innerHTML = errorCode;
                    contentLoader.contentHolder.innerHTML = request.responseText;
                }
            }
        };
        request.send();
    }

    /**
     * Same as staticLoadError but not need to send contentHolder
     * @param {int} errorCode
     */
    loadError(errorCode) {
        ContentLoader.staticLoadError(errorCode, this);
    }
}

//====================Content loading end====================\\

//TODO rewrite this
class NavbarTop {
    constructor(holder) {
        this.messagesElement = document.querySelector("#NavbarTopMessages");
        this.count = 0;
        this.holder = holder;
        this.showAllMessagesElement = "<li class='divider'></li>" +
            "<li>" +
            "<a class='text-center' role='button' onclick='dashboard.sliderRightHandler.openMenu(\"messages\")'>" +
            "<strong>Read All Messages</strong>" +
            "<i class='fa fa-angle-right'></i>" +
            "</a>" +
            "</li>";

        this.divider = "<li class='divider'></li>";

        this.messagesElement.innerHTML = "<li>" +
            "<div class='text-center'>" +
            "<strong>Nothing to show</strong>" +
            "</div>" +
            "</li>" + this.showAllMessagesElement;
    }

    addMessage(message) {
        this.count++;
        this.drawMessage(message);
    }

    drawMessage(message) {
        if (this.count <= 1) {
            this.messagesElement.innerHTML = "";
        }
        if (this.count > 5) {
            this.removeLast();
            this.count = 5;
        }

        let pattern = "<li>" +
            "<a role='button' onclick='onMessageClick(event)'>" +
            "<div>" +
            "<strong>{&name}</strong>" +
            "<span class='pull-right text-muted'>" +
            "<em data-timer='1' data-time={&date}>{&dateAfterMoment}</em>" +
            "</span> " +
            "</div>" +
            "<div>{&text}</div>" +
            "</a>" +
            "</li>";

        let msg = "";
        if (message.message.length > 36) {
            msg = message.message.substring(0, 33) + "...";
        } else {
            msg = message.message;
        }

        let data = {
            name: message.sender,
            date: message.date,
            dateAfterMoment: moment(message.date, "YYYYMMDD-hh:mm").fromNow(),
            text: msg
        };
        this.messagesElement.innerHTML = new Pattern().setParameters(data).setPattern(pattern).build() +
            ((this.count > 1) ? this.divider : "") +
            this.messagesElement.innerHTML.substring(0, this.messagesElement.innerHTML.length - this.showAllMessagesElement.length);
        this.messagesElement.innerHTML += this.showAllMessagesElement;

        momentTimer.update();
    }

    removeLast() {
        this.messagesElement.removeChild(this.messagesElement.childNodes.item(8));
        this.messagesElement.removeChild(this.messagesElement.childNodes.item(7));
    }

    onMessageClick(event) {
        let target = event.target;
        target.className = "messageNotActive";
        // this.sliderRight.showMessage();
        console.log(event);
    }
}
//====================Right Slider====================\\

/**
 * This class used for open and close right slider
 */
class SliderRightHandler {
    constructor() {
        this.menu = document.querySelector("#slideMenu");
        this.pageWrapper = document.querySelector("#page-wrapper");
        this.isMenuOpened = false;
        this.disabledTabs = [];
    }

    showMenu() {
        if (!this.isMenuOpened) {
            this.pageWrapper.style.marginRight = "20%";
            let menu = this.menu;
            setTimeout(() => {
                menu.style.width = "20%";
            }, 500);
        }
        this.isMenuOpened = true;
    }

    /**
     * @param {string} menu menu id
     */
    openMenu(menu) {
        if (this.disabledTabs.indexOf(menu) == -1) {
            return;
        }
        this.menu.querySelectorAll(".active").forEach((element) => {
            element.classList.remove("active");
            element.classList.remove("in");
        });
        let elem = this.menu.querySelector("#" + menu);
        if (elem != null) {
            elem.classList.add("active");
            elem.classList.add("in");
        }
        this.showMenu();
    }

    closeMenu() {
        if (this.isMenuOpened) {
            this.menu.style.width = "0";
            let pageWrapper = this.pageWrapper;
            setTimeout(() => {
                pageWrapper.style.marginRight = "0";
            }, 5);
        }
        this.isMenuOpened = false;
    }

    /**
     * @param {string} tabName
     */
    disableTab(tabName) {
        this.disabledTabs.push(tabName);
    }

    /**
     * @param {string} tabName
     */
    enableTab(tabName) {
        this.disabledTabs.splice(this.disabledTabs.indexOf(tabName), 1);
    }
}


//====================Right Slider End====================\\
//====================Messages====================\\

class MessageHandler {
    constructor() {
        this.messageNavbar = document.querySelector("#messageNavbar");
        this.messageChat = document.querySelector("#messageChat");
        this.messageSenderElement = document.querySelector("#messageSenderName");

        function setupHeight() {
            document.querySelector("#slideMenu").style.height = window.innerHeight + "px";
            document.querySelector("#messageNavbar").style.height = (window.innerHeight - 42) + "px";
            document.querySelector("#messageElement").style.height = (window.innerHeight - 122) + "px";
            document.querySelector("#messageChat").style.height = (window.innerHeight - 122) + "px";
        }

        window.addEventListener("resize", setupHeight);
        setupHeight();

        this.chats = [];
    }

    static disable() {
        document.querySelector("#slideMenu > div > ul a[href='#messages']").parentNode.classList.add("disabled");
        dashboard.sliderRightHandler.disableTab("messages");
        updateDisabled();
    }

    static enable() {
        document.querySelector("#slideMenu > div > ul a[href='#messages']").parentNode.classList.remove("disabled");
        dashboard.sliderRightHandler.enableTab("messages");
        updateDisabled();
    }

    /**
     * @param {Message} message
     */
    showMessage(message) {
        this.showUser(message.sender);
    }

    /**
     * @param {string} username
     */
    showUser(username) {
        dashboard.sliderRightHandler.openMenu("messages");
        if (this.messageSenderElement.innerHTML != username) {
            this.findChatFromUsername(username).redrawElement();
            this.messageSenderElement.innerHTML = username;
        }
    }

    /**
     * @param {User} user
     */
    addUser(user) {
        let pattern = "<li class='user'>" +
            "<div>" +
            "<img src='{&avatarUrl}'>" +
            "</div>" +
            "<div>" +
            "<p>{&name}</p>" +
            "</div>" +
            "</li>";
        this.messageNavbar.innerHTML = new Pattern().setPattern(pattern).setParameters(user).build() + this.messageNavbar.innerHTML;
    }

    /**
     * @param {Chat} chat
     */
    addChat(chat) {
        chat.element = this.messageChat;
        this.chats.push(chat);
    }

    /**
     * @param {Message} message
     */
    addMessage(message) {
        this.findChatFromMessage(message).addMessage(message);
        this.showMessage(message);
    }

    /**
     * @param {Message} message
     * @return {Chat}
     */
    findChatFromMessage(message) {
        return this.findChatFromUsername(message.sender)
    }

    /**
     * @param {string} username
     * @return {Chat}
     */
    findChatFromUsername(username) {
        for (let chat of this.chats) {
            if (chat.sender.name == username || chat.receiver.name == username) {
                return chat;
            }
        }
    }

    /**
     * @param {Message} messageOld
     * @param {Message} messageNew
     */
    setMessage(messageOld, messageNew) {
        this.findChatFromMessage(messageOld).setMessage(messageOld, messageNew);
    }

    /**
     * @param {string} messageText
     */
    sendMessage(messageText) {
        let msg = new Message(this.chats[0].receiver, moment().format("YYYYMMDD-hh:mm"), messageText);
        this.addMessage(msg);
        // this.webSocketManager.sendMessage(msg);
    }
}

/**
 * This class used for hold and works with messages.
 * This class also used for works with element(add, set messages)
 */
class Chat {
    /**
     * @param {User} sender
     * @param {User} receiver
     */
    constructor(sender, receiver) {
        this.sender = sender;
        this.receiver = receiver;

        this.messagePatternRight = "<li class='right clearfix'>" +
            "<span class='chat-img pull-right'>" +
            "<img src='{&avatarUrl}' alt='User avatar' class='img-circle'>" +
            "</span>" +
            "<div class='chat-body clearfix'>" +
            "<div class='header'>" +
            "<strong class='primary-font pull-right'>{&senderName}</strong>" +
            "<small class='text-muted flex-row'>" +
            "<i class='fa fa-clock-o fa-fw'></i>" +
            "<p data-timer='1' data-time='{&time}'></p>" +
            "</small>" +
            "</div>" +
            "<p style='word-break: break-all'>{&message}</p>" +
            "</div>" +
            "</li>";
        this.messagePatternLeft = "<li class='left clearfix'>" +
            "<span class='chat-img pull-left'>" +
            "<img src='{&avatarUrl}' alt='User avatar' class='img-circle'>" +
            "</span>" +
            "<div class='chat-body clearfix'>" +
            "<div class='header'>" +
            "<strong class='primary-font'>{&senderName}</strong>" +
            "<small class='pull-right text-muted flex-row'>" +
            "<i class='fa fa-clock-o fa-fw'></i>" +
            "<p data-timer='1' data-time='{&time}'>{&moment}</p>" +
            "</small>" +
            "</div>" +
            "<p style='word-break: break-all'>{&message}</p>" +
            "</div>" +
            "</li>";

        this.messages = [];
        this.element = undefined;
    }

    /**
     * @param {Message} message
     */
    //FIXME
    addMessage(message) {
        this.messages.push(message);
        this.element.innerHTML += this.bakeHTMLMessage(message);
    }

    /**
     * @param {Message} messageOld
     * @param {Message} messageNew
     */
    setMessage(messageOld, messageNew) {
        let replaceIndex = this.messages.lastIndexOf(messageOld);
        if (replaceIndex == -1) {
            return;
        }
        this.messages[replaceIndex] = messageNew;
        let searchString = this.bakeHTMLMessage(messageOld);
        if (this.element.innerHTML.lastIndexOf(searchString) > 0) {
            this.element.innerHTML = this.element.innerHTML.substring(0, this.element.innerHTML.lastIndexOf(searchString));
            this.addMessage(messageNew)
        }
    }

    redrawElement() {
        this.clearElement();
        this.messages.forEach((message, i, list) => this.addMessage(message));
    }

    clearElement() {
        this.element.innerHTML = "";
    }

    /**
     * @param {Message} message
     * @return {string}
     */
    bakeHTMLMessage(message) {
        let parameters = {};
        let pattern;
        if (message.sender == this.sender.name) {
            parameters.avatarUrl = this.sender.avatarUrl;
            pattern = this.messagePatternLeft;
        } else if (message.sender == this.receiver.name) {
            parameters.avatarUrl = this.receiver.avatarUrl;
            pattern = this.messagePatternRight;
        } else {
            throw new Error("WTF happens!");
        }
        parameters.senderName = message.sender;
        parameters.time = message.date;
        parameters.moment = moment(parameters.time, "YYYYMMDD-hh:mm").fromNow();
        parameters.message = message.message;
        return new Pattern().setPattern(pattern).setParameters(parameters).build();
    }
}

/**
 * This class is abstraction of user
 */
class User {
    /**
     * @param {string} name
     * @param {string} avatarUrl url to avatar(image 16x16 px)
     */
    constructor(name, avatarUrl) {
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    /**
     * @return {string}
     */
    serialize() {
        return JSON.stringify({name: this.name, avatarUrl: this.avatarUrl});
    }

    /**
     * @param {string} json json string
     * @return {User}
     */
    static deserialize(json) {
        let parsed = JSON.parse(json);
        return new User(parsed.name, parsed.avatarUrl);
    }
}

/**
 * This class is abstraction of message
 */
class Message {
    /**
     * @param {string} sender
     * @param {string} date
     * @param {string} message
     */
    constructor(sender, date, message) {
        this.sender = sender;
        this.date = date;
        this.message = message;
    }

    /**
     * @param {string} json json string
     * @return {Message}
     */
    static deserialize(json) {
        let parsed = JSON.parse(json);
        return new Message(parsed.sender, parsed.date, parsed.message);
    }

    /**
     * @return {string}
     */
    serialize() {
        return JSON.stringify({sender: this.sender, date: this.date, message: this.message});
    }
}

//====================Messages End====================\\
//====================Websocket====================\\

/**
 * @deprecated
 */
class WebSocketManager {
    constructor(dashboard) {
        this.webSocket = new WebSocket("ws" + new RegExp("://.*/").exec(document.location.href) + "dashboard");
        this.webSocketSender = new WebsocketSender(this.webSocket);
        this.webSocket.onmessage = (event) => {
            let parts = event.data.split(":");
            let type = parts[0];
            let subType = parts[1];
            let message = event.data.substring(parts[0].length + 1 + parts[1].length + 1);
            switch (type) {
                case "menu":
                    switch (subType) {
                        case "set":
                            dashboard.navigator.parse(message);
                            break;
                        case "render":
                            dashboard.navigator.renderMenu();
                            break;
                    }
                    break;
                case "menuPlugins":
                    if (subType == "set") {
                        dashboard.menuPluginHandler.parse(message);
                    }
                    break;
                case "currentCompInfo":
                    if (subType == "set") {
                        dashboard.currentCompInfo.parse(message);
                    }
                    break;
            }
        };

        this.dashboard = dashboard;

        this.isGetCurrentComputerInfoEnabled = false;
    }

    /**
     * @param {Message} message
     */
    //TODO write backend
    sendMessage(message) {
        this.webSocketSender.send("messages:sendMessage:" + message.serialize());
    }

    updateMenu() {
        this.webSocketSender.send("menu:update");
    }

    updateMenuPlugins() {
        this.webSocketSender.send("menuPlugins:get");
    }

    disableCurrentComputerInfo() {
        if (this.isGetCurrentComputerInfoEnabled) {
            this.webSocketSender.send("currentCompInfo:stop");
            this.isGetCurrentComputerInfoEnabled = false;
        }
    }

    enableCurrentComputerInfo() {
        if (!this.isGetCurrentComputerInfoEnabled) {
            this.webSocketSender.send("currentCompInfo:start");
            this.isGetCurrentComputerInfoEnabled = true;
        }
    }

    performGC() {
        this.webSocketSender.send("system:gc");
    }
}

/**
 * This class used for send requests on WebSocket. If WebSocket is not ready then sender add message to queue and if
 * WebSocket ready send all messages in queue
 */
class WebsocketSender {
    /**
     * @param {WebSocket} websocket
     */
    constructor(websocket) {
        this.websocket = websocket;
        this.queue = [];

        let that = this;
        this.websocket.onopen = () => {
            that.queue.forEach((message) => {
                that.websocket.send(message);
            })
        };
    }

    /**
     * @param {string} message
     */
    send(message) {
        if (this.websocket.readyState < 1) {
            this.queue.push(message);
        } else {
            this.websocket.send(message);
        }
    }
}

//====================Websocket end====================\\

//====================Menu plugin====================\\

class MenuUtils {
    /**
     * If enabled is true then enable sortable on both elements(#menuPluginsEditor, #menuPlugins). If enabled is false
     * then disable both elements.
     * @param {boolean} enabled
     */
    static sortable(enabled) {
        if (enabled) {
            $("#menuPluginsEditor").sortable({
                connectWith: ".menuPluginsConnected",
                cancel: ".menuPluginsDisabled"
            }).disableSelection().removeClass("menuPluginsDisabled");
            $("#menuPlugins").sortable({
                connectWith: ".menuPluginsConnected",
                cancel: ".menuPluginsDisabled"
            }).disableSelection().removeClass("menuPluginsDisabled");
        } else {
            $("#menuPluginsEditor").addClass("menuPluginsDisabled");
            $("#menuPlugins").addClass("menuPluginsDisabled");
        }
    }

    /**
     * Setup width of top menu
     */
    static setupMenu() {
        let menuPluginsElement = document.querySelector("#menuPlugins");
        let menu = document.querySelector("#topNavbar");

        let maxWidth = menu.offsetWidth - document.querySelector("#topNavbar > .navbar-header").offsetWidth
            - document.querySelector("#topNavbar > .navbar-right").offsetWidth;
        menuPluginsElement.style.width = maxWidth + "px";
        menuPluginsElement.style.height = menu.offsetHeight - 1 + "px";
    }
}

/**
 * This class used for handle menu plugins editor. If needed element doesn't exists then it do nothing
 */
class MenuPluginsEditorHandler {
    /**
     * @param {MenuPluginHandler} menuPluginHandler
     */
    constructor(menuPluginHandler) {
        this.menuPluginHandler = menuPluginHandler;
        this.editorElement = document.querySelector("#menuPluginsEditor");
        if (this.editorElement == null) {
            return;
        }

        this.elementPattern = "<li data-name='{&name}'>" +
            "<div style='width: {&width} px; height: {&height} px;'>" +
            "{&source}" +
            "</div>" +
            "</li>";

        menuPluginHandler.onPluginAdd = (plugin) => {
            this.draw(plugin);
        };

        $("#menuPluginsEditor").on("sortreceive", (event, ui) => {
            let elements = document.querySelector("#menuPluginsEditor").querySelectorAll("li[data-name=" + ui.item.data("name") + "]");
            if (elements.length > 1) {
                elements.forEach((element, i) => {
                    if (i > 0) {
                        element.parentNode.removeChild(element);
                    }
                })
            }
        }).on("sortremove", (event, ui) => {
            this.draw(menuPluginHandler.getPluginFromName(ui.item.data("name")));
        });
    }

    /**
     * Redraw all elements
     */
    redraw() {
        this.menuPluginHandler.getAllPlugins().forEach((plugin) => {
            this.draw(plugin);
        });
    }

    /**
     * Draw plugin(Create new element from pattern and push ot into end of the container)
     * @param {MenuPlugin} plugin
     */
    draw(plugin) {
        if (plugin == undefined) {
            return;
        }
        this.editorElement.innerHTML += new Pattern().setPattern(this.elementPattern).setParameters({
            width: plugin.getWidth(),
            height: document.querySelector("#menuPlugins").offsetHeight,
            source: plugin.getCode(),
            name: plugin.getName()
        }).build();
    }

    /**
     * Reset #menuPluginsEditor(set innerHTML to "")
     */
    reset() {
        this.editorElement.innerHTML = "";
    }

    destroy() {
        this.menuPluginHandler.onPluginAdd = () => {
        };
    }
}

/**
 * This class holds plugins for top menu. Used in MenuEditorHandler.
 */
class MenuPluginHandler extends Parser {
    constructor() {
        super();

        this.plugins = [];
        this.onPluginAdd = (plugin, handler) => {
        };
    }

    /**
     * @param {MenuPlugin} plugin
     */
    addPlugin(plugin) {
        this.plugins.push(plugin);
        if (plugin.isActive()) {
            this.onPluginAdd(plugin, this);
        }
    }

    /**
     * @return {string[]} names of active plugins
     */
    getPluginNames() {
        return this.plugins.filter((plugin) => plugin.isActive()).map((plugin) => plugin.getName());
    }

    /**
     * @param {string} name
     */
    getPluginFromName(name) {
        for (let plugin of this.plugins) {
            if (plugin.getName() == name) {
                return plugin;
            }
        }
    }

    /**
     * @return {MenuPlugin[]} plugins
     */
    getAllPlugins() {
        let plugins = [];
        this.plugins.filter((plugin) => plugin.isActive()).forEach((plugin) => plugins.push(plugin));
        return plugins;
    }

    /**
     * Clear and set elements form json
     * @param {string} message
     */
    parse(message) {
        let split = message.split(":");

        let parts = [];
        parts.push(split[0]);
        parts.push(split[1]);
        parts.push(split.slice(2, split.length).join(":"));
        if (parts[1] == "set") {
            return JSON.parse(parts[2]);
        } else {
            console.log("[MenuPluginHandler] Unexpected message: " + message);
            return {};
        }
    }

    postParse(result) {
        this.plugins = [];
        result.items.forEach((item) => {
            let clazz = eval(item.value);

            dashboard.menuPluginHandler.addPlugin(new clazz());
        })
    }
}

//====================Menu plugin end====================\\
//====================Menu plugin API====================\\

class MenuPlugin {
    constructor() {
        this.active = false;
        this.name = "";
        this.parameters = {};
    }

    /**
     * @return {boolean} is plugin active
     */
    isActive() {
        return this.active;
    }

    /**
     * @return {int} plugin width in pixels
     */
    getWidth() {

    }

    /**
     * @return {string} code
     */
    getCode() {

    }

    /**
     * @return {string} name of plugin
     */
    getName() {
        return this.name;
    }

    /**
     * Set parameter of MenuPlugin. Parameters used in plugin code for add settings to plugin
     * @param {string} name
     * @param parameter
     */
    setParameter(name, parameter) {
        this.parameters[name] = parameter;
    }

    /**
     * Return default parameters map. Used for generate settings screen
     * @return {object}
     */
    getDefaultParameters() {
        return {};
    }

    /**
     * @return {string}
     */
    serialize() {
        return JSON.stringify(this);
    }

    /**
     * @param {string} string
     * @return {MenuPlugin}
     */
    deserialize(string) {
        let object = JSON.parse(string);
        this.active = object.active;
        this.name = object.name;
        this.parameters = object.parameters;
    }
}

//====================Menu plugin API End====================\\

//====================Permissions====================\\
/**
 * This class used for check permissions
 */
class Permissions {
    /**
     * If permission acquired - call resolve(), overwise reject()
     * @return {Promise.<>}
     */
    static acquirePushNotificationPermission() {
        if (Notification.permission == undefined) {
            alert("Can't use notification api!");
            return Promise.reject();
        }
        switch (Notification.permission.toLowerCase()) {
            case "granted":
                return Promise.resolve();
            case "denied":
                return Promise.reject();
            case "default": {
                return new Promise((resolve, reject) => {
                    Notification.requestPermission((result) => {
                        if (result.toLowerCase() == "granted") {
                            resolve();
                        } else {
                            reject();
                        }
                    })
                });
            }
        }
    }
}
//====================Permissions end====================\\

//====================Notification API====================\\
/**
 * This class used for build notification
 */
class PushNotificationBuilder {
    constructor(title) {
        this._title = title;
        this._dir = "auto";
        this._lang = "en-US";
        this._body = "";
        this._tag = "";
        this._icon = "";
        this._data = "";
        this._requireInteraction = false;
        this._silent = false;

        this._listeners = new Map();
    }

    /**
     * <code>auto</code> - auto, <code>ltr</code> - left to right, <code>rtl</code> - right to left
     * @param {string} value
     */
    setTextDirection(value) {
        this._dir = value;
        return this;
    }

    /**
     *
     * @param {string} value like <code>en-US</code>
     * @return {PushNotificationBuilder}
     */
    setLang(value) {
        this._lang = value;
        return this;
    }

    /**
     * @param {string} value
     * @return {PushNotificationBuilder}
     */
    setBody(value) {
        this._body = value;
        return this;
    }

    /**
     * If app create second notification with tag, it will replace first
     * @param {string} value
     * @return {PushNotificationBuilder}
     */
    setTag(value) {
        this._tag = value;
        return this;
    }

    /**
     * @param {string} value
     * @return {PushNotificationBuilder}
     */
    setIcon(value) {
        this._icon = value;
        return this;
    }

    /**
     * @param {string} value
     * @return {PushNotificationBuilder}
     */
    setData(value) {
        this._data = value;
        return this;
    }

    /**
     *
     * @param {boolean} value
     * @return {PushNotificationBuilder}
     */
    setRequireInteraction(value) {
        this._requireInteraction = value;
        return this;
    }

    /**
     *
     * @param {boolean} value
     * @return {PushNotificationBuilder}
     */
    setSilent(value) {
        this._silent = value;
        return this;
    }

    /**
     *
     * @param {string} name
     * @param {function} listener
     * @return {PushNotificationBuilder}
     */
    addListener(name, listener) {
        let v = this._listeners.get(name);
        if (v == undefined) {
            v = [];
            this._listeners.set(message, v);
        }
        v.push(listener);
        return this;
    }

    /**
     *
     * @param {string} name
     * @param {function} listener
     * @return {PushNotificationBuilder}
     */
    removeListener(name, listener) {
        let v = this._listeners.get(name);
        if (v == undefined) {
            return this;
        }
        v.splice(v.indexOf(listener), 1);
        return this;
    }

    /**
     * if notification created - resolve(notification), else - reject()
     * @return {Promise.<Notification>}
     */
    build() {
        return Permissions.acquirePushNotificationPermission().then((resolve) => {
            let notification = new Notification(this._title, {
                dir: this._dir,
                lang: this._lang,
                body: this._body,
                tag: this._tag,
                icon: this._icon,
                data: this._data,
                requireInteraction: this._requireInteraction,
                silent: this._silent
            });
            this._listeners.forEach((value, key) => {
                value.forEach((v) => notification.addEventListener(key, v));
            });

            if (resolve != undefined) {
                resolve(notification);
            }
        })
    }
}
//====================Notification API end====================\\
/**
 * This is main class of AlesharikWebServer dashboard
 */
class Dashboard {
    constructor() {
        if (!window.Worker) {
            alert("Workers ot supported! Please update your browser!");
        }

        MessagingManager.sendMessage("hello"); //Login in WebSocket

        this.navbarTop = new NavbarTop();
        this.contentLoader = new ContentLoader();
        this.navigator = new Navigator(this.contentLoader);
        MessagingManager.addParser("menu", this.navigator);
        MessagingUtils.updateMenu();
        // this.webSocketManager = new WebSocketManager(this);
        this.sliderRightHandler = new SliderRightHandler();
        // this.webSocketManager.updateMenu();

        this.messageHandler = new MessageHandler();

        MenuUtils.setupMenu();
        this.menuPluginHandler = new MenuPluginHandler();
        MessagingManager.addParser("menuPlugins", this.menuPluginHandler);

        // this.webSocketManager.updateMenuPlugins();

        this.currentCompInfo = new ComputerInfo();
        MessagingManager.addParser("currentCompInfo", this.currentCompInfo);
        MessagingUtils.enableReceivingComputerInfo();
    }

    /**
     * Load js file as <script>
     * @param {string} file local or internet file address
     * @return {Promise} with new element as first argument to resolve
     */
    loadJSFile(file) {
        return new Promise((resolve) => {
            let request = new XMLHttpRequest();
            request.open("GET", file);
            request.onreadystatechange = () => {
                if (request.readyState === 4) {
                    let scriptElement = document.createElement("script");
                    scriptElement.type = "text/javascript";
                    if (request.status !== 200) {
                        scriptElement.src = file;
                    } else {
                        scriptElement.innerHTML = request.responseText;
                    }
                    document.getElementsByTagName("head")[0].appendChild(scriptElement);
                    resolve(scriptElement);
                }
            };
            request.send();
        });
    }

    /**
     * Load css file as <style> or as <link>
     * @param file local or internet file address call on load ended. Receive created element.
     * @return {Promise} with new element as first argument to resolve function
     */
    loadCSSFile(file) {
        return new Promise((resolve) => {
            let request = new XMLHttpRequest();
            request.open("GET", file);
            request.onreadystatechange = () => {
                if (request.readyState === 4) {
                    if (request.status !== 200) {
                        let link = document.createElement("link");
                        link.rel = "stylesheet";
                        link.type = "text/css";
                        link.href = file;
                        document.getElementsByTagName("head")[0].appendChild(link);
                        resolve(link);
                    } else {
                        let style = document.createElement("style");
                        style.innerHTML = request.responseText;
                        document.getElementsByTagName("head")[0].appendChild(style);
                        resolve(style);
                    }
                }
            };
            request.send();
        });
    }
}

