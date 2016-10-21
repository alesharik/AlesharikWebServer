'use strict';
var dashboardCPUPlotIntervalId;
var diskUpdateIntervalId;
events.addEventListener("loadingContentEnded", () => {
    function generateData(cpu, last) {
        if (last == undefined) {
            last = [];
            for (let j = 0; j < dashboard.currentCompInfo.cpuCount; j++) {
                last[j] = generateData(j, [0, 0, 0, 0, 0, 0, 0, 0]);
            }
        }

        let load = dashboard.currentCompInfo.cpuLoad;

        function warpSubtract(a, b) {
            return (a > b) ? a - b : 0
        }

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

        return neww;
    }

    let last = [];
    for (let j = 0; j < dashboard.currentCompInfo.cpuCount; j++) {
        last[j] = generateData(j, [0, 0, 0, 0, 0, 0, 0, 0]);
    }

    let diskDatatable = $('#diskDatatable').dataTable().api();

    let main = () => {
        let cpuData = [];
        for (let j = 0; j < dashboard.currentCompInfo.cpuCount; j++) {
            cpuData[j] = generateData(j, last[j]);
        }

        let data = [];

        for (let i = 0; i < 7; i++) {
            if (i == 3) {
                continue;
            }
            let dat = [];
            for (let j = 1; j <= dashboard.currentCompInfo.cpuCount; j++) {
                let cpuLoad = cpuData[j - 1];
                let total = cpuLoad[7] == 0 ? 1 : cpuLoad[7];
                dat.push([j, cpuLoad[i] / total * 100]);
            }
            data.push(dat);
        }
        $.plot("#CPUPlot", data, {
            series: {
                stack: true,
                bars: {
                    show: true,
                    barWidth: 0.6
                }
            }
        });
        last = cpuData;

        let memoryData = [];
        let ram = dashboard.currentCompInfo.ram;
        memoryData.push({
            label: "Free " + (Math.round(ram[1] / (1024 * 1024 * 1024) * 100) / 100) + "G",
            data: Math.round(ram[1] / (1024 * 1024))
        });
        memoryData.push({
            label: "Shared " + (Math.round(ram[2] / (1024 * 1024 * 1024) * 100) / 100) + "G",
            data: Math.round(ram[2] / (1024 * 1024))
        });
        memoryData.push({
            label: "Buffer " + (Math.round(ram[3] / (1024 * 1024 * 1024) * 100) / 100) + "G",
            data: Math.round(ram[3] / (1024 * 1024))
        });
        memoryData.push({
            label: "Used " + (Math.round((ram[0] - (ram[1] + ram[2] + ram[3])) / (1024 * 1024 * 1024) * 100) / 100) + "G",
            data: Math.round((ram[0] - (ram[1] + ram[2] + ram[3])) / (1024 * 1024))
        });
        $.plot("#MemoryPlot", memoryData, {
            series: {
                pie: {
                    show: true,
                    radius: 1,
                    innerRadius: 0.5,
                }
            },
            legend: {
                show: true
            }
        });

        let maxSpace = Math.round(ram[4] / (1024 * 1024 * 1024) * 100) / 100;
        let usedPercent = ((ram[4] - ram[5]) / ram[4] * 100);
        let freePercent = (ram[5] / ram[4] * 100);

        //noinspection JSJQueryEfficiency
        $("#swapProgressBarUsed").css("width", usedPercent + "%");
        if (usedPercent > 10) {
            $("#swapProgressBarUsed").text("Used space: " + Math.round((ram[4] - ram[5]) / (1024 * 1024 * 1024) * 100) / 100 + "G/" + maxSpace + "G");
        }
        //noinspection JSJQueryEfficiency
        $("#swapProgressBarFree").css("width", freePercent + "%");
        if (freePercent) {
            $("#swapProgressBarFree").text("Free space: " + Math.round(ram[5] / (1024 * 1024 * 1024) * 100) / 100 + "G/" + maxSpace + "G");
        }
    };

    let diskUpdate = () => {
        diskDatatable.clear();
        diskDatatable.rows.add(newDataArray);
        diskDatatable.draw();
    };
    dashboardCPUPlotIntervalId = setInterval(main, 1000);
    diskUpdateIntervalId = setInterval(diskUpdate, 10 * 1000);
    main();
    events.removeEventListener("loadingContentEnded", this);
});
events.addEventListener("finalizeContent", () => {
    clearInterval(dashboardCPUPlotIntervalId);
    clearInterval(diskUpdateIntervalId);
    events.removeEventListener("finalizeContent", this);
});

