'use strict';
let text = "";

let canvas;
let columnWidth;
let heightDividedByTwo;
let state = 4; //State machine control variable: 0 - start, 1 - start: second stage, 2 - run, 3 - end, 4 - disable
function setup() {
    canvas = document.querySelector("#backgroundCanvas");
    let audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    let audioElement = document.getElementById('audio');
    let audioSrc = audioCtx.createMediaElementSource(audioElement);
    let analyser = audioCtx.createAnalyser();
    let ctx = canvas.getContext("2d");


    let frequencyData = new Uint8Array(600);
    columnWidth = canvas.width / 600;
    heightDividedByTwo = canvas.height / 2;

    ctx.fillStyle = "rgb(0, 255, 0)";
    ctx.strokeStyle = "rgb(0, 255, 0)";
    ctx.font = "48px serif";
    ctx.textAlign = "center";

    audioSrc.connect(analyser);
    audioSrc.connect(audioCtx.destination);

    let pastDate = +new Date();
    let isTimeSettedForFirst = false;
    let isTimeSettedForSecond = false;
    let isTimeSettedForThird = false;
    let date = +new Date();

    const MOVE_TIME = 600;

    function renderChart() {
        requestAnimationFrame(renderChart);
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        if (state == 2) {
            analyser.getByteFrequencyData(frequencyData);
            ctx.moveTo(0, frequencyData[0] + heightDividedByTwo);

            ctx.beginPath();
            frequencyData.forEach((num, i, arr) => {
                if (num == 600) {
                    return;
                }
                ctx.quadraticCurveTo(i * columnWidth, heightDividedByTwo + num
                    , (i * columnWidth + (i + 1) * columnWidth) / 2
                    , ((heightDividedByTwo + num) + (heightDividedByTwo + arr[i + 1])) / 2);
            });
            ctx.stroke();

            ctx.beginPath();
            ctx.moveTo(0, heightDividedByTwo - frequencyData[0]);
            frequencyData.forEach((num, i, arr) => {
                if (num == 600) {
                    return;
                }
                ctx.quadraticCurveTo(i * columnWidth, heightDividedByTwo - num
                    , (i * columnWidth + (i + 1) * columnWidth) / 2
                    , ((heightDividedByTwo - num) + (heightDividedByTwo - arr[i + 1])) / 2);
            });
            ctx.stroke();

            ctx.beginPath();
            ctx.fillText(text, canvas.width / 2, heightDividedByTwo);
            ctx.stroke();
        } else if (state == 0) {
            date = +new Date();
            if (!isTimeSettedForFirst) {
                pastDate = +new Date();
                isTimeSettedForFirst = true;
            }
            if (date > pastDate + MOVE_TIME) {
                state = 1;
                isTimeSettedForFirst = false;
                return;
            }

            ctx.beginPath();
            ctx.moveTo(0, heightDividedByTwo);
            ctx.lineTo(Math.round(canvas.width / 2 * ((date - pastDate) / MOVE_TIME)), heightDividedByTwo);
            ctx.stroke();

            ctx.beginPath();
            ctx.moveTo(canvas.width, heightDividedByTwo);
            ctx.lineTo(Math.round(canvas.width - (canvas.width / 2 * ((date - pastDate) / MOVE_TIME))), heightDividedByTwo);
            ctx.stroke();
        } else if (state == 1) {
            if (!isTimeSettedForSecond) {
                pastDate = +new Date();
                isTimeSettedForSecond = true;
            }
            let past = +new Date() - pastDate;
            if (past < 300) {
                ctx.beginPath();
                ctx.moveTo(0, heightDividedByTwo);
                ctx.lineTo(canvas.width, heightDividedByTwo);
                ctx.stroke();
            } else if (past > 500 && past < 700) {
                ctx.beginPath();
                ctx.moveTo(0, heightDividedByTwo);
                ctx.lineTo(canvas.width, heightDividedByTwo);
                ctx.stroke();
            } else if (past >= 700) {
                state = 2;
                isTimeSettedForSecond = false;
            }
        } else if (state == 3) {
            date = +new Date();
            if (!isTimeSettedForThird) {
                pastDate = +new Date();
                isTimeSettedForThird = true;
            }
            if (date > pastDate + MOVE_TIME) {
                state = 4;
                isTimeSettedForThird = false;
                return;
            }

            ctx.beginPath();
            ctx.moveTo(0, heightDividedByTwo);
            ctx.lineTo(Math.round(canvas.width / 2 * (((pastDate + MOVE_TIME) - date) / MOVE_TIME)), heightDividedByTwo);
            ctx.stroke();

            ctx.beginPath();
            ctx.moveTo(canvas.width, heightDividedByTwo);
            ctx.lineTo(Math.round(canvas.width - (canvas.width / 2 * (((pastDate + MOVE_TIME) - date) / MOVE_TIME))), heightDividedByTwo);
            ctx.stroke();
        }
    }

    // let last = 0;
    // let lastI = 0;
    // function renderChart() {
    //     requestAnimationFrame(renderChart);
    //     ctx.clearRect(0, 0, canvas.width, canvas.height);
    //     ctx.beginPath();
    //
    //     // Copy frequency data to frequencyData array.
    //     analyser.getByteFrequencyData(frequencyData);
    //     // ctx.moveTo(0, frequencyData[0] + 300);
    //     //
    //     // last = 0;
    //     // lastI = 0;
    //     // frequencyData.forEach((num, i, arr) => {
    //     //     ctx.quadraticCurveTo(last + 300, lastI * 6, i * 6, num + 300);
    //     //     last = num;
    //     //     lastI = i;
    //     // });
    //     ctx.stroke();
    //     ctx.beginPath();
    //     ctx.moveTo(0, 300 - frequencyData[0]);
    //
    //     last = 0;
    //     lastI = 0;
    //     frequencyData.forEach((num, i, arr) => {
    //         ctx.quadraticCurveTo(500 - last, lastI * 2, i * 2, 500 - num);
    //         last = num;
    //         lastI = i;
    //     });
    //
    //     ctx.stroke();
    //     ctx.beginPath();
    //     ctx.moveTo(0, 300);
    //     ctx.lineTo(1200, 300);
    //     ctx.stroke();
    // }

    renderChart();
}

function start() {
    state = 0;
}

function end() {
    state = 3;
}

function update() {
    columnWidth = canvas.width / 600;
    heightDividedByTwo = canvas.height / 2;
}

function setText(textt) {
    text = textt;
}


//TODO remove this
function test() {
    document.getElementById("audio").onplay = function () {
        start();
    };
    document.getElementById("audio").onpause = function () {
        end();
    }
}