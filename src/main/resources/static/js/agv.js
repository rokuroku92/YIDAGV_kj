var lastTaskJson = null;
var lastNotificationJson = null;
// var stations = {1: 'Station1', 2: 'Station2', 3: 'Station3', 4: 'Station4'};
// taskprogress <div class=\"progress\"><div id=\"taskProgress"+tasks[i].taskNumber+"\" class=\"progress-bar\" /*style=\"width: 30%;\"*/></div></div>
window.onresize =  function(){
    updateGridPositions();
    analysisMargin();
}

document.addEventListener("DOMContentLoaded", async function() {
    // setWindow();
    // setStationPositions();
    // updateAGVPositions();
    try {
        await setAGVList();
    } catch (error) {
        console.error('發生錯誤：', error);
    }
    // console.log("success");
    setTimeout(function() {
        updateGridPositions();
    }, 300);
    analysisMargin();

    // 更新資料
    updateAGVStatus();  //  取得狀態資料
    updateTask();
    updateNotification();
    updateGridStatus();
    getAnalysis();
    setInterval(updateAGVStatus, 5000);
    setInterval(updateTask, 1000);  //  每秒更新
    setInterval(updateNotification, 1000);
    setInterval(updateGridStatus, 1000);
    setInterval(getAnalysis, 60000);
});


function setWindow(){
    var AGVTask = document.querySelectorAll('.AGVTask');
    if (window.innerWidth < 1197) {
        for(let i=0;i<AGVTask.length;i++)
            AGVTask[i].style.display = "none";
    } else {
        for(let i=0;i<AGVTask.length;i++)
            AGVTask[i].style.display = "block";
    }
}

function updateAGVStatus() {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', baseUrl + "/api/homepage/agv", true);
    xhr.send();
    xhr.onload = function(){
        if(xhr.status == 200){
            var agvStatus = JSON.parse(this.responseText);
            agvUpdate(agvStatus);
        }
    };
}

