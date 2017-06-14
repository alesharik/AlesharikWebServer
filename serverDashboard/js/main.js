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

function updateDisabled() {
    $('.disabled').click((e) => {
        if (event.target.parentNode.classList.contains("disabled")) {
            e.preventDefault();
            return false;
        }
    });
}

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

/**
 * Close slider and set #wrapper display to 'none'
 */
function clearScreen() {
    if (isScreenCleared) {
        return;
    }

    isScreenCleared = true;
    if (dashboard.sliderRightHandler.isMenuOpened) {
        dashboard.sliderRightHandler.closeMenu();
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
            dashboard.sliderRightHandler.showMenu();
        }
        document.querySelector("#wrapper").style.display = "";
    }
}

document.onmousemove = function () {
    clearTimeout(timeoutID);
    timeoutID = setTimeout(function () {
        clearScreen()
    }, AFK_TIMEOUT);
    if (isScreenCleared) {
        loadScreen();
    }
};

//--------------------AFK Module End--------------------\\

/**
 * Init timer
 */
let momentTimer = new Timer();
let dashboard;

/**
 * Load all needed
 */
window.addEventListener("load", () => {
    let logpass = getCookie("Logpass");
    if (logpass !== undefined) {
        localStorage.setItem("logpass", logpass);
    }

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
    dashboard.navbarTop.onMessageClick(event);
}

/**
 * Send message
 */
function sendMessage() {
    dashboard.messageHandler.sendMessage(document.querySelector('#messageInput').value);
    document.querySelector('#messageInput').value = ''
}

/**
 * Used to handle message <input> and send message if enter pressed
 */
function handleMessageInput(e) {
    if (e === null || e === undefined) {
        return;
    }

    let keyCode = e.keyCode || e.which;
    if (keyCode === 13) {
        sendMessage();
    }
}

/**
 * Called if menu item pressed. Load content form event and reset search if it needed
 */
function handleMenuItemClick(e, type) {
    // dashboard.navigator.showContent(e.target || e.srcElement);
    MenuItemManager.getItemForType(type).handleClick(e);
}

/**
 * Search items in menu
 * @param input DOM element <input>
 */
function searchInMenu(input) {
    if (input.value === "" || input.value === undefined || input.value === null) {
        return;
    }

    dashboard.navigator.search(input.value);
}

/**
 * Used for handle pressed enter on menu search <input>
 */
function handleMenuSearchInput(e) {
    if (e === null || e === undefined) {
        return;
    }

    let target = e.target || e.srcElement;

    let keyCode = e.keyCode || e.width;
    if (keyCode === 13) {
        searchInMenu(target);
    } else if (keyCode === 27 && dashboard.navigator.hasSearchModeOn) {
        dashboard.navigator.toggleSearchMode();
    }
}

/**
 * This function used for get specific cookie form name
 * @param {string} name
 * @return {*} can be undefined!
 */
function getCookie(name) {
    let matches = document.cookie.match(new RegExp(
        "(?:^|; )" + name.replace(/([.$?*|{}()\[\]\\\/+^])/g, '\\$1') + "=([^;]*)"
    ));
    return matches ? decodeURIComponent(matches[1]) : undefined;
}

// //====================Droppable====================\\
// function switchToMenuEdit() {
//
// }
//
// function disableMenuEditing() {
//
// }
// //====================End droppable====================\\
