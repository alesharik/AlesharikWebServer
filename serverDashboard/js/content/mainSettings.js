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
class ChangeLoginPassword {
    constructor() {
        this.oldLogin = $("#oldLogin");
        this.oldPassword = $("#oldPassword");
    }

    /**
     * If isError == true, then old login and password field will be marked as `form-control-danger` class, overwise `form-control-danger`
     * will be removed
     * @param {boolean} isError
     */
    setOldLoginPasswordError(isError) {
        if (isError) {
            this.oldLogin.addClass("form-control-danger");
            this.oldPassword.addClass("form-control-danger");
        } else {
            this.oldLogin.removeClass("form-control-danger");
            this.oldPassword.removeClass("form-control-danger");
        }
    }

    /**
     * @param {string} error
     */
    static showError(error) {
        document.querySelector("#loginPasswordChangeErrorModal .modal-body").innerHTML = error;
    }

    /**
     * Request user for changes
     */
    static trySendChanges() {
        $('#loginPasswordChangeModal').modal({
            keyboard: false,
            show: true
        });
    }

    static showSuccess() {
        $('#loginPasswordChangeOkModal').modal({
            keyboard: true,
            show: true
        });
    }

    sendChanges() {
        let request = new XMLHttpRequest();
        request.open("GET", "/changeLoginPassword?oldLogin=" + document.querySelector("#oldLogin").value
            + "&oldPassword=" + document.querySelector("#oldPassword").value
            + "&newLogin=" + document.querySelector("#newLogin").value
            + "&newPassword=" + document.querySelector("#newPassword").value);
        request.onreadystatechange = () => {
            if (request.readyState === 4) {
                if (request.status === 200) {
                    if (request.responseText === "ok") {
                        ChangeLoginPassword.showSuccess();
                        this.setOldLoginPasswordError(false);
                    } else if (request.responseText === "oldLogPassError") {
                        ChangeLoginPassword.showError("Old login and/or password are incorrect!");
                        this.setOldLoginPasswordError(true);
                    }
                } else {
                    ChangeLoginPassword.showError("Oops! You have internet or server problems!");
                }
            }
        };
        request.send();
    }
}

class MainSettings {
    constructor() {
        this.changeLoginPassword = new ChangeLoginPassword();
    }
}

//noinspection ES6ConvertVarToLetConst
var mainSettings;
events.addEventListenerOnce("loadingContentEnded", () => {
    mainSettings = new MainSettings();
});
events.addEventListenerOnce("finalizeContent", () => {
    mainSettings = undefined;
});