function agvUpdate(agv){  // 更新資料
    for(let i=0;i<agv.length;i++){ // agv遍歷
        // 更新電量百分比
        var batteryString = "AGVBattery"+(i+1);
        document.getElementById(batteryString).innerHTML = agv[i].battery+"%";  
        // 更新電量
        batteryString = "AGVBatterySvg"+(i+1);
        var agvBattery = document.getElementById(batteryString);
        agv[i].battery > 90 ? agvBattery.setAttribute("xlink:href", "#battery-6") :
            agv[i].battery > 80 ? agvBattery.setAttribute("xlink:href", "#battery-5") : 
            agv[i].battery > 60 ? agvBattery.setAttribute("xlink:href", "#battery-4") :
            agv[i].battery > 40 ? agvBattery.setAttribute("xlink:href", "#battery-3") :
            agv[i].battery > 20 ? agvBattery.setAttribute("xlink:href", "#battery-2") :
            agv[i].battery > 0 ? agvBattery.setAttribute("xlink:href", "#battery-1") :
            agvBattery.setAttribute("xlink:href", "#battery-0");

        // 更新信號強度
        var signalString = "AGVSignalSvg"+(i+1);
        var agvSignal = document.getElementById(signalString);
        agv[i].signal > 90 ? agvSignal.setAttribute("xlink:href", "#wifi-4") : 
            agv[i].signal > 75 ? agvSignal.setAttribute("xlink:href", "#wifi-3") :
            agv[i].signal > 50 ? agvSignal.setAttribute("xlink:href", "#wifi-2") :
            agv[i].signal > 25 ? agvSignal.setAttribute("xlink:href", "#wifi-1") :
            agvSignal.setAttribute("xlink:href", "#wifi-0");
        
        // 更新AGV狀態
        // agvStatus[i].task(任務號碼)未使用
        document.getElementById('AGVStatusDiv'+(i+1)).classList.remove('error', 'warning', 'normal');
        document.getElementById('AGVStatus'+(i+1)).classList.remove('error', 'warning', 'normal');
        /*
        OFFLINE(1), ONLINE(2), MANUAL(3), REBOOT(4),
        STOP(5), DERAIL(6), COLLIDE(7), OBSTACLE(8),
        EXCESSIVE_TURN_ANGLE(9), WRONG_TAG_NUMBER(10), UNKNOWN_TAG_NUMBER(11),
        EXCEPTION_EXCLUSION(12), SENSOR_ERROR(13), CHARGE_ERROR(14), ERROR_AGV_DATA(15);
        */
        var statusHTMLClass = "normal"; 
        var statusText = agv[i].status; 
        switch (agv[i].status) {
            case "OFFLINE":
                statusHTMLClass = "error";
                break;
            case "ONLINE":
                if(agv[i].taskStatus == "NO_TASK"){
                    if(!agv[i].task){
                        statusHTMLClass = "warning";
                        statusText="IDLE";
                    } else {
                        // 回待命點的任務
                        statusText="GO_STANDBY";
                    }
                } else {
                    statusText="WORKING";
                }
                break;
            case "MANUAL":
                statusHTMLClass = "warning"; 
                break;
            case "REBOOT":
                statusHTMLClass = "error";
                break;
            case "STOP":
                statusHTMLClass = "error";
                break;
            case "DERAIL":
                statusHTMLClass = "error";
                break;
            case "COLLIDE":
                statusHTMLClass = "error"; 
                break;
            case "OBSTACLE":
                statusHTMLClass = "warning"; 
                break;
            case "EXCESSIVE_TURN_ANGLE":
                statusHTMLClass = "error";
                break;
            case "WRONG_TAG_NUMBER":
                statusHTMLClass = "error";
                break;
            case "UNKNOWN_TAG_NUMBER":
                statusHTMLClass = "error";
                break;
            case "EXCEPTION_EXCLUSION":
                statusHTMLClass = "error";
                break;
            case "SENSOR_ERROR":
                statusHTMLClass = "error";
                break;
            case "CHARGE_ERROR":
                statusHTMLClass = "error";
                break;
            case "ERROR_AGV_DATA":
                statusHTMLClass = "error";
                break;
            default:
                statusHTMLClass = "error";
                break;
        }
        document.getElementById('AGVStatusDiv'+(i+1)).classList.add(statusHTMLClass);
        document.getElementById('AGVStatus'+(i+1)).classList.add(statusHTMLClass);
        document.getElementById('AGVStatus'+(i+1)).innerHTML = statusText;

        if(agv[i].task){
            document.getElementById('AGVTaskNumber'+(i+1)).innerHTML = agv[i].task.taskNumber;
            let taskContent = "START: " + agv[i].task.startStation + " | TERMINAL: " + agv[i].task.terminalStation;
            document.getElementById('AGVTaskST'+(i+1)).innerHTML = taskContent;
        } else {
            document.getElementById('AGVTaskNumber'+(i+1)).innerHTML = "";
            document.getElementById('AGVTaskST'+(i+1)).innerHTML = "<h5>NO TASK</h5>";
        }
        // 更新AGV位置
        // updateAGVPositions(agvStatus[i].place);

        // updateTN();
    }
}

function updateTask() {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', baseUrl + "/api/task/now", true);
    xhr.send();
    xhr.onload = function(){
        if(xhr.status == 200){
            var tasks = JSON.parse(this.responseText);
            taskUpdate(tasks);
        }
    };
}

function taskUpdate(tasks){
    if(JSON.stringify(lastTaskJson) !== JSON.stringify(tasks)){
        lastTaskJson = tasks;
        var taskHTML="";
        for(let i=0;i<tasks.length;i++){
            var datetime = tasks[i].createTaskTime;
            var year = datetime.substring(0, 4);
            var month = datetime.substring(4, 6);
            var day = datetime.substring(6, 8);
            var hour = datetime.substring(8, 10);
            var minute = datetime.substring(10, 12);
            var second = datetime.substring(12, 14);
            taskHTML+= `<div class="row task">
                            <div class="col agvTask">
                            <div class="row agvTask-row">
                                <div class="col-8">
                                <div class="row taskTitle">
                                    <div class="col">
                                    <p>` + tasks[i].taskNumber + `</p>
                                    </div>
                                </div>
                                <div class="row taskContent">
                                    <div class="col">
                                    <p>AGV: ` + tasks[i].agv + ` | START: ` + tasks[i].start + ` | TERMINAL: ` + tasks[i].terminal + `</p>
                                    </div>
                                </div>
                                </div>
                                <div class="col-2">
                                <div class="row taskTB">
                                    <div class="col-7 taskBar"></div>
                                    <div class="col-5 datetime">
                                    <div class="row">
                                        <div class="col">
                                        <labe class="right">` + year + `/` + month + `/` + day + `</label>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col">
                                        <label class="right">` + hour + `:` + minute + `:` + second + `</label>
                                        </div>
                                    </div>
                                    </div>
                                </div>
                                </div>
                                <div class="col-2 center">
                                <button type="button" class="btn btn-danger d-flex removetask" onclick="cancelTask('` + tasks[i].taskNumber + `')">
                                    <svg width="20" height="20" role="img"><use xlink:href="#trash"/></svg>
                                </button>
                                </div>
                            </div>
                            </div>
                        </div>`;
        }
        document.getElementById("taskQueue").innerHTML = taskHTML;
    }
    
}

