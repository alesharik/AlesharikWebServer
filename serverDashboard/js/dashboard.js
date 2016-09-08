'use strict';
class Dashboard {
    constructor() {
        this.navbarTop = new NavbarTop();
        this.contentLoader = new ContentLoader();
        this.navigator = new Navigator(this.contentLoader);
        this.webSocketManager = new WebSocketManager(this);
        this.webSocketManager.updateMenu();

        MenuUtils.setupMenu();
        this.menuPluginHandler = new MenuPluginHandler();
    }

    onMessageClick(event) {
        this.navbarTop.onMessageClick(event);
    }
}

//====================Side menu====================\\

/**
 * This class used for works with side menu
 */
class Navigator {
    /**
     * @param {ContentLoader} contentLoader
     */
    constructor(contentLoader) {
        this.contentLoader = contentLoader;
        this.items = [];
        this.navigator = document.querySelector("#navigator");
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
        this.hasSearchModeOn = false;
        this.searchElement = document.querySelector("#navigatorSearch");
        this.searchList = this.searchElement.querySelector("ul.list-group");
    }

    parse(jsonString) {
        let json = JSON.parse(jsonString);
        let items = this.items;
        json.items.forEach((item) => {
            item = JSON.parse(item);
            items.push((item.type == "item") ? MenuItem.deserialize(item) : MenuDropdown.deserialize(item));
        });
        this.renderMenu();
    }

    renderMenu() {
        this.navigator.innerHTML = this.searchElementCode;
        //noinspection JSCheckFunctionSignatures
        this.items.forEach((item) => {
            this.navigator.innerHTML += item.toHTML(0);
        });

        $('#navigator').metisMenu();

    }

    search(text) {
        if (!this.hasSearchModeOn) {
            this.toggleSearchMode();
        }

        document.querySelectorAll("#menuSearchInputElement").forEach((elem) => {
            elem.value = text;
        });
        let result = [];
        //noinspection JSCheckFunctionSignatures
        this.items.forEach((item) => {
            if (item.type == "item") {
                if (item.text.indexOf(text) != -1) {
                    result.push(item);
                }
            } else {
                result = result.concat(item.search(text));
            }
        });

        if (result.length > 0) {
            this.renderSearchList(result);
        } else {
            this.searchList.innerHTML = "<div class='text-center'>" +
                "<strong>Your search did not match any items.</strong>" +
                "</div>";
        }
    }

