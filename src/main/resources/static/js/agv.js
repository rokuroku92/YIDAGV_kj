var lastTaskJson = null;
var lastNotificationJson = null;
// var stations = {1: 'Station1', 2: 'Station2', 3: 'Station3', 4: 'Station4'};
// taskprogress <div class=\"progress\"><div id=\"taskProgress"+tasks[i].taskNumber+"\" class=\"progress-bar\" /*style=\"width: 30%;\"*/></div></div>
window.onresize = function(){
    updateGridPositions();
    analysisMargin();
}

document.addEventListener("DOMContentLoaded", async function() {
    // setWindow();
    // setStationPositions();
    // updateAGVPositions();
    // setInterval(demoPlace, 100);
    // updateGridPositions();
    // updateAGVPosition("1003");
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
    setInterval(updateAGVStatus, 1000);
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
        if(agv[i].place){
            updateAGVPosition(agv[i].place);
        }
        
    }
}

var tag = 1001;
var tagF = true;
function demoPlace(){
    if(tagF){
        if(tag<1166){
            tag = tag + 1;
        } else {
            tagF = false;
        }
    } else {
        if(tag>1001){
            tag = tag - 1;
        } else {
            tagF = true;
        }
    }
    
    updateAGVPosition(String(tag));
}

const mapTagPositions = {
    "1001": [25, 18],
    "1002": [28, 18],
    "1003": [25, 31.5],
    "1004": [28, 31.5],
    "1005": [25, 45.5],
    "1006": [28, 45],
    "1007": [25, 59],
    "1008": [28, 59],
    "1009": [28, 72],
    "1010": [24.5, 72],
    "1011": [21, 72],
    "1012": [18.8, 75],
    "1013": [18.8, 77],

    "1014": [17.2, 77],
    "1015": [17.2, 82],
    "1016": [15.6, 77],
    "1017": [15.6, 82],
    "1018": [14, 77],
    "1019": [14, 82],
    "1020": [12.6, 77],
    "1021": [12.6, 82],
    "1022": [11, 77],
    "1023": [11, 82],
    "1024": [9.4, 77],
    "1025": [9.4, 82],
    "1026": [7.9, 77],
    "1027": [7.9, 82],

    "1028": [18.8, 88],
    "1029": [18, 92],

    "1030": [17.2, 92],
    "1031": [17.2, 87],
    "1032": [15.6, 92],
    "1033": [15.6, 87],
    "1034": [14, 92],
    "1150": [14, 87],
    "1151": [12.6, 92],
    "1152": [12.6, 87],
    "1153": [11, 92],
    "1154": [11, 87],
    "1155": [9.4, 92],
    "1156": [9.4, 87],
    "1157": [7.9, 92],
    "1158": [7.9, 87],

    "1035": [20, 77],
    "1036": [24, 77],
    "1037": [27, 81],
    "1038": [32, 81],
    "1039": [29.5, 77],
    "1040": [27.5, 77],
    "1041": [34, 77],
    "1042": [36.5, 77],
    "1043": [37, 81],
    "1044": [45, 77],
    "1045": [40, 77],
    "1046": [47, 77],
    "1047": [51, 75],
    "1048": [51.5, 74],
    "1049": [36, 72],
    "1050": [44, 72],
    "1051": [54, 72],
    // "1052": [50, 70],
    // "1053": [50, 81],
    "1054": [56, 72],
    "1055": [60, 75],
    "1056": [62, 77],
    "1057": [65, 78],
    "1058": [65.5, 78],
    "1059": [67.3, 78],
    "1060": [67.3, 82],
    "1061": [69, 78],
    "1062": [69, 82],
    "1063": [70.7, 78],
    "1064": [70.7, 82],
    "1065": [72.4, 78],
    "1066": [72.4, 82],
    "1067": [74.1, 78],
    "1068": [74.1, 82],
    "1069": [75.8, 78],
    "1070": [75.8, 82],
    "1071": [77.5, 78],
    "1072": [77.5, 82],
    "1073": [79.2, 78],
    "1074": [79.2, 82],
    "1075": [81.2, 78],
    "1076": [81.2, 89],
    "1077": [80, 93],
    "1078": [79.2, 93],
    "1079": [79.2, 89],
    "1080": [77.5, 93],
    "1081": [77.5, 89],
    "1082": [75.8, 93],
    "1083": [75.8, 89],
    "1084": [74.1, 93],
    "1085": [74.1, 89],
    "1086": [72.4, 93],
    "1087": [72.4, 89],
    "1088": [70.7, 93],
    "1089": [70.7, 89],
    "1090": [69, 93],
    "1091": [69, 89],
    "1092": [67.3, 93],
    "1093": [67.3, 89],
    "1094": [66.5, 93],
    "1095": [65.5, 89],
    "1096": [65.5, 73],
    "1097": [79, 73.5],
    "1098": [78.5, 67],
    "1099": [78.5, 61],
    "1100": [78.5, 49],
    "1101": [76.5, 50],
    "1102": [75.7, 40],
    "1103": [75.7, 30],
    "1104": [73.6, 36],
    "1105": [73.6, 31],
    "1106": [73.6, 27],
    "1107": [75.7, 27],
    "1108": [66, 22.3],
    "1109": [78.6, 27],
    "1110": [81.5, 27],
    "1111": [83, 27],
    "1112": [90, 27],
    "1113": [92.5, 30],
    "1114": [92.5, 42.8],
    "1115": [89.6, 42.8],
    "1116": [92.5, 46],
    "1117": [89.6, 46],
    "1118": [92.5, 49],
    "1119": [89.6, 49],
    "1120": [92.5, 52],
    "1121": [89.6, 52],
    "1122": [92.5, 55],
    "1123": [89.6, 55],
    "1124": [92.5, 58],
    "1125": [89.6, 58],
    "1126": [92.5, 61.2],
    "1127": [89.6, 61.2],
    "1128": [92.5, 64.4],
    "1129": [89.6, 64.4],
    "1130": [92.5, 65.5],
    "1131": [90, 68],
    "1132": [83, 68],
    "1133": [83, 64.4],
    "1134": [86, 64.4],
    "1135": [83, 61.2],
    "1136": [86, 61.2],
    "1137": [83, 58],
    "1138": [86, 58],
    "1139": [83, 55],
    "1140": [86, 55],
    "1141": [83, 52],
    "1142": [86, 52],
    "1143": [83, 49],
    "1144": [86, 49],
    "1145": [83, 46],
    "1146": [86, 46],
    "1147": [83, 42.8],
    "1148": [86, 42.8],
    "1149": [83, 32],
    
    "1159": [48.5, 80.5],
    "1160": [45, 84],
    "1161": [43, 84],
    "1162": [40.8, 84],
    "1163": [40.8, 89],
    "1164": [39.3, 84],
    "1165": [39.3, 89],
    
}

function updateAGVPosition(tag) {
    tag = String(((Number(tag)-1000) % 250) + 1000);
    var map = document.getElementById("map");
    var mapWidth = map.clientWidth;
    var mapHeight = map.clientHeight;
    let mapGrid = document.getElementById("agv_car");
    let agvWidth = mapGrid.clientWidth;
    let agvHeight = mapGrid.clientHeight;

    var place = mapTagPositions[tag];
    let translate = "translate(" + ((place[0]/100) * mapWidth - (agvWidth/2)) +"px, " + ((place[1]/100) * mapHeight - (agvHeight/2)) + "px)";

    document.getElementById("agv_car").style.transform = translate;
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