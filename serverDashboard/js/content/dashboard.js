'use strict';
var dashboardCPUPlotIntervalId;
var diskUpdateIntervalId;
// var cpuUpdateIntervalId;
events.addEventListener("loadingContentEnded", () => {
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
            array.push([
                partition.name,
                partition.address,
                partition.type,
                Math.floor(partition.maxSize / (1024 * 1024 * 1024) * 100) / 100,
                Math.floor(partition.usedSize / (1024 * 1024 * 1024) * 100) / 100,
                Math.floor(partition.freeSize / (1024 * 1024 * 1024) * 100) / 100,
                partition.inodes,
                partition.inodesFree
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
    events.removeEventListener("loadingContentEnded", this);
});
events.addEventListener("finalizeContent", () => {
    clearInterval(dashboardCPUPlotIntervalId);
    clearInterval(diskUpdateIntervalId);
    // clearInterval(cpuUpdateIntervalId);
    events.removeEventListener("finalizeContent", this);
});