function updateNotification() {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', baseUrl + "/api/homepage/notifications", true);
    xhr.send();
    xhr.onload = function(){
        if(xhr.status == 200){
            var notifications = JSON.parse(this.responseText);
            notificationUpdate(notifications);
        }
    };
}

function notificationUpdate(notifications){
    if(JSON.stringify(lastNotificationJson) !== JSON.stringify(notifications)){
        lastNotificationJson = notifications;
        let notificationHTML="";
        for(let i=0;i<notifications.length;i++){
            var datetime = notifications[i].createTime;
            var year = datetime.substring(0, 4);
            var month = datetime.substring(4, 6);
            var day = datetime.substring(6, 8);
            var hour = datetime.substring(8, 10);
            var minute = datetime.substring(10, 12);
            var second = datetime.substring(12, 14);

            var status;
            switch (notifications[i].level) {
                case 0:
                    status="normal";
                    break;
                case 1:
                    status="info";
                    break;
                case 2:
                    status="warning";
                    break;
                case 3:
                    status="danger";
                    break;
            
                default:
                    break;
            }
            notificationHTML += `<div class="row">
                                    <div class="col message">
                                    <div class="nfStatus ${status}"></div>
                                    <div class="messageContent">
                                        <label>${notifications[i].name}</label>
                                        <p>${notifications[i].message}</p>
                                        <p style="float: right;margin: 0px;">${year}/${month}/${day}&nbsp;${hour}:${minute}:${second}</p>
                                    </div>
                                    </div>
                                </div>`;
        }
        document.getElementById("notification").innerHTML = notificationHTML;
    }
}

function updateGridStatus() {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', baseUrl + "/api/grid/status", true);
    xhr.send();
    xhr.onload = function(){
        if(xhr.status == 200){
            var gridStatus = JSON.parse(this.responseText);
            gridUpdate(gridStatus);
        }
    };
}

function gridUpdate(data){
    // console.log(data);
    allGrids = document.querySelectorAll("[data-val]");
    allGrids.forEach(function(grid) {
        grid.classList.remove("booked");
        grid.classList.remove("occupied");
        gridName = grid.getAttribute("data-val");
        data.forEach(function(gdata) {
            if(gdata.station === gridName){
                if(gdata.status === 1){
                    grid.classList.add("booked");
                } else if(gdata.status === 2){
                    grid.classList.add("occupied");
                }
            }
        });
    });
    lastGridData = data;
}

function getAnalysis(){
    var xhr = new XMLHttpRequest();
    xhr.open('GET', baseUrl + "/api/analysis/mode?agvId=1&value=recently", true);
    xhr.send();
    xhr.onload = function(){
        if(xhr.status == 200){
            var data = JSON.parse(this.responseText);
            // console.log(data);
            countRate(data);
        }
    };
}

function countRate(data) {
    var task_sum = 0;
    var work_sum = 0;
    var open_sum = 0;
    var x=0;
    for(let i=0 ; i < data.length ; i++) {
        task_sum += data[i].task;
        work_sum += data[i].workingMinute;
        open_sum += data[i].openMinute;
        if(data[i].task>0)x++;
    }
    document.getElementById("work_sum").value = String(Math.floor(work_sum/60))+"hr";
    document.getElementById("open_sum").value = String(Math.floor(open_sum/60))+"hr";
    document.getElementById("rate").value = String(((work_sum/open_sum)*100).toFixed(1))+"%";
    document.getElementById("task_sum").value = task_sum;
}

