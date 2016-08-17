'use strict';

document.addEventListener("resize", () => {
    let topOffset = 50;
    let width = (window.innerWidth > 0) ? window.innerWidth : screen.width;
    if (width < 768) {
        document.querySelector("div.navbar-collapse").classList.add("collapse");
        topOffset = 100;
    } else {
        document.querySelector("div.navbar-collapse").classList.remove("collapse");
    }

    let height = ((window.innerHeight > 0) ? window.innerHeight : screen.height) - 1 - topOffset;
    if (height < 1) {
        height = 1;
    }
    if (height > topOffset) {
        document.querySelector("#page-wrapper").style.minHeight = height + "px";
    }
});

//--------------------AFK Module Start--------------------\\
let AFK_TIMEOUT = 60000;

let timeoutID;
let isSliderOpen = false;
let isScreenCleared = false;
document.onmousemove = function () {
    clearTimeout(timeoutID);
    timeoutID = setTimeout(function () {
        clearScreen()
    }, AFK_TIMEOUT);
    if (isScreenCleared) {
        loadScreen();
    }
};

/**
 * Close slider and set #wrapper display to 'none'
 */
function clearScreen() {
    if (isScreenCleared) {
        return;
    }

    isScreenCleared = true;
    if (dashboard.navbarTop.sliderRight.isMenuOpened) {
        dashboard.navbarTop.sliderRight.hideMenu();
        isSliderOpen = true;
    } else {
        isSliderOpen = false;
    }
    setTimeout(function () {
        document.querySelector("#wrapper").style.display = "none";
    }, 2005)
}

/**
 * Restore screen and if need open slider
 */
function loadScreen() {
    if (isScreenCleared) {
        if (isSliderOpen) {
            dashboard.navbarTop.sliderRight.showMenu();
        }
        document.querySelector("#wrapper").style.display = "";
    }
}
//--------------------AFK Module End--------------------\\

/**
 * Init timer
 */
new Timer();
var dashboard;

/**
 * Load all needed
 */
document.addEventListener("DOMContentLoaded", () => {
    dashboard = new Dashboard();

    let canvas = document.querySelector("#backgroundCanvas");
    canvas.style.width = window.innerWidth + "px";
    setup();
    dashboard.navigator.renderMenu();
});

/**
 * Resize canvas
 */
document.addEventListener("resize", () => {
    let canvas = document.querySelector("#backgroundCanvas");
    canvas.style.width = window.innerWidth + "px";
    update();
});
/**
 * Used for load page form its ID without reset menu search
 */
function goTo(pageID) {
    dashboard.contentLoader.load(pageID);
}
/**
 * Called on click on message in right slider
 */
function onMessageClick(event) {
    dashboard.onMessageClick(event);
}

/**
 * Send message
 */
function sendMessage() {
    dashboard.navbarTop.sliderRight.sendMessage(document.querySelector('#messageInput').value);
    document.querySelector('#messageInput').value = ''
}

/**
 * Used to handle message <input> and send message if enter pressed
 */
function handleMessageInput(e) {
    if (e == null || e == undefined) {
        return;
    }

    let keyCode = e.keyCode || e.which;
    if (keyCode == 13) {
        sendMessage();
    }
}

/**
 * Called if menu item pressed. Load content form event and reset search if it needed
 */
function handleMenuItemClick(e) {
    dashboard.navigator.showContent(e.target || e.srcElement)
}

/**
 * Search items in menu
 * @param input DOM element <input>
 */
function searchInMenu(input) {
    if (input.value == "" || input.value == undefined || input.value == null) {
        return;
    }

    dashboard.navigator.search(input.value);
}

/**
 * Used for handle pressed enter on menu search <input>
 */
function handleMenuSearchInput(e) {
    if (e == null || e == undefined) {
        return;
    }

    let target = e.target || e.srcElement;

    let keyCode = e.keyCode || e.width;
    if (keyCode == 13) {
        searchInMenu(target);
    }
}
