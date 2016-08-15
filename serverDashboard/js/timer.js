'use strict';
class Timer {
    constructor() {
        setInterval(function () {
            document.querySelectorAll('[data-timer="1"]').forEach((element, i, list) => {
                element.innerHTML = moment(element.getAttribute("data-time"), "YYYYMMDD-hh:mm").fromNow()
            });
        }, 10000);
    }
}