    renderSearchList(list) {
        let pattern = "<li class='list-group-item'>" +
            "<a data-contentID='{&contentId}' onclick='handleMenuItemClick(event)' role='button' style='cursor: pointer'>" +
            "<i class='fa fa-{&fa} fa-fw'></i>" +
            "{&text}</a>" +
            "</li>";
        let searchList = this.searchList;
        searchList.innerHTML = "";
        list.forEach((item) => {
            searchList.innerHTML += new Pattern().setPattern(pattern).setParameters(item).build();
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

    //TODO add normal selection on active element
    showContent(target) {
        if (this.hasSearchModeOn) {
            this.toggleSearchMode();
        }
        this.contentLoader.load(target.getAttribute("data-contentid"));

        let parent = target.parentNode.parentNode;
        parent.classList.add("in");
        target.classList.add("active");
        let element = parent.parentNode;
        if (element.tagName == "LI") {
            element.classList.add("active");
        }
    }

    clear() {
        this.items = [];
        this.renderMenu();
    }
}

class MenuItem {
    constructor(contentId, fa, text) {
        this.contentId = contentId;
        this.fa = fa;
        this.text = text;
        this.type = "item";
    }

    static deserialize(json) {
        return new MenuItem(json.contentId, json.fa, json.text);
    }

    toHTML() {
        let pattern = "<li>" +
            "<a data-contentID='{&contentId}' onclick='handleMenuItemClick(event)' role='button'>" +
            "<i class='fa fa-{&fa} fa-fw'></i>" +
            "{&text}</a>" +
            "</li>";
        return new Pattern().setPattern(pattern).setParameters(this).build();
    }
}

class MenuDropdown {
    constructor(fa, text, items) {
        this.type = "dropdown";
        this.fa = fa;
        this.text = text;
        this.items = items;
    }

    static deserialize(json) {
        let items = [];
        json.items.forEach((item, i, arr) => {
            item = JSON.parse(item);
            items.push((item.type == "item") ? MenuItem.deserialize(item) : MenuDropdown.deserialize(item))
        });
        return new MenuDropdown(json.fa, json.text, items);
    }

    toHTML(depth) {
        if (depth > 1) {
            return;
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

    search(text) {
        let result = [];
        //noinspection JSCheckFunctionSignatures
        this.items.forEach((item) => {
            if (item.type == "item") {
                if (item.text.indexOf(text) != -1) {
                    result.push(item);
                }
            } else {
                result = result.concat(item.search(text));
            }
        });
        return result;
    }
}

//====================Side menu end====================\\
//====================Content loading====================\\

class ContentLoader {
    constructor() {
        this.contentHolder = document.querySelector("#contentHolder");
        this.header = document.querySelector("#contentHeader");
    }

    load(id) {
        let that = this;

        let request = new XMLHttpRequest();
        request.open("GET", "/content/" + id);
        request.onreadystatechange = function () {
            if (request.readyState == 4) {
                if (request.status != 200) {
                    ContentLoader.staticLoadError(request.status, that);
                } else {
                    that.header.innerHTML = id;
                    that.contentHolder.innerHTML = request.responseText;
                }
            }
        };
        request.send();
    }

    static staticLoadError(errorCode, contentLoader) {
        let header = contentLoader.header;
        let holder = contentLoader.contentLoader;

        let request = new XMLHttpRequest();
        request.open("GET", "/errors/" + errorCode);
        request.onreadystatechange = function () {
            if (request.readyState == 4) {
                if (request.status != 200) {
                    header.innerHTML = request.status;
                    holder.innerHTML = "Oops! You have network issues or crashed server!";
                } else {
                    header.innerHTML = request.status;
                    holder.innerHTML = request.responseText;
                }
            }
        }
    }

    loadError(errorCode) {
        ContentLoader.staticLoadError(errorCode, this);
    }
}

//====================Content loading end====================\\

class Holder {
    constructor() {
        this.messages = [];
        this.onmessageList = [];
    }

    addMessage(message) {
        this.messages.push(message);
        this.onmessage(message)
    }

    addMessageHandler(handler) {
        this.onmessageList.push(handler);
    }

    onmessage(message) {
        //noinspection JSCheckFunctionSignatures
        this.onmessageList.forEach((handler) => {
            handler(message);
        })
    }
}

class NavbarTop {
    constructor(holder) {
        this.messagesElement = document.querySelector("#NavbarTopMessages");
        this.count = 0;
        this.sliderRight = new SliderRight(holder);
        this.holder = holder;
        this.showAllMessagesElement = "<li class='divider'></li>" +
            "<li>" +
            "<a class='text-center' role='button' onclick='dashboard.navbarTop.sliderRight.openMenu(\"messages\")'>" +
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

/**
 * This class used in right menu
 */
class SliderRight {
    constructor(holder) {
        this.holder = holder;
        this.menu = document.querySelector("#slideMenu");
        this.menu.style.height = window.innerHeight + "px";
        document.querySelector("#messageNavbar").style.height = (window.innerHeight - 42) + "px";
        document.querySelector("#messageElement").style.height = (window.innerHeight - 122) + "px";
        document.querySelector("#messageChat").style.height = (window.innerHeight - 122) + "px";
        window.resize = function () {
            document.querySelector("#slideMenu").style.height = window.innerHeight + "px";
            document.querySelector("#messageNavbar").style.height = (window.innerHeight - 42) + "px";
            document.querySelector("#messageElement").style.height = (window.innerHeight - 122) + "px";
            document.querySelector("#messageChat").style.height = (window.innerHeight - 122) + "px";
        };
        this.pageWrapper = document.querySelector("#page-wrapper");
        this.messageNavbar = document.querySelector("#messageNavbar");
        this.messageChat = document.querySelector("#messageChat");
        this.messageSenderElement = document.querySelector("#messageSenderName");
        this.chats = [];

        this.isMenuOpened = false;
    }

    showMessage(message) {
        this.showUser(message.sender);
    }

    showUser(username) {
        this.openMenu("message");
        if (this.messageSenderElement.innerHTML != username) {
            this.findChatFromUsername(username).redrawElement();
            this.messageSenderElement.innerHTML = username;
        }
    }

    showMenu() {
        this.isMenuOpened = true;
        this.pageWrapper.style.marginRight = "20%";
        let menu = this.menu;
        setTimeout(function () {
            menu.style.width = "20%";
        }, 500);
    }

    openMenu(menuName) {
        this.showMenu();
    }

    hideMenu() {
        this.isMenuOpened = false;
        let menu = this.menu;
        menu.style.width = "0";
        let pageWrapper = this.pageWrapper;
        setTimeout(function () {
            pageWrapper.style.marginRight = "0";
        }, 5);
    }

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

    addChat(chat) {
        chat.element = this.messageChat;
        this.chats.push(chat);
    }

    addMessage(message) {
        debugger;
        this.findChat(message).addMessage(message);
        this.showMessage(message);
    }

    findChat(message) {
        return this.findChatFromUsername(message.sender)
    }

    findChatFromUsername(username) {
        for (let chat of this.chats) {
            if (chat.sender.name == username) {
                return chat;
            }
        }
    }

    setMessage(messageOld, messageNew) {
        this.findChat(messageOld).setMessage(messageOld, messageNew);
    }

    //TODO write this
    sendMessage(messageText) {
        let msg = new Message(this.chats[0].receiver, moment().format("YYYYMMDD-hh:mm"), messageText);
        this.addMessage(msg);
        // this.webSocketManager.sendMessage(msg);
    }
}

class Chat {
    /**
     * @param sender User
     * @param receiver User
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
    }

    addMessage(message) {
        this.messages.push(message);
        this.element.innerHTML += this.getBuildedMessage(message);
    }

    setMessage(messageOld, messageNew) {
        let replaceIndex = this.messages.lastIndexOf(messageOld);
        if (replaceIndex != -1) {
            this.messages[replaceIndex] = messageNew;
        } else {
            return;
        }

        let searchString = this.getBuildedMessage(messageOld);
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

    getBuildedMessage(message) {
        let parameters = {};
        let pattern;
        if (message.sender == this.sender.name) {
            parameters.avatarUrl = this.sender.avatarUrl;
            pattern = this.messagePatternLeft;
        } else if (message.sender = this.receiver.name) {
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

class User {
    constructor(name, avatarUrl) {
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    serialize() {
        return JSON.stringify({name: this.name, avatarUrl: this.avatarUrl});
    }

    static deserialize(json) {
        let parsed = JSON.parse(json);
        return new User(parsed.name, parsed.avatarUrl);
    }
}

class Message {
    constructor(sender, date, message) {
        this.sender = sender;
        this.date = date;
        this.message = message;
    }

    static deserialize(string) {
        let parsed = JSON.parse(string);
        return new Message(parsed.sender, parsed.date, parsed.message);
    }

    serialize() {
        return JSON.stringify({sender: this.sender, date: this.date, message: this.message});
    }
}

//====================Websocket====================\\

class WebSocketManager {
    constructor(dashboard) {
        this.webSocket = new WebSocket("ws" + new RegExp("://.*/").exec(document.location.href) + "dashboard");
        this.webSocketSender = new WebsocketSender(this.webSocket);
        this.webSocket.onmessage = (event) => {
            // if (event.data.startsWith("messages:")) {
            //     let type = event.data.substring(9, event.data.indexOf(":", 10));
            //     let message = event.data.substring(10 + type.length);
            //     // switch (type) {
            //     //     case "addMessage":
            //     //         msg = Message.deserialize(message);
            //     //         this.navbarTop.addMessage(msg);
            //     //         this.sliderRight.addMessage(msg);
            //     //         break;
            //     //     case "setMessage":
            //     //         msg = message.split("");
            //     //         this.sliderRight.setMessage(Message.deserialize(msg[0]), Message.deserialize(msg[1]));
            //     //         break;
            //     // }
            //     switch (type) {
            //         case "menu":
            //             dashboard.navigator.parse(message);
            //     }
            // }
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
            }
        };

        this.dashboard = dashboard;
    }

    sendMessage(message) {
        this.webSocketSender.send("messages:sendMessage:" + message.serialize());
    }

    updateMenu() {
        this.webSocketSender.send("menu:update");
    }
}

class WebsocketSender {
    constructor(websocket) {
        this.websocket = websocket;
        this.startQueue = [];
        let queue = this.startQueue;
        let socket = this.websocket;
        this.websocket.onopen = () => {
            queue.forEach((message) => {
                socket.send(message);
            })
        };
    }

    send(message) {
        if (this.websocket.readyState < 1) {
            this.startQueue.push(message);
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
                connectWith: ".menuPluginsConnected"
            }).disableSelection();
            $("#menuPlugins").sortable({
                connectWith: ".menuPluginsConnected"
            }).disableSelection();
        } else {
            $("#menuPluginsEditor").sortable("disable");
            $("#menuPlugins").sortable("disable");
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

        this.elementPattern = "<li>" +
            "<div style='width: {&width} px; height: {&height} px;'>" +
            "{&source}" +
            "</div>" +
            "</li>";

        let that = this;
        menuPluginHandler.onPluginAdd = (plugin) => {
            that.draw(plugin);
        }
    }

    /**
     * Redraw all elements
     */
    redraw() {
        let that = this;
        this.menuPluginHandler.getAllPlugins().forEach((plugin) => {
            that.draw(plugin);
        });
    }

    /**
     * Draw plugin(Create new element from pattern and push ot into end of the container)
     * @param {MenuPlugin} plugin
     */
    draw(plugin) {
        this.editorElement.innerHTML += new Pattern().setPattern(this.elementPattern).setParameters({
            width: plugin.getWidth(),
            height: document.querySelector("#menuPlugins").offsetHeight,
            source: plugin.getCode()
        }).build();
    }

    /**
     * Reset #menuPluginsEditor(set innerHTML to "")
     */
    reset() {
        this.editorElement.innerHTML = "";
    }
}

/**
 * This class holds plugins for top menu. Used in MenuEditorHandler.
 */
class MenuPluginHandler {
    constructor() {
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
        let names = [];
        this.plugins.filter((plugin) => plugin.isActive()).forEach((plugin) => names.push(plugin.getName()));
        return names;
    }

    /**
     * @param {string} name
     */
    getPluginForName(name) {
        for (let plugin in this.plugins) {
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
}

//====================Menu plugin end====================\\
//====================Menu plugin API====================\\

class MenuPlugin {
    constructor() {
        this.active = false;
        this.name = "";
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
}

//====================Menu plugin API End====================\\