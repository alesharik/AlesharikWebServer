'use strict';
var dashboardCPUPlotIntervalId;
var diskUpdateIntervalId;
var jvmChartsUpdateIntervalId;
// var cpuUpdateIntervalId;

events.addEventListener("loadingContentEnded", () => {
    class DataHolder {
        constructor() {
            this.items = [];
            this.max = 1000;
        }

        /**
         * @param {int} max
         */
        setMax(max) {
            this.max = max;
        }

        pushData(data) {
            this.items.push(data);
            if (this.items.length > this.max) {
                while (this.items.length > this.max) {
                    this.items.shift();
                }
            }
        }
    }

    let backgroundColors = ['rgba(255, 99, 132, 0.2)', 'rgba(54, 162, 235, 0.2)', 'rgba(255, 206, 86, 0.2)', 'rgba(75, 192, 192, 0.2)', 'rgba(153, 102, 255, 0.2)', 'rgba(255, 159, 64, 0.2)'];
    let borderColors = ['rgba(255,99,132,1)', 'rgba(54, 162, 235, 1)', 'rgba(255, 206, 86, 1)', 'rgba(75, 192, 192, 1)', 'rgba(153, 102, 255, 1)', 'rgba(255, 159, 64, 1)'];

    function warpSubtract(a, b) {
        return (a > b) ? a - b : 0
    }

    console.log("asd");
    function generateData(cpu, last) {
        if (last == undefined) {
            last = [];
            for (let j = 0; j < dashboard.currentCompInfo.cpuCount; j++) {
                last[j] = generateData(j, [0, 0, 0, 0, 0, 0, 0, 0, 0]);
            }
        }

        let load = dashboard.currentCompInfo.cpuLoad;



        let usertime = load[cpu][0], nicetime = load[cpu][1], systemtime = load[cpu][2], idletime = load[cpu][3];
        let ioWait = load[cpu][4], irq = load[cpu][5], softIrq = load[cpu][6];
        let totaltime = usertime + nicetime + systemtime + irq + softIrq + idletime + ioWait;

        let neww = [];
        neww[0] = warpSubtract(usertime, last[0]);
        neww[1] = warpSubtract(nicetime, last[1]);
        neww[2] = warpSubtract(systemtime, last[2]);
        neww[3] = warpSubtract(idletime, last[3]);
        neww[4] = warpSubtract(ioWait, last[4]);
        neww[5] = warpSubtract(irq, last[5]);
        neww[6] = warpSubtract(softIrq, last[6]);
        neww[7] = warpSubtract(totaltime, last[7]);
        // neww[7] = neww[0] + neww[1] + neww[2] + neww[3] + neww[4] + neww[5] + neww[6];
        return neww;
    }

    let last = [];
    for (let j = 0; j < dashboard.currentCompInfo.cpuCount; j++) {
        last[j] = generateData(j, [0, 0, 0, 0, 0, 0, 0, 0, 0]);
    }

    let diskDatatable = $('#diskDatatable').dataTable().api();

    let memoryChart = new Chart(document.querySelector("#MemoryPlot").getContext("2d"), {
        type: "doughnut",
        options: {
            responsive: true,
            maintainAspectRatio: false
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


    let cpuLabels = [];
    for (let i = 0; i < dashboard.currentCompInfo.cpuCount; i++) {
        cpuLabels[i] = "Cpu " + i;
    }
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
            }
        }
    });


    let cpuData = [];
    let updateCpu = () => {
        for (let j = 0; j < dashboard.currentCompInfo.cpuCount; j++) {
            cpuData[j] = generateData(j, last[j]);
        }
    };
    // cpuUpdateIntervalId = setInterval(updateCpu, 1000);
    // updateCpu();

    cpuChart.data.datasets = [
        {
            label: "User time",
            data: cpuData[0],
            backgroundColor: 'rgba(255, 99, 132, 0.2)',
            borderColor: 'rgba(255,99,132,1)',
            borderWidth: 1
        },
        {
            label: "Nice time",
            data: cpuData[1],
            backgroundColor: 'rgba(54, 162, 235, 0.2)',
            borderColor: 'rgba(54, 162, 235, 1)',
            borderWidth: 1
        },
        {
            label: "System time",
            data: cpuData[2],
            backgroundColor: 'rgba(255, 206, 86, 0.2)',
            borderColor: 'rgba(255, 206, 86, 1)',
            borderWidth: 1
        },
        {
            label: "IO Wait",
            data: cpuData[4],
            backgroundColor: 'rgba(75, 192, 192, 0.2)',
            borderColor: 'rgba(75, 192, 192, 1)',
            borderWidth: 1
        },
        {
            label: "IRQ",
            data: cpuData[5],
            backgroundColor: 'rgba(153, 102, 255, 0.2)',
            borderColor: 'rgba(153, 102, 255, 1)',
            borderWidth: 1
        },
        {
            label: "Soft IRQ",
            data: cpuData[6],
            backgroundColor: 'rgba(255, 159, 64, 0.2)',
            borderColor: 'rgba(255, 159, 64, 1)',
            borderWidth: 1
        }
    ];

    // cpuChart.data.datasets = [

    // ];

    let ram = dashboard.currentCompInfo.ram;
    memoryChart.data.datasets = [
        {
            data: [Math.round(ram[1] / (1024 * 1024)), Math.round(ram[2] / (1024 * 1024)), Math.round(ram[3] / (1024 * 1024)), Math.round((ram[0] - (ram[1] + ram[2] + ram[3])) / (1024 * 1024))],
            backgroundColor: [
                "rgba(255, 99, 132, 0.2)",
                "rgba(54, 162, 235, 0.2)",
                "rgba(255, 206, 86, 0.2)",
                "rgba(75, 192, 192, 0.2)"
            ],
            borderColor: [
                "rgba(255,99,132,1)",
                "rgba(54, 162, 235, 1)",
                "rgba(255, 206, 86, 1)",
                "rgba(75, 192, 192, 1)"
            ],
            borderWidth: 1
        }
    ];

    let main = () => {
        updateCpu();
        let counter0 = 0;
        for (let i = 0; i < 7; i++) {
            if (i == 3) {
                continue;
            }

            let elem = cpuChart.data.datasets[counter0];
            let arr = [];
            for (let j = 0; j < dashboard.currentCompInfo.cpuCount; j++) {
                arr.push(Math.floor(cpuData[j][i] / cpuData[j][7] * 1000) / 10);
            }
            elem.data = arr;

            counter0++;

            // console.table(cpuChart.data.datasets);
        }

        // let dataa = [];
        // let datasetsCopy = cpuChart.data.datasets;
        // datasetsCopy.forEach(elem => {
        //     dataa.push(elem.data);
        // });
        // console.table(dataa);

        cpuChart.update();
        last = cpuData;

        let ram = dashboard.currentCompInfo.ram;
        memoryChart.data.datasets[0].data = [Math.round(ram[1] / (1024 * 1024)), Math.round(ram[2] / (1024 * 1024)), Math.round(ram[3] / (1024 * 1024)), Math.round((ram[0] - (ram[1] + ram[2] + ram[3])) / (1024 * 1024))];

        memoryChart.update();
        let maxSpace = Math.round(ram[4] / (1024 * 1024 * 1024) * 100) / 100;
        let usedPercent = ((ram[4] - ram[5]) / ram[4] * 100);
        let freePercent = (ram[5] / ram[4] * 100);

        $("#swapProgressBarUsed").css("width", usedPercent + "%");
        if (usedPercent > 10) {
            $("#swapProgressBarUsed").text("Used space: " + Math.round((ram[4] - ram[5]) / (1024 * 1024 * 1024) * 100) / 100 + "G/" + maxSpace + "G");
        }
        $("#swapProgressBarFree").css("width", freePercent + "%");
        if (freePercent) {
            $("#swapProgressBarFree").text("Free space: " + Math.round(ram[5] / (1024 * 1024 * 1024) * 100) / 100 + "G/" + maxSpace + "G");
        }

        updateCpu();
    };

    let diskUpdate = () => {
        diskDatatable.clear();
        let array = [];
        for (let i = 0; i < dashboard.currentCompInfo.partitions.length; i++) {
            let partition = dashboard.currentCompInfo.partitions[i];
            let used = Math.floor(partition.freeSize / (1024 * 1024 * 1024) * 100) / 100;
            if (used < 0) {
                used = 0;
            }
            array.push([
                partition.name,
                partition.address,
                partition.type,
                Math.floor(partition.maxSize / (1024 * 1024 * 1024) * 100) / 100,
                Math.floor(partition.usedSize / (1024 * 1024 * 1024) * 100) / 100,
                used,
                ((partition.inodes < 0) ? "device not mounted" : partition.inodes),
                ((partition.inodesFree < 0) ? "device not mounted" : partition.inodesFree)
            ]);
        }
        diskDatatable.rows.add(array);
        diskDatatable.draw();
        // diskDatatable.rows.add(newDataArray);
    };
    diskUpdate();
    dashboardCPUPlotIntervalId = setInterval(main, 2000);
    diskUpdateIntervalId = setInterval(diskUpdate, 10 * 1000);
    main();

    let jvmCpuEnabled = dashboard.currentCompInfo.java.cpuUsage.isSupported;
    let jvmGCEnabled = dashboard.currentCompInfo.java.gc.length > 0;

    // let jvmCpuEnabled = false;
    // let jvmGCEnabled = false;

    // let jvmCpuDataHolder = new DataHolder();
    let jvmCpuMaxHold = 60;
    let jvmGCMaxHold = 60;

    $("#JVMCPUButtons").find("li").click((event) => {
        // jvmCpuDataHolder.setMax(event.target.dataset.time);
        jvmCpuMaxHold = event.target.dataset.time;
        document.querySelector("#JVMCPUButtons > button").innerHTML = event.target.innerHTML;
    });

    $("#JVMGCButtons").find("li").click((event) => {
        jvmCpuMaxHold = event.target.dataset.time;
        document.querySelector("#JVMGCButtons > button").innerHTML = event.target.innerHTML;
    });

    let jvmCpuChart;
    // let jvmCpuChart = new Chart(document.querySelector("#JVMCPUChart").getContext("2d"), {
    //     type: 'line',
    //     data: {
    //         datasets: [{
    //             label: "CPU usage",
    //         //     data: [randomScalingFactor(), randomScalingFactor(), randomScalingFactor(), randomScalingFactor(), randomScalingFactor(), randomScalingFactor(), randomScalingFactor()],
    //         // }, {
    //         //     label: "My Second dataset",
    //         //     data: [randomScalingFactor(), randomScalingFactor(), randomScalingFactor(), randomScalingFactor(), randomScalingFactor(), randomScalingFactor(), randomScalingFactor()],
    //             backgroundColor: 'rgba(255, 99, 132, 0.2)',
    //             borderColor: 'rgba(255,99,132,1)',
    //             borderWidth: 1
    //         }]
    //     },
    //     options: {
    //         responsive: true,
    //         bezierCurve : false,
    //         elements: {
    //             point: {
    //                 radius: 0
    //             }
    //         },
    //             // title: {
    //         //     display: true,
    //         //     text: 'Chart.js Line Chart'
    //         // },
    //         tooltips: {
    //             mode: 'index',
    //             intersect: false
    //         },
    //         scales: {
    //             xAxes: [{
    //                 display: true,
    //                 scaleLabel: {
    //                     show: true,
    //                 }
    //             }],
    //             yAxes: [{
    //                 display: true,
    //                 scaleLabel: {
    //                     show: true,
    //                 },
    //                 ticks: {
    //                     beginAtZero: true
    //                 }
    //             }]
    //         }
    //     }
    // });

    function generateRandomColor() {
        return "rgba(" + Math.floor(Math.random() * 255) + ", " + Math.floor(Math.random() * 255) + ", " + Math.floor(Math.random() * 255) + ", 0.2)"
    }

    function generateBorderForColor(color) {
        return color.replace("0.2", "1");
    }

    // let lastJvmCpuTime = 0;
    let lastJvmUptime = 0;
    let labelIForCPU = 0;
    let lastJvmCpuData = [];

    let labelIForGC = 0;
    let lastGCCounts = [];
    let lastGCTime = [];
    let gcData = [];
    for (let i = 0; i < dashboard.currentCompInfo.java.gc.length; i++) {
        gcData.push({
            label: (dashboard.currentCompInfo.java.gc[i].name + " GC activity" + ((dashboard.currentCompInfo.java.gc[i].isValid) ? "" : "(invalid)")),
            backgroundColor: (i < 6) ? backgroundColors[i] : generateRandomColor(),
            borderColor: (i < 6) ? borderColors[i] : generateBorderForColor(this.backgroundColor),
            borderWidth: 1
        });
        lastGCCounts.push(dashboard.currentCompInfo.java.gc[i].gcCount);
        lastGCTime.push(dashboard.currentCompInfo.java.gc[i].gcTime);
    }
    let jvmGCChart;

    // let jvmGCChart = new Chart(document.querySelector("#JVMGCChart").getContext("2d"), {
    //     type: 'line',
    //     data: {
    //         datasets: gcData
    //     },
    //     options: {
    //         responsive: true,
    //         bezierCurve : false,
    //         elements: {
    //             point: {
    //                 radius: 0
    //             }
    //         },
    //         // title: {
    //         //     display: true,
    //         //     text: 'Chart.js Line Chart'
    //         // },
    //         tooltips: {
    //             mode: 'index',
    //             intersect: false
    //         },
    //         scales: {
    //             xAxes: [{
    //                 display: true,
    //                 scaleLabel: {
    //                     show: true,
    //                 }
    //             }],
    //             yAxes: [{
    //                 display: true,
    //                 scaleLabel: {
    //                     show: true,
    //                 },
    //                 ticks: {
    //                     beginAtZero: true
    //                 }
    //             }]
    //         }
    //     }
    // });

    for (let i = 0; i < dashboard.currentCompInfo.java.cpuUsage.count; i++) {
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
                    //     data: [randomScalingFactor(), randomScalingFactor(), randomScalingFactor(), randomScalingFactor(), randomScalingFactor(), randomScalingFactor(), randomScalingFactor()],
                    // }, {
                    //     label: "My Second dataset",
                    //     data: [randomScalingFactor(), randomScalingFactor(), randomScalingFactor(), randomScalingFactor(), randomScalingFactor(), randomScalingFactor(), randomScalingFactor()],
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
                // title: {
                //     display: true,
                //     text: 'Chart.js Line Chart'
                // },
                tooltips: {
                    mode: 'index',
                    intersect: false
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
                // title: {
                //     display: true,
                //     text: 'Chart.js Line Chart'
                // },
                tooltips: {
                    mode: 'index',
                    intersect: false
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

    let jvmHeapChart = new Chart(document.querySelector("#JVMNonHeapChart").getContext("2d"), {
        type: 'line',
        data: {
            datasets: gcData
        },
        options: { //TODO написать профилирование базы данных и реквестов
            responsive: true,
            bezierCurve: false,
            elements: {
                point: {
                    radius: 0
                }
            },
            // title: {
            //     display: true,
            //     text: 'Chart.js Line Chart'
            // },
            tooltips: {
                mode: 'index',
                intersect: false
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

    let jvmChartsUpdate = () => {
        let elapsedTime = (dashboard.currentCompInfo.java.uptime - lastJvmUptime) * 1000000;

        if (jvmCpuEnabled) {
            let currentElapsedCpuTime = 0;
            for (let i = 0; i < dashboard.currentCompInfo.java.cpuUsage.count; i++) {
                currentElapsedCpuTime += (((dashboard.currentCompInfo.java.cpuUsage.data[i] / (dashboard.currentCompInfo.processorCount)) - (lastJvmCpuData[i] / (dashboard.currentCompInfo.processorCount))));
            }
            // let elapsedCpuTime = currentCpuTime - lastJvmCpuTime;

            // jvmCpuDataHolder.pushData(currentElapsedCpuTime);

            // jvmCpuChart.data.datasets[0].data = jvmCpuDataHolder.items;

            // for(let i = 0; i < jvmCpuDataHolder.items.length; i++) {
            //
            // }
            let currentJvmCpuValue = elapsedTime > 0 ? Math.round(Math.min(1000 * currentElapsedCpuTime / elapsedTime, 1000) * 10) / 100 : 0;
            if (currentJvmCpuValue < 0) {
                currentJvmCpuValue = 0;
            }
            jvmCpuChart.data.datasets[0].data.push(currentJvmCpuValue);
            jvmCpuChart.data.labels.push((labelIForCPU % 5 == 0) ? moment().format("hh:mm:ss") : "");

            while (jvmCpuChart.data.datasets[0].data.length > jvmCpuMaxHold) {
                jvmCpuChart.data.datasets[0].data.shift();
                jvmCpuChart.data.labels.shift();
            }


            labelIForCPU++;
            if (labelIForCPU >= 10) {
                labelIForCPU = 0;
            }

            jvmCpuChart.update();

            lastJvmCpuData = dashboard.currentCompInfo.java.cpuUsage.data;
        }

        if (jvmGCEnabled) {

            for (let i = 0; i < dashboard.currentCompInfo.java.gc.length; i++) {
                let has = dashboard.currentCompInfo.java.gc[i].gcCount > lastGCCounts[i];
                if (has) {
                    let elapsedGCTime = (dashboard.currentCompInfo.java.gc[i].gcTime / dashboard.currentCompInfo.processorCount * 1000000) - (lastGCTime[i] / dashboard.currentCompInfo.processorCount * 1000000);
                    let currentJvmGCValue = Math.round(Math.min(1000 * elapsedGCTime / elapsedTime, 1000) * 10) / 100;
                    if (currentJvmGCValue < 0) {
                        currentJvmGCValue = 0;
                    }
                    jvmGCChart.data.datasets[i].data.push(currentJvmGCValue);
                } else {
                    jvmGCChart.data.datasets[i].data.push(0);
                }
                lastGCCounts[i] = dashboard.currentCompInfo.java.gc[i].gcCount;
                lastGCTime[i] = dashboard.currentCompInfo.java.gc[i].gcTime;
            }

            jvmGCChart.data.labels.push((labelIForGC % 5 == 0) ? moment().format("hh:mm:ss") : "");

            while (jvmGCChart.data.datasets[0].data.length > jvmCpuMaxHold) {
                for (let i = 0; i < dashboard.currentCompInfo.java.gc.length; i++) {
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
        //noinspection JSUnresolvedVariable
        lastJvmUptime = dashboard.currentCompInfo.java.uptime;
    };

    jvmChartsUpdateIntervalId = setInterval(jvmChartsUpdate, 1000);
    jvmChartsUpdate();

    events.removeEventListener("loadingContentEnded", this);
});
events.addEventListener("finalizeContent", () => {
    clearInterval(dashboardCPUPlotIntervalId);
    clearInterval(diskUpdateIntervalId);
    clearInterval(jvmChartsUpdateIntervalId);
    // clearInterval(cpuUpdateIntervalId);
    events.removeEventListener("finalizeContent", this);
});
