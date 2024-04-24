var lastGridData = null;

window.onresize =  function(){
    updateGridPositions();
}

document.addEventListener("DOMContentLoaded", function() {
    bindGridButton();
    updateGridStatus();  //  取得狀態資料
    zoomMap();
    setTimeout(function() {
        updateGridPositions();
    }, 300);
    setInterval(updateGridStatus, 1000);  //  每秒更新
});

// function zoomMap() {
//     var myElement = document.querySelector('.map');
//     var hammer = new Hammer(myElement);

//     // 缩放的范围
//     var minScale = 1;  // 設置更小的最小縮放比例
//     var maxScale = 4;  // 設置更小的最大縮放比例

//     // 縮放速度
//     var zoomInSpeed = 0.2;  // 放大的速度
//     var zoomOutSpeed = 0.4; // 縮小的速度
    
//     // 移動速度
//     var baseMoveSpeed = 0.2;  // 基本移動速度
//     var moveSpeedScale = 0.1;   // 移動速度的縮放比例

//     // 移動範圍限制
//     var maxX = (myElement.offsetWidth * (maxScale - 1));  // 最大 X 坐標
//     var maxY = (myElement.offsetHeight * (maxScale - 1));  // 最大 Y 坐標
//     var minX = -(myElement.offsetWidth * (maxScale - 1));  // 最小 X 坐標
//     var minY = -(myElement.offsetHeight * (maxScale - 1)); // 最小 Y 坐標

//     // 当前缩放比例
//     var currentScale = 1;

//     // 當前位置
//     var currentPosition = { x: 0, y: 0 };

//     // 過渡時間
//     var transitionTime = 0.1;  // 過渡時間設為 0.1 秒

//     var throttleTimeout;

//     hammer.get('pinch').set({ enable: true });
//     hammer.get('pan').set({ enable: true });
//     hammer.get('doubletap').set({ enable: true });  // 啟用 double tap 事件

//     hammer.on('pinch pan doubletap', function(event) {
//         if (!throttleTimeout) {
//             throttleTimeout = setTimeout(function () {
//                 // 根據手勢方向調整縮放速度
//                 var zoomSpeed = event.scale > 1 ? zoomInSpeed : zoomOutSpeed;
                

//                 if (event.type === 'doubletap') {
//                     currentScale = 1;
//                     currentPosition = { x: 0, y: 0 };
//                 } else {
//                     // 计算新的缩放比例，使用縮放速度
//                     currentScale = Math.min(Math.max(minScale, currentScale + (event.scale - 1) * zoomSpeed), maxScale);

//                     // 調整移動速度，使得在放大時移動更慢
//                     var moveSpeed = baseMoveSpeed * currentScale * moveSpeedScale;

//                     // 计算移動距離，使用移動速度
//                     currentPosition.x += event.deltaX * moveSpeed;
//                     currentPosition.y += event.deltaY * moveSpeed;

//                     // 限制移動範圍
//                     currentPosition.x = Math.max(Math.min(currentPosition.x, maxX), minX);
//                     currentPosition.y = Math.max(Math.min(currentPosition.y, maxY), minY);
//                 }

//                 // 添加過渡效果
//                 myElement.style.transition = 'transform ' + transitionTime + 's ease-out';

//                 // 使用 translate3d 進行硬件加速
//                 myElement.style.transform = 'scale(' + currentScale + ') translate3d(' + currentPosition.x + 'px, ' + currentPosition.y + 'px, 0)';

//                 // Reset the throttle timeout
//                 throttleTimeout = null;
//             }, 16);  // 16ms is roughly 60 frames per second
//         }
//     });
// }


