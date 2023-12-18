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
    let httpTimeout = document.getElementById("httpTimeout").value;
    let httpMaxRetry = document.getElementById("httpMaxRetry").value;
    if(agvControlUrl === ""){
        return "AgvControlUrl參數為空值";
    } else if(agvLowBattery === ""){
        return "AgvLowBattery參數為空值";
    } else if(agvLowBatteryDuration === ""){
        return "AgvLowBatteryDuration參數為空值";
    } else if(agvObstacleDuration === ""){
        return "AgvObstacleDuration參數為空值";
    } else if(httpTimeout === ""){
        return "HttpTimeout參數為空值";
    } else if(httpMaxRetry === ""){
        return "HttpMaxRetry參數為空值";
    }
    let config = {
        agvControlUrl: agvControlUrl,
        agvLowBattery: agvLowBattery,
        agvLowBatteryDuration: agvLowBatteryDuration,
        agvObstacleDuration: agvObstacleDuration,
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
    }).catch(error => {
        // 处理错误
        alert("修改失敗: ", error);
    });
}