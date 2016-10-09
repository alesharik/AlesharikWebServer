'use strict';
class MainSettings {
    constructor() {
        this.changeLoginPassword = new ChangeLoginPassword();
    }
}

class ChangeLoginPassword {
    constructor() {

    }

    /**
     *
     * @param {boolean} isError
     */
    setOldLoginPasswordError(isError) {
        if (isError) {
            $("#oldLogin").addClass("form-control-danger");
            $("#oldPassword").addClass("form-control-danger");
        } else {
            $("#oldLogin").removeClass("form-control-danger");
            $("#oldPassword").removeClass("form-control-danger");
        }
    }

    /**
     * @param {string} error
     */
    showError(error) {
        document.querySelector("#loginPasswordChangeErrorModal .modal-body").innerHTML = error;
    }

    trySendChanges() {
        $('#loginPasswordChangeModal').modal({
            keyboard: false,
            show: true
        });
    }

    showSuccess() {
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
            + "&newPassword=" + document.querySelector("#newPassword").value, true);
        request.onreadystatechange = () => {
            if (request.readyState == 4) {
                if (request.status == 200) {
                    if (request.responseText == "ok") {
                        this.showSuccess();
                        this.setOldLoginPasswordError(false);
                    } else if (request.responseText == "oldLogPassError") {
                        this.showError("Old login and/or password are incorrect!");
                        this.setOldLoginPasswordError(true);
                    }
                } else {
                    this.showError("Oops! You have internet or server problems!");
                }
            }
        };
        request.send();
    }

}

var mainSettings;
events.addEventListener("loadingContentEnded", () => {
    mainSettings = new MainSettings();
});
events.addEventListener("finalizeContent", () => {
    mainSettings = undefined;

    events.removeEventListener("finalizeContent", this);
});