function zoomMap() {
    var myElement = document.querySelector('.map');
    var timeInMs = 0;
    var last = { x: 0, y: 0, z: 1 };
    var current = { x: 0, y: 0, z: 1, width: myElement.offsetWidth, height: myElement.offsetHeight };
    var oImgRec = myElement.getBoundingClientRect();
    var cx = oImgRec.left + oImgRec.width * 0.5;
    var cy = oImgRec.top + oImgRec.height * 0.5;
    var imageCenter = { x: cx, y: cy };
    var pinchImageCenter = {};
    var deltaIssue = { x: 0, y: 0 };
    var lastEvent = '';
    var doubleTapThreshold = 300; // 連點兩下的時間閾值

    var hammer = new Hammer(myElement);
    hammer.get('pinch').set({ enable: true });
    hammer.get('pan').set({ direction: Hammer.DIRECTION_ALL }).recognizeWith(hammer.get('pinch'));

    hammer.on("pinchstart", function (e) {
        last.x = current.x;
        last.y = current.y;
        pinchImageCenter = { x: imageCenter.x + last.x, y: imageCenter.y + last.y };
        lastEvent = 'pinchstart';
    });

    hammer.on("pinchmove", function (e) {
        var newScale = (last.z * e.scale) >= 1 ? (last.z * e.scale) : 1;
        var d = scaleCal(e.center, pinchImageCenter, last.z, newScale);
        current.x = d.x + last.x;
        current.y = d.y + last.y;
        current.z = d.z + last.z;
        update();
        lastEvent = 'pinchmove';
    });

    hammer.on("pinchend", function (e) {
        last.x = current.x;
        last.y = current.y;
        last.z = current.z;
        lastEvent = 'pinchend';
    });

    hammer.on("panmove", function (e) {
        var panDelta = { x: e.deltaX, y: e.deltaY };
        if (lastEvent !== 'panmove') {
            deltaIssue = { x: panDelta.x, y: panDelta.y };
        }
        current.x = (last.x + panDelta.x - deltaIssue.x);
        current.y = (last.y + panDelta.y - deltaIssue.y);
        lastEvent = 'panmove';
        update();
    });

    hammer.on("panend", function (e) {
        last.x = current.x;
        last.y = current.y;
        lastEvent = 'panend';
    });

    hammer.on('tap', function (e) {
        if ((Date.now() - timeInMs) < doubleTapThreshold) {
            resetToOriginal();
        }
        timeInMs = Date.now();
        lastEvent = 'tap';
    });

    function resetToOriginal() {
        last.x = 0;
        last.y = 0;
        last.z = 1;
        current.x = 0;
        current.y = 0;
        current.z = 1;
        update();
    }

    function scaleCal(eCenter, originCenter, currentScale, newScale) {
        var zoomDistance = newScale - currentScale;
        var x = (originCenter.x - eCenter.x) * (zoomDistance) / currentScale;
        var y = (originCenter.y - eCenter.y) * (zoomDistance) / currentScale;
        var output = { x: x, y: y, z: zoomDistance };
        return output;
    }

    function update() {
        current.height = myElement.offsetHeight * current.z;
        current.width = myElement.offsetWidth * current.z;
        if (current.z < 1) {
            current.z = 1;
        }
        myElement.style.transform = " translate3d(" + current.x + "px, " + current.y + "px, 0) scale(" + current.z + ") ";
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
    if(JSON.stringify(lastGridData) !== JSON.stringify(data)){
        console.log(data);
        allGrids = document.querySelectorAll("[data-val]");
        allGrids.forEach(function(grid) {
            grid.classList.remove("booked");
            grid.classList.remove("occupied");
            grid.classList.remove("disable");
            gridName = grid.getAttribute("data-val");
            data.forEach(function(gdata) {
                if(gdata.station === gridName){
                    if(gdata.status === 1){
                        grid.classList.add("booked");
                    } else if(gdata.status === 2){
                        grid.classList.add("occupied");
                    } else if(gdata.status === 6){
                        grid.classList.add("disable");
                    }
                }
            });
        });
        lastGridData = data;
    }
    
}

function bindGridButton(){
    grids = document.querySelectorAll("[data-val]");

    // 为每个按钮添加点击事件处理程序
    grids.forEach(grid => {
        grid.addEventListener('click', function() {
            gridName = grid.getAttribute("data-val");
            if(grid.classList.contains('booked')){
                alert(gridName + "格位已被系統預訂，無法取消！");
            } else if(grid.classList.contains('occupied')){
                const cf = confirm("是否取消 " + gridName + " 格位佔用狀態");
                if(cf){
                    fetch(baseUrl + `/api/grid/setStatus?stationName=${gridName}&mode=clear`)
                        .then(response => {
                            if (!response.ok) {
                            throw new Error(`HTTP error! Status: ${response.status}`);
                            }

                            return response.text();
                        })
                        .then(data => {
                            alert(data);
                        })
                        .catch(error => {
                            console.error('Fetch error:', error);
                        });
                }
            } else if(!grid.classList.contains('disable')){
                const cf = confirm("是否將 " + gridName + " 格位設置佔用狀態");
                if(cf){
                    fetch(baseUrl + `/api/grid/setStatus?stationName=${gridName}&mode=occupied`)
                        .then(response => {
                            if (!response.ok) {
                            throw new Error(`HTTP error! Status: ${response.status}`);
                            }

                            return response.text();
                        })
                        .then(data => {
                            alert(data);
                        })
                        .catch(error => {
                            console.error('Fetch error:', error);
                        });
                }
            }

        });
    });
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
