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
var dashboardCPUPlotIntervalId;
var diskUpdateIntervalId;
var jvmChartsUpdateIntervalId;
// var cpuUpdateIntervalId;

events.addEventListener("loadingContentEnded", () => {

    /**
     * Background color sets. Count is 6
     * @type {[*]}
     */
    const backgroundColors = ['rgba(255, 99, 132, 0.2)', 'rgba(54, 162, 235, 0.2)', 'rgba(255, 206, 86, 0.2)', 'rgba(75, 192, 192, 0.2)', 'rgba(153, 102, 255, 0.2)', 'rgba(255, 159, 64, 0.2)'];
    /**
     * Border color sets. Count is 6
     * @type {[*]}
     */
    const borderColors = ['rgba(255,99,132,1)', 'rgba(54, 162, 235, 1)', 'rgba(255, 206, 86, 1)', 'rgba(75, 192, 192, 1)', 'rgba(153, 102, 255, 1)', 'rgba(255, 159, 64, 1)'];

    //====================Static functions====================\\

    function generateRandomColor() {
        return "rgba(" + Math.floor(Math.random() * 255) + ", " + Math.floor(Math.random() * 255) + ", " + Math.floor(Math.random() * 255) + ", 0.2)"
    }

    function generateBorderForColor(color) {
        return color.replace("0.2", "1");
    }

    function warpSubtract(a, b) {
        return (a > b) ? a - b : 0
    }

    /**
     * Generate cpu data
     * @param {int} cpu cpu id
     * @param {Array} last lastCpuData generated data
     * @return {Array} new data
     */
    function generateData(cpu, last) {
        const newCpuLoad = dashboard.currentCompInfo.cpuLoad[cpu];

        const usertime = newCpuLoad[0], nicetime = newCpuLoad[1], systemtime = newCpuLoad[2], idletime = newCpuLoad[3];
        const ioWait = newCpuLoad[4], irq = newCpuLoad[5], softIrq = newCpuLoad[6];
        const totaltime = usertime + nicetime + systemtime + irq + softIrq + idletime + ioWait;

        let newRetData = [];
        newRetData[0] = warpSubtract(usertime, last[0]);
        newRetData[1] = warpSubtract(nicetime, last[1]);
        newRetData[2] = warpSubtract(systemtime, last[2]);
        newRetData[3] = warpSubtract(idletime, last[3]);
        newRetData[4] = warpSubtract(ioWait, last[4]);
        newRetData[5] = warpSubtract(irq, last[5]);
        newRetData[6] = warpSubtract(softIrq, last[6]);
        newRetData[7] = warpSubtract(totaltime, last[7]);
        return newRetData;
    }

    //====================Init and create other functions====================\\
    const cpuCount = dashboard.currentCompInfo.cpuCount;

    /**
     * Last cpu data
     * @type {Array}
     */
    let lastCpuData = [];
    for (let j = 0; j < cpuCount; j++) {
        lastCpuData[j] = generateData(j, [0, 0, 0, 0, 0, 0, 0, 0, 0]);
    }

    /**
     * Current cpu data
     * @type {Array}
     */
    let cpuData = [];

    /**
     * Labels for cpu chart
     * @type {Array}
     */
    let cpuLabels = [];
    for (let i = 0; i < cpuCount; i++) {
        cpuLabels[i] = "Cpu " + i;
    }

    /**
     * @type {jQuery}
     */
    let diskDatatable = $('#diskDatatable').dataTable().api();

    const ram = dashboard.currentCompInfo.ram;
    /**
     * @type {Chart}
     */
    let memoryChart = new Chart(document.querySelector("#MemoryPlot").getContext("2d"), {
        type: "doughnut",
        options: {
            responsive: true,
            maintainAspectRatio: false,
            tooltips: {
                callbacks: {
                    label: function (tooltipItems, data) {
                        return data.datasets[0].data[tooltipItems.index] + ' MB';
                    }
                }
            }
        },
        data: {
            labels: [
                "Free",
                "Shared",
                "Buffer",
                "Used"
            ]
        }
    });

    /**
     * @type {Chart}
     */
    let cpuChart = new Chart(document.querySelector("#CPUPlot").getContext("2d"), {
        type: "bar",
        data: {
            labels: cpuLabels
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                xAxes: [
                    {
                        stacked: true
                    }
                ],
                yAxes: [
                    {
                        stacked: true,
                        ticks: {
                            beginAtZero: true
                        }
                    }
                ]
            },
            tooltips: {
                callbacks: {
                    label: function (tooltipItems, data) {
                        return tooltipItems.yLabel + '%';
                    }
                }
            }
        }
    });


    //====================Set datasets====================\\

    cpuChart.data.datasets = [
        {
            label: "User time",
            data: cpuData[0],
            backgroundColor: backgroundColors[0],
            borderColor: borderColors[0],
            borderWidth: 1
        },
        {
            label: "Nice time",
            data: cpuData[1],
            backgroundColor: backgroundColors[1],
            borderColor: borderColors[1],
            borderWidth: 1
        },
        {
            label: "System time",
            data: cpuData[2],
            backgroundColor: backgroundColors[2],
            borderColor: borderColors[2],
            borderWidth: 1
        },
        {
            label: "IO Wait",
            data: cpuData[4],
            backgroundColor: backgroundColors[3],
            borderColor: borderColors[3],
            borderWidth: 1
        },
        {
            label: "IRQ",
            data: cpuData[5],
            backgroundColor: backgroundColors[4],
            borderColor: borderColors[4],
            borderWidth: 1
        },
        {
            label: "Soft IRQ",
            data: cpuData[6],
            backgroundColor: backgroundColors[5],
            borderColor: borderColors[5],
            borderWidth: 1
        }
    ];

    memoryChart.data.datasets = [
        {
            data: [
                Math.round(ram[1] / 1048576), //1048576 - 1024 * 1024 - 1mb
                Math.round(ram[2] / 1048576), //1048576 - 1024 * 1024 - 1mb
                Math.round(ram[3] / 1048576), //1048576 - 1024 * 1024 - 1mb
                Math.round((ram[0] - ram[1] - ram[2] - ram[3]) / 1048576) //1048576 - 1024 * 1024 - 1mb
            ],
            backgroundColor: [
                backgroundColors[0],
                backgroundColors[1],
                backgroundColors[2],
                backgroundColors[3]
            ],
            borderColor: [
                borderColors[0],
                borderColors[1],
                borderColors[2],
                borderColors[3]
            ],
            borderWidth: 1
        }
    ];
    cpuChart.update();
    memoryChart.update();

    function updateCpuAndRamCharts() {
        for (let j = 0; j < cpuCount; j++) {
            cpuData[j] = generateData(j, lastCpuData[j]);
        }
        new Parallel(JSON.stringify({cpuCount: cpuCount, cpuData: cpuData, ram: ram})).spawn(dataString => {
            let data = JSON.parse(dataString);
            let cpuCount = data.cpuCount;
            let cpuData = data.cpuData;
            let cpuDatasets = [];
            for (let i = 0; i < 6; i++) {
                cpuDatasets.push({});
            }
            let ram = data.ram;

            for (let i = 0, j = 0; i < 7; i++) {
                if (i === 3) {
                    continue; //Don't add ALL cpu time
                }

                let elem = cpuDatasets[j];
                let arr = [];
                for (let k = 0; k < cpuCount; k++) {
                    arr.push(Math.floor(cpuData[k][i] / cpuData[k][7] * 1000) / 10);
                }
                elem.data = arr;
                j++;
            }

            let memoryData = [
                Math.round(ram[1] / 1048576), //1048576 - 1024 * 1024 - 1mb
                Math.round(ram[2] / 1048576), //1048576 - 1024 * 1024 - 1mb
                Math.round(ram[3] / 1048576), //1048576 - 1024 * 1024 - 1mb
                Math.round((ram[0] - ram[1] - ram[2] - ram[3]) / 1048576) //1048576 - 1024 * 1024 - 1mb
            ];

            const ramWithIndex4 = ram[4];
            const ramWithIndex5 = ram[5];

            const ramWithIndex4MinusRamWithIndex5 = (ramWithIndex4 - ramWithIndex5);

            let maxSpace = Math.round(ramWithIndex4 / 1073741824 * 100) / 100;
            let usedPercent = (ramWithIndex4MinusRamWithIndex5 / ramWithIndex4 * 100);
            let freePercent = (ramWithIndex5 / ramWithIndex4 * 100);
            let usedGb = Math.round(ramWithIndex4MinusRamWithIndex5 / 1073741824 * 100) / 100;
            let freeGb = Math.round(ramWithIndex5 / 1073741824 * 100) / 100;

            return JSON.stringify({
                cpuDatasets: cpuDatasets,
                memoryData: memoryData,
                usedPercent: usedPercent,
                freePercent: freePercent,
                maxSpace: maxSpace,
                usedGb: usedGb,
                freeGb: freeGb
            });
        }).then(dataString => {
            let data = JSON.parse(dataString);
            let cpuDatasets = data.cpuDatasets;
            for (let i = 0; i < 6; i++) {
                cpuChart.data.datasets[i].data = cpuDatasets[i].data;
            }
            // cpuChart.data.datasets = data.cpuDatasets;
            memoryChart.data.datasets[0].data = data.memoryData;

            $("#swapProgressBarUsed").css("width", data.usedPercent + "%");
            if (data.usedPercent > 30) {
                $("#swapProgressBarUsed").text(`Used space: ${data.usedGb}G/${data.maxSpace}G`);
            }
            $("#swapProgressBarFree").css("width", data.freePercent + "%");
            if (data.freePercent) {
                $("#swapProgressBarFree").text(`Free space: ${data.freeGb}G/${data.maxSpace}G`);
            }

            memoryChart.update();
            cpuChart.update();

            lastCpuData = cpuData;
        });
    };


    const partitions = dashboard.currentCompInfo.partitions;
    /**
     * Update disk table
     */
    let diskUpdate = () => {
        diskDatatable.clear();
        let array = [];
        for (let i = 0; i < partitions.length; i++) {
            let partition = partitions[i];
            let used = Math.floor(partition.freeSize / 1073741824 * 100) / 100;
            if (used < 0) {
                used = 0;
            }

            array.push([
                partition.name,
                partition.address,
                partition.type,
                Math.floor(partition.maxSize / 1073741824 * 100) / 100,
                Math.floor(partition.usedSize / 1073741824 * 100) / 100,
                used,
                ((partition.inodes < 0) ? "device not mounted" : partition.inodes),
                ((partition.inodesFree < 0) ? "device not mounted" : partition.inodesFree)
            ]);
        }
        diskDatatable.rows.add(array);
        diskDatatable.draw();
    };

    //====================Start CPU and RAM animation====================\\

    updateCpuAndRamCharts();
    diskUpdate();
    dashboardCPUPlotIntervalId = setInterval(updateCpuAndRamCharts, 2000);
    diskUpdateIntervalId = setInterval(diskUpdate, 10 * 1000);

    const java = dashboard.currentCompInfo.java;

    const jvmCpuEnabled = java.cpuUsage.isSupported;
    const jvmGCEnabled = java.gc.length > 0;

    //====================All Max hold values and it's setters====================\\

    let jvmCpuMaxHold = 60;
    let jvmGCMaxHold = 60;

    let jvmHeapMaxHold = 60;
    let jvmNonHeapMaxHold = 60;

    let jvmCodeCacheMaxHold = 60;
    let jvmMetaspaceMaxHold = 60;
    let jvmCompressedClassSpaceMaxHold = 60;
    let jvmPSEdenSpaceMaxHold = 60;

    $("#JVMCPUButtons").find("li").click((event) => {
        jvmCpuMaxHold = event.target.dataset.time;
        document.querySelector("#JVMCPUButtons > button").innerHTML = event.target.innerHTML;
    });

    $("#JVMGCButtons").find("li").click((event) => {
        jvmGCMaxHold = event.target.dataset.time;
        document.querySelector("#JVMGCButtons > button").innerHTML = event.target.innerHTML;
    });

    $("#JVMHeapButtons").find("li").click((event) => {
        jvmHeapMaxHold = event.target.dataset.time;
        document.querySelector("#JVMHeapButtons > button").innerHTML = event.target.innerHTML;
    });

    $("#JVMNonHeapButtons").find("li").click((event) => {
        jvmNonHeapMaxHold = event.target.dataset.time;
        document.querySelector("#JVMNonHeapButtons > button").innerHTML = event.target.innerHTML;
    });

    $("#JVMCodeCacheButtons").find("li").click((event) => {
        jvmCodeCacheMaxHold = event.target.dataset.time;
        document.querySelector("#JVMCodeCacheButtons > button").innerHTML = event.target.innerHTML;
    });

    $("#JVMMetaspaceButtons").find("li").click((event) => {
        jvmMetaspaceMaxHold = event.target.dataset.time;
        document.querySelector("#JVMMetaspaceButtons > button").innerHTML = event.target.innerHTML;
    });

    $("#JVMCompressedClassSpaceButtons").find("li").click((event) => {
        jvmPSEdenSpaceMaxHold = event.target.dataset.time;
        document.querySelector("#JVMCompressedClassSpaceButtons > button").innerHTML = event.target.innerHTML;
    });

    $("#JVMPSEdenSpaceButtons").find("li").click((event) => {
        jvmPSEdenSpaceMaxHold = event.target.dataset.time;
        document.querySelector("#JVMPSEdenSpaceButtons > button").innerHTML = event.target.innerHTML;
    });

    //====================End====================\\

    /**
     * Cpu chart
     * @type {Chart | undefined}
     */
    let jvmCpuChart;

    let lastJvmUptime = 0;
    let labelIForCPU = 0;
    let lastJvmCpuData = [];

    let labelIForGC = 0;
    let lastGCCounts = [];
    let lastGCTime = [];
    let gcData = [];

    let lastIForHeap = 0;
    let lastIForNonHeap = 0;

    for (let i = 0; i < java.gc.length; i++) {
        gcData.push({
            label: (java.gc[i].name + " GC activity" + ((java.gc[i].isValid) ? "" : "(invalid)")),
            backgroundColor: (i < 6) ? backgroundColors[i] : generateRandomColor(),
            borderColor: (i < 6) ? borderColors[i] : generateBorderForColor(this.backgroundColor),
            borderWidth: 1
        });
        lastGCCounts.push(java.gc[i].gcCount);
        lastGCTime.push(java.gc[i].gcTime);
    }
    let jvmGCChart;

    for (let i = 0; i < java.cpuUsage.count; i++) {
        lastJvmCpuData.push(0);
    }

    if (!jvmCpuEnabled) {
        let canvas = document.querySelector("#JVMCPUChart");
        canvas.style.display = "none";
        canvas.parentNode.querySelector("div").style.display = 'block';
        jvmCpuChart = undefined;
    } else {
        jvmCpuChart = new Chart(document.querySelector("#JVMCPUChart").getContext("2d"), {
            type: 'line',
            data: {
                datasets: [{
                    label: "CPU usage",
                    backgroundColor: 'rgba(255, 99, 132, 0.2)',
                    borderColor: 'rgba(255,99,132,1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                bezierCurve: false,
                elements: {
                    point: {
                        radius: 0
                    }
                },
                tooltips: {
                    mode: 'index',
                    intersect: false,
                    callbacks: {
                        label: function (tooltipItems) {
                            return tooltipItems.yLabel + '%';
                        }
                    }
                },
                scales: {
                    xAxes: [{
                        display: true,
                        scaleLabel: {
                            show: true,
                        }
                    }],
                    yAxes: [{
                        display: true,
                        scaleLabel: {
                            show: true,
                        },
                        ticks: {
                            beginAtZero: true
                        }
                    }]
                }
            }
        });
    }

    if (!jvmGCEnabled) {
        let canvas = document.querySelector("#JVMGCChart");
        canvas.style.display = "none";
        canvas.parentNode.querySelector("div").style.display = 'block';
        jvmGCChart = undefined;
    } else {
        jvmGCChart = new Chart(document.querySelector("#JVMGCChart").getContext("2d"), {
            type: 'line',
            data: {
                datasets: gcData
            },
            options: {
                responsive: true,
                bezierCurve: false,
                elements: {
                    point: {
                        radius: 0
                    }
                },
                tooltips: {
                    mode: 'index',
                    intersect: false,
                    callbacks: {
                        label: function (tooltipItems) {
                            return tooltipItems.yLabel + '%';
                        }
                    }
                },
                scales: {
                    xAxes: [{
                        display: true,
                        scaleLabel: {
                            show: true,
                        }
                    }],
                    yAxes: [{
                        display: true,
                        scaleLabel: {
                            show: true,
                        },
                        ticks: {
                            beginAtZero: true
                        }
                    }]
                }
            }
        });
    }

    let jvmHeapChart = new Chart(document.querySelector("#JVMHEapChart").getContext("2d"), {
        type: 'line',
        data: {
            datasets: [
                {
                    label: "committed",
                    backgroundColor: backgroundColors[1],
                    borderColor: borderColors[1],
                    borderWidth: 1,
                    fill: false
                },
                {
                    label: "init",
                    backgroundColor: backgroundColors[2],
                    borderColor: borderColors[2],
                    borderWidth: 1,
                    fill: false
                },
                {
                    label: "used",
                    backgroundColor: backgroundColors[3],
                    borderColor: borderColors[3],
                    borderWidth: 1,
                    fill: false
                }
            ]
        },
        options: {
            responsive: true,
            bezierCurve: false,
            elements: {
                point: {
                    radius: 0
                }
            },
            tooltips: {
                mode: 'index',
                intersect: false,
                callbacks: {
                    label: function (tooltipItems) {
                        return tooltipItems.yLabel + ' MB';
                    }
                }
            },
            scales: {
                xAxes: [{
                    display: true,
                    scaleLabel: {
                        show: true,
                    }
                }],
                yAxes: [{
                    display: true,
                    scaleLabel: {
                        show: true,
                    },
                    ticks: {
                        beginAtZero: true
                    }
                }]
            }
        }
    });

    let jvmNonHeapChart = new Chart(document.querySelector("#JVMNonHeapChart").getContext("2d"), {
        type: 'line',
        data: {
            datasets: [
                {
                    label: "committed",
                    backgroundColor: backgroundColors[1],
                    borderColor: borderColors[1],
                    borderWidth: 1,
                    fill: false
                },
                {
                    label: "init",
                    backgroundColor: backgroundColors[2],
                    borderColor: borderColors[2],
                    borderWidth: 1,
                    fill: false
                },
                {
                    label: "used",
                    backgroundColor: backgroundColors[3],
                    borderColor: borderColors[3],
                    borderWidth: 1,
                    fill: false
                }
            ]
        },
        options: {
            responsive: true,
            bezierCurve: false,
            elements: {
                point: {
                    radius: 0
                }
            },
            tooltips: {
                mode: 'index',
                intersect: false,
                callbacks: {
                    label: function (tooltipItems, data) {
                        return tooltipItems.yLabel + ' MB';
                    }
                }
            },
            scales: {
                xAxes: [{
                    display: true,
                    scaleLabel: {
                        show: true,
                    }
                }],
                yAxes: [{
                    display: true,
                    scaleLabel: {
                        show: true,
                    },
                    ticks: {
                        beginAtZero: true
                    }
                }]
            }
        }
    });

    let jvmCodeCacheChart = new Chart(document.querySelector("#JVMCodeCacheChart").getContext("2d"), {
        type: 'line',
        data: {
            datasets: [
                {
                    label: "committed",
                    backgroundColor: backgroundColors[1],
                    borderColor: borderColors[1],
                    borderWidth: 1,
                    fill: false
                },
                {
                    label: "init",
                    backgroundColor: backgroundColors[2],
                    borderColor: borderColors[2],
                    borderWidth: 1,
                    fill: false
                },
                {
                    label: "used",
                    backgroundColor: backgroundColors[3],
                    borderColor: borderColors[3],
                    borderWidth: 1,
                    fill: false
                }
            ]
        },
        options: {
            responsive: true,
            bezierCurve: false,
            elements: {
                point: {
                    radius: 0
                }
            },
            tooltips: {
                mode: 'index',
                intersect: false,
                callbacks: {
                    label: function (tooltipItems, data) {
                        return tooltipItems.yLabel + ' MB';
                    }
                }
            },
            scales: {
                xAxes: [{
                    display: true,
                    scaleLabel: {
                        show: true,
                    }
                }],
                yAxes: [{
                    display: true,
                    scaleLabel: {
                        show: true,
                    },
                    ticks: {
                        beginAtZero: true
                    }
                }]
            }
        }
    });

    let jvmMetaspaceChart = new Chart(document.querySelector("#JVMMetaspaceChart").getContext("2d"), {
        type: 'line',
        data: {
            datasets: [
                {
                    label: "committed",
                    backgroundColor: backgroundColors[1],
                    borderColor: borderColors[1],
                    borderWidth: 1,
                    fill: false
                },
                {
                    label: "used",
                    backgroundColor: backgroundColors[3],
                    borderColor: borderColors[3],
                    borderWidth: 1,
                    fill: false
                }
            ]
        },
        options: {
            responsive: true,
            bezierCurve: false,
            elements: {
                point: {
                    radius: 0
                }
            },
            tooltips: {
                mode: 'index',
                intersect: false,
                callbacks: {
                    label: function (tooltipItems, data) {
                        return tooltipItems.yLabel + ' MB';
                    }
                }
            },
            scales: {
                xAxes: [{
                    display: true,
                    scaleLabel: {
                        show: true,
                    }
                }],
                yAxes: [{
                    display: true,
                    scaleLabel: {
                        show: true,
                    },
                    ticks: {
                        beginAtZero: true
                    }
                }]
            }
        }
    });

    let jvmCompressedClassSpaceChart = new Chart(document.querySelector("#JVMCompressedClassSpaceChart").getContext("2d"), {
        type: 'line',
        data: {
            datasets: [
                {
                    label: "committed",
                    backgroundColor: backgroundColors[1],
                    borderColor: borderColors[1],
                    borderWidth: 1,
                    fill: false
                },
                {
                    label: "used",
                    backgroundColor: backgroundColors[3],
                    borderColor: borderColors[3],
                    borderWidth: 1,
                    fill: false
                }
            ]
        },
        options: {
            responsive: true,
            bezierCurve: false,
            elements: {
                point: {
                    radius: 0
                }
            },
            tooltips: {
                mode: 'index',
                intersect: false,
                callbacks: {
                    label: function (tooltipItems, data) {
                        return tooltipItems.yLabel + ' MB';
                    }
                }
            },
            scales: {
                xAxes: [{
                    display: true,
                    scaleLabel: {
                        show: true,
                    }
                }],
                yAxes: [{
                    display: true,
                    scaleLabel: {
                        show: true,
                    },
                    ticks: {
                        beginAtZero: true
                    }
                }]
            }
        }
    });

    let jvmPSEdenSpaceChart = new Chart(document.querySelector("#JVMPSEdenSpaceChart").getContext("2d"), {
        type: 'line',
        data: {
            datasets: [
                {
                    label: "committed",
                    backgroundColor: backgroundColors[1],
                    borderColor: borderColors[1],
                    borderWidth: 1,
                    fill: false
                },
                {
                    label: "init",
                    backgroundColor: backgroundColors[2],
                    borderColor: borderColors[2],
                    borderWidth: 1,
                    fill: false
                },
                {
                    label: "used",
                    backgroundColor: backgroundColors[3],
                    borderColor: borderColors[3],
                    borderWidth: 1,
                    fill: false
                }
            ]
        },
        options: {
            responsive: true,
            bezierCurve: false,
            elements: {
                point: {
                    radius: 0
                }
            },
            tooltips: {
                mode: 'index',
                intersect: false,
                callbacks: {
                    label: function (tooltipItems, data) {
                        return tooltipItems.yLabel + ' MB';
                    }
                }
            },
            scales: {
                xAxes: [{
                    display: true,
                    scaleLabel: {
                        show: true,
                    }
                }],
                yAxes: [{
                    display: true,
                    scaleLabel: {
                        show: true,
                    },
                    ticks: {
                        beginAtZero: true
                    }
                }]
            }
        }
    });

    let lastIForCodeCache = 0;
    let lastIForMetaspace = 0;
    let lastIForCompressedClassSpace = 0;
    let lastIForPSEdenSpace = 0;

    let lastJvmCpuValue;
    let jvmChartsUpdate = () => {
        const java = dashboard.currentCompInfo.java;

        let elapsedTime = (java.uptime - lastJvmUptime) * 1000000;

        if (jvmCpuEnabled) {
            let currentElapsedCpuTime = 0;
            for (let i = 0; i < java.cpuUsage.count; i++) {
                currentElapsedCpuTime += (((java.cpuUsage.data[i] / (dashboard.currentCompInfo.processorCount)) - (lastJvmCpuData[i] / (dashboard.currentCompInfo.processorCount))));
            }
            let currentJvmCpuValue = elapsedTime > 0 ? Math.round(Math.min(1000 * currentElapsedCpuTime / elapsedTime, 1000) * 100) / 100 : 0;
            if (currentJvmCpuValue <= 0) {
                currentJvmCpuValue = lastJvmCpuValue;
            }
            jvmCpuChart.data.datasets[0].data.push(currentJvmCpuValue);
            jvmCpuChart.data.labels.push((labelIForCPU % 5 === 0) ? moment().format("hh:mm:ss") : "");

            while (jvmCpuChart.data.datasets[0].data.length > jvmCpuMaxHold) {
                jvmCpuChart.data.datasets[0].data.shift();
                jvmCpuChart.data.labels.shift();
            }


            labelIForCPU++;
            if (labelIForCPU >= 10) {
                labelIForCPU = 0;
            }

            jvmCpuChart.update();

            lastJvmCpuData = java.cpuUsage.data;
            lastJvmCpuValue = currentJvmCpuValue;
        }

        if (jvmGCEnabled) {

            for (let i = 0; i < java.gc.length; i++) {
                let has = java.gc[i].gcCount > lastGCCounts[i];
                if (has) {
                    let elapsedGCTime = (java.gc[i].gcTime / dashboard.currentCompInfo.processorCount * 1000000) - (lastGCTime[i] / dashboard.currentCompInfo.processorCount * 1000000);
                    let currentJvmGCValue = Math.round(Math.min(1000 * elapsedGCTime / elapsedTime, 1000) * 100) / 100;
                    if (currentJvmGCValue < 0) {
                        currentJvmGCValue = 0;
                    }
                    jvmGCChart.data.datasets[i].data.push(currentJvmGCValue);
                } else {
                    jvmGCChart.data.datasets[i].data.push(0);
                }
                lastGCCounts[i] = java.gc[i].gcCount;
                lastGCTime[i] = java.gc[i].gcTime;
            }

            jvmGCChart.data.labels.push((labelIForGC % 5 === 0) ? moment().format("hh:mm:ss") : "");

            while (jvmGCChart.data.datasets[0].data.length > jvmGCMaxHold) {
                for (let i = 0; i < java.gc.length; i++) {
                    jvmGCChart.data.datasets[i].data.shift();
                }
                jvmGCChart.data.labels.shift();
            }

            labelIForGC++;
            if (labelIForGC >= 10) {
                labelIForGC = 0;
            }

            jvmGCChart.update();

        }

        jvmHeapChart.data.datasets[0].data.push(Math.round(java.memory.heap.committed / (1024 * 1024)));
        jvmHeapChart.data.datasets[1].data.push(Math.round(java.memory.heap.init / (1024 * 1024)));
        jvmHeapChart.data.datasets[2].data.push(Math.round(java.memory.heap.used / (1024 * 1024)));

        jvmHeapChart.data.labels.push((lastIForHeap % 5 === 0) ? moment().format("hh:mm:ss") : "");

        while (jvmHeapChart.data.datasets[0].data.length > jvmCpuMaxHold) {
            for (let i = 0; i < 3; i++) {
                jvmHeapChart.data.datasets[i].data.shift();
            }
            jvmHeapChart.data.labels.shift();
        }

        lastIForHeap++;
        if (lastIForHeap >= 10) {
            lastIForHeap = 0;
        }

        jvmHeapChart.update();

        jvmNonHeapChart.data.datasets[0].data.push(Math.round(java.memory.nonHeap.committed / (1024 * 1024)));
        jvmNonHeapChart.data.datasets[1].data.push(Math.round(java.memory.nonHeap.init / (1024 * 1024)));
        jvmNonHeapChart.data.datasets[2].data.push(Math.round(java.memory.nonHeap.used / (1024 * 1024)));

        jvmNonHeapChart.data.labels.push((lastIForNonHeap % 5 === 0) ? moment().format("hh:mm:ss") : "");

        while (jvmNonHeapChart.data.datasets[0].data.length > jvmCpuMaxHold) {
            for (let i = 0; i < 3; i++) {
                jvmNonHeapChart.data.datasets[i].data.shift();
            }
            jvmNonHeapChart.data.labels.shift();
        }

        lastIForNonHeap++;
        if (lastIForNonHeap >= 10) {
            lastIForNonHeap = 0;
        }

        jvmNonHeapChart.update();

        let memoryPool = undefined;
        for (let i = 0; i < java.memory.memoryPools.length; i++) {
            if (java.memory.memoryPools[i].name === "Code Cache") {
                memoryPool = java.memory.memoryPools[i];
            }
        }
        jvmCodeCacheChart.data.datasets[0].data.push(Math.round(memoryPool.usage.committed / (1024 * 1024)));
        jvmCodeCacheChart.data.datasets[1].data.push(Math.round(memoryPool.usage.init / (1024 * 1024)));
        jvmCodeCacheChart.data.datasets[2].data.push(Math.round(memoryPool.usage.used / (1024 * 1024)));

        jvmCodeCacheChart.data.labels.push((lastIForCodeCache % 5 === 0) ? moment().format("hh:mm:ss") : "");

        while (jvmCodeCacheChart.data.datasets[0].data.length > jvmCodeCacheMaxHold) {
            for (let i = 0; i < 3; i++) {
                jvmCodeCacheChart.data.datasets[i].data.shift();
            }
            jvmCodeCacheChart.data.labels.shift();
        }

        lastIForCodeCache++;
        if (lastIForCodeCache >= 10) {
            lastIForCodeCache = 0;
        }

        jvmCodeCacheChart.update();

        let memoryPool1 = undefined;
        for (let i = 0; i < java.memory.memoryPools.length; i++) {
            if (java.memory.memoryPools[i].name === "Metaspace") {
                memoryPool1 = java.memory.memoryPools[i];
            }
        }
        jvmMetaspaceChart.data.datasets[0].data.push(Math.round(memoryPool1.usage.committed / (1024 * 1024)));
        jvmMetaspaceChart.data.datasets[1].data.push(Math.round(memoryPool1.usage.used / (1024 * 1024)));

        jvmMetaspaceChart.data.labels.push((lastIForMetaspace % 5 === 0) ? moment().format("hh:mm:ss") : "");

        while (jvmMetaspaceChart.data.datasets[0].data.length > jvmMetaspaceMaxHold) {
            for (let i = 0; i < 2; i++) {
                jvmMetaspaceChart.data.datasets[i].data.shift();
            }
            jvmMetaspaceChart.data.labels.shift();
        }

        lastIForMetaspace++;
        if (lastIForMetaspace >= 10) {
            lastIForMetaspace = 0;
        }

        jvmMetaspaceChart.update();

        let memoryPool2 = undefined;
        for (let i = 0; i < java.memory.memoryPools.length; i++) {
            if (java.memory.memoryPools[i].name === "Compressed Class Space") {
                memoryPool2 = java.memory.memoryPools[i];
            }
        }
        jvmCompressedClassSpaceChart.data.datasets[0].data.push(Math.round(memoryPool2.usage.committed / (1024 * 1024)));
        jvmCompressedClassSpaceChart.data.datasets[1].data.push(Math.round(memoryPool2.usage.used / (1024 * 1024)));

        jvmCompressedClassSpaceChart.data.labels.push((lastIForCompressedClassSpace % 5 === 0) ? moment().format("hh:mm:ss") : "");

        while (jvmCompressedClassSpaceChart.data.datasets[0].data.length > jvmCompressedClassSpaceMaxHold) {
            for (let i = 0; i < 2; i++) {
                jvmCompressedClassSpaceChart.data.datasets[i].data.shift();
            }
            jvmCompressedClassSpaceChart.data.labels.shift();
        }

        lastIForCompressedClassSpace++;
        if (lastIForCompressedClassSpace >= 10) {
            lastIForCompressedClassSpace = 0;
        }

        jvmCompressedClassSpaceChart.update();

        for (let i = 0; i < java.memory.memoryPools.length; i++) {
            if (java.memory.memoryPools[i].name === "PS Eden Space") {
                memoryPool2 = java.memory.memoryPools[i];
            }
        }
        jvmPSEdenSpaceChart.data.datasets[0].data.push(Math.round(memoryPool2.usage.committed / (1024 * 1024)));
        jvmPSEdenSpaceChart.data.datasets[1].data.push(Math.round(memoryPool2.usage.init / (1024 * 1024)));
        jvmPSEdenSpaceChart.data.datasets[2].data.push(Math.round(memoryPool2.usage.used / (1024 * 1024)));

        jvmPSEdenSpaceChart.data.labels.push((lastIForPSEdenSpace % 5 === 0) ? moment().format("hh:mm:ss") : "");

        while (jvmPSEdenSpaceChart.data.datasets[0].data.length > jvmPSEdenSpaceMaxHold) {
            for (let i = 0; i < 3; i++) {
                jvmPSEdenSpaceChart.data.datasets[i].data.shift();
            }
            jvmPSEdenSpaceChart.data.labels.shift();
        }

        lastIForPSEdenSpace++;
        if (lastIForPSEdenSpace >= 10) {
            lastIForPSEdenSpace = 0;
        }

        jvmPSEdenSpaceChart.update();

        lastJvmUptime = java.uptime;
    };

    jvmChartsUpdateIntervalId = setInterval(jvmChartsUpdate, 1000);
    jvmChartsUpdate();

    events.removeEventListener("loadingContentEnded", this);
});
events.addEventListener("finalizeContent", () => {
    clearInterval(dashboardCPUPlotIntervalId);
    clearInterval(diskUpdateIntervalId);
    clearInterval(jvmChartsUpdateIntervalId);
    events.removeEventListener("finalizeContent", this);
});
