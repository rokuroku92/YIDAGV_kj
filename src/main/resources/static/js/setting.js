document.addEventListener("DOMContentLoaded", function() {
    getConfig();
    document.getElementById("save").addEventListener("click", function (){
        setConfig();
    })
});

function getConfig(){
    var xhr = new XMLHttpRequest();
    xhr.open('GET', baseUrl + "/api/getConfig", true);
    xhr.send();
    xhr.onload = function(){
        if(xhr.status == 200){
            var configJSON = JSON.parse(this.responseText);
            document.getElementById("agvControlUrl").value = configJSON.agvControl.url;
            document.getElementById("agvLowBattery").value = configJSON.agv.low_battery;
            document.getElementById("agvLowBatteryDuration").value = configJSON.agv.low_battery_duration;
            document.getElementById("agvObstacleDuration").value = configJSON.agv.obstacle_duration;
            document.getElementById("agvTaskExceptionOption").value = configJSON.agv.task_exception_option;
            document.getElementById("httpTimeout").value = configJSON.http.timeout;
            document.getElementById("httpMaxRetry").value = configJSON.http.max_retry;
        }
    };
}

function setConfig(){
    let agvControlUrl = document.getElementById("agvControlUrl").value;
    let agvLowBattery = document.getElementById("agvLowBattery").value;
    let agvLowBatteryDuration = document.getElementById("agvLowBatteryDuration").value;
    let agvObstacleDuration = document.getElementById("agvObstacleDuration").value;
    let agvTaskExceptionOption = document.getElementById("agvTaskExceptionOption").value;
    let httpTimeout = document.getElementById("httpTimeout").value;
    let httpMaxRetry = document.getElementById("httpMaxRetry").value;

    if(agvControlUrl === ""){
        alert("AgvControlUrl參數為空值");
        return;
    } else if(agvLowBattery === ""){
        alert("AgvLowBattery參數為空值");
        return;
    } else if(agvLowBatteryDuration === ""){
        alert("AgvLowBatteryDuration參數為空值");
        return;
    } else if(agvObstacleDuration === ""){
        alert("AgvObstacleDuration參數為空值");
        return;
    } else if(agvTaskExceptionOption === ""){
        alert("AgvTaskExceptionOption參數為空值");
        return;
    } else if(httpTimeout === ""){
        alert("HttpTimeout參數為空值");
        return;
    } else if(httpMaxRetry === ""){
        alert("HttpMaxRetry參數為空值");
        return;
    }

    if(agvTaskExceptionOption != 0 && agvTaskExceptionOption != 1){
        alert("agvTaskExceptionOption參數輸入錯誤，應為0|1");
        return;
    }

    let config = {
        agvControlUrl: agvControlUrl,
        agvLowBattery: agvLowBattery,
        agvLowBatteryDuration: agvLowBatteryDuration,
        agvObstacleDuration: agvObstacleDuration,
        agvTaskExceptionOption: agvTaskExceptionOption,
        httpTimeout: httpTimeout,
        httpMaxRetry: httpMaxRetry
    };
    fetch(baseUrl+'/api/setConfig', {
        method: 'POST',  // 可以根据需要使用不同的 HTTP 方法
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(config)
    }).then(response => {
        return response.text();
    }).then(response => {
        // 处理后端的响应
        alert(response);
        if(response == "OK"){
            alert("請重新啟動Server方可生效。");
        }
    }).catch(error => {
        // 处理错误
        alert("修改失敗: ", error);
    });
}