function setAGVList() {
    return new Promise((resolve, reject) => {
        var xhr = new XMLHttpRequest();
        xhr.open('GET', baseUrl + "/api/homepage/agvlist", true);
        xhr.send();
        xhr.onload = function(){
            if(xhr.status == 200){
                agvList = JSON.parse(this.responseText);
                addAGVList(agvList);
                resolve(); // 解析成功时，将 Promise 设置为已完成状态
            }else {
                reject(new Error('AGV列表獲取失敗')); // 解析失败时，将 Promise 设置为拒绝状态
            }
        };
    });
}

function addAGVList(agvList){  // 更新資料
    if(agvList.length===1){
        document.getElementById("AGVName1").innerText = agvList[0].name;
        document.getElementById("AGVMemo1").innerText = agvList[0].memo;
        document.getElementById("AGVImg1").setAttribute("src", "image/"+agvList[0].img);
    }
}

function setStationPositions() {
    var map = document.getElementById("map");
    var mapWidth = map.clientWidth;
    var stationHeight = map.clientHeight/3.9;

    for(let i=0;i<4;i++){
        stationString = "station"+(i+1);
        document.getElementById(stationString).style.transform = "translate(" +
            (-mapWidth*(0.973-i*0.31))+"px, "+stationHeight+"px)"; // i*0.31 (0.93/(站點-1)) (0.93/(4-1))=0.31|(0.93/(5-1))
    }
}

function updateAGVPositions(station) {
    var map = document.getElementById("map");
    var mapWidth = map.clientWidth;
    var stationHeight = map.clientHeight/2.53;
    var place = parseInt(station.slice(-1)) - 1;
    /*** 
     * -mapWidth*(0.9765)   第一站
     * -mapWidth*(0.05)     第四站
     * (0.9765-0.05)/3 = 0.309 == 四站間距
     *  ***/

    // document.getElementById("agv1").style.transform = "translate(" + (-mapWidth*(0.9765))+"px, "+stationHeight+"px) rotate(90deg)";
    document.getElementById("agv1").style.transform = "translate(" + (-mapWidth*(0.05+place*0.309))+"px, "+stationHeight+"px) rotate(90deg)";
}

function cancelTask(taskNumber){
    let cf = confirm("是否取消任務號碼： " + taskNumber);
    if(cf){
        var xhr = new XMLHttpRequest();
        xhr.open('GET', baseUrl + "/api/cancelTask?taskNumber=" + taskNumber.slice(1), true);
        xhr.send();
        xhr.onload = function(){
            if(xhr.status == 200){
                if(this.responseText == 'OK')
                    alert("成功取消任務: " + taskNumber);
                else
                    alert("取消任務失敗，可能是格式錯誤");
            }else
                alert("取消任務失敗");
            window.location.reload();
        };
    }

}


function updateGridPositions(){
    let map = document.getElementById("map");
    let mapWidth = map.clientWidth;
    let mapHeight = map.clientHeight;
    Object.entries(mapGridPositions).forEach(([gridName, positions]) => {
        let mapGrid = document.querySelector("[data-val=" + gridName + "]");
        let gridWidth = mapGrid.clientWidth;
        let gridHeight = mapGrid.clientHeight;
        let translate = "translate(" + ((positions[0]/100) * mapWidth + (gridWidth/2)) +"px, " + ((positions[1]/100) * mapHeight + (gridHeight/2)) + "px)";
        
        let originalTransform = window.getComputedStyle(mapGrid).getPropertyValue('transform');
        if(originalTransform !== "none"){
            // 因為瀏覽器返回的transform: rotate(90deg)會變成transform: matrix(0, 1, -1, 0, 0, 0)，要改回來需要這樣
            let matrix = new DOMMatrix(originalTransform);
            let angle = Math.atan2(matrix.b, matrix.a) * (180 / Math.PI);
            mapGrid.style.transform = translate + " rotate(" + angle +"deg)";
        } else {
            mapGrid.style.transform = translate;
        }
    });
}

function analysisMargin(){
    if(document.body.clientWidth == 1600){
        document.getElementById("miniporter").style.display = "flex";
    } else {
        document.getElementById("miniporter").style.display = "none";
    }
}