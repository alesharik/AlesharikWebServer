'use strict';
class Timer {
    constructor() {
        setInterval(() => {
            document.querySelectorAll('[data-timer="1"]').forEach((element) => {
                element.innerHTML = moment(element.getAttribute("data-time"), "YYYYMMDD-hh:mm").fromNow()
            });
        }, 10000);
        setInterval(() => {
            document.querySelectorAll("[data-clock='true']").forEach((element) => {
                element.innerHTML = moment().format("hh:mm:ss");
            })
        }, 1000);
    }
}