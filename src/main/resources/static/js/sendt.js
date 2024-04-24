var lastGridData = null;
document.addEventListener("DOMContentLoaded", function() {
    bindModeButton();
    bindGridButton();

    var btnGroup = document.querySelector('#stationOption');
    btnGroup.addEventListener("click", function (event) {
        // 检查是否点击了选项按钮（btn-check 类的元素）
        if (event.target.classList.contains("btn-check")) {
            switchStation(event.target);
        }
    });
    
    updateGridStatus();  //  取得狀態資料
    setInterval(updateGridStatus, 1000);  //  每秒更新

    const lastLine = localStorage.getItem("line");
    if(lastLine){
        document.getElementById(lastLine).click();
    }
});

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

function sendTask(){
    let start = document.getElementById("infrom").value;
    let terminal = document.getElementById("into").value;
    if(start === ''){
        alert("未選擇起始格位");
        return;
    }else if(terminal === ''){
        alert("未選擇終點格位");
        return;
    }
    let task = {
        startGrid: start,
        terminalGrid: terminal
    };
    fetch(baseUrl+'/api/sendtask', {
        method: 'POST',  // 可以根据需要使用不同的 HTTP 方法
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(task)
    }).then(response => {
        return response.text();
    }).then(response => {
        // 处理后端的响应
        alert(response);
    }).catch(error => {
        // 处理错误
        alert("任務發送失敗: ", error);
    });
}

function bindModeButton(){
    var btnGroup = document.querySelector('#modeOption');
    btnGroup.addEventListener("click", function (event) {
        // 检查是否点击了选项按钮（btn-check 类的元素）
        if (event.target.classList.contains("btn-check")) {
            switchLine(event.target);
        }
    });
}

function bindGridButton(){
    // 获取所有具有.grid-btn类的按钮
    const startDiv = document.querySelector('#startGrid');
    const startGrids = startDiv.querySelectorAll('.grid-btn');

    // 为每个按钮添加点击事件处理程序
    startGrids.forEach(grid => {
        grid.addEventListener('click', function() {
            if(!grid.classList.contains('booked') && grid.classList.contains('occupied') && !grid.classList.contains('disable')){
                document.getElementById("infrom").value = this.getAttribute("data-val");
            }

        });
    });

    // 获取所有具有.grid-btn类的按钮
    const terminalDiv = document.querySelector('#terminalGrid');
    const terminalGrids = terminalDiv.querySelectorAll('.grid-btn');

    // 为每个按钮添加点击事件处理程序
    terminalGrids.forEach(grid => {
        grid.addEventListener('click', function() {
            if(!grid.classList.contains('occupied') && !grid.classList.contains('booked') && !grid.classList.contains('disable')){
                document.getElementById("into").value = this.getAttribute("data-val");
            }
        });
    });
}

function switchStation(radioButton) {
    localStorage.setItem("line", radioButton.id);
    switch (radioButton.id) {
        case "stB":
            document.getElementById("modeOption").innerHTML = `<input type="radio" class="btn-check" name="modebtngroup" id="A2B" checked>
                <label class="btn btn-outline-warning" for="A2B">A->B</label>
    
                <input type="radio" class="btn-check" name="modebtngroup" id="B2C">
                <label class="btn btn-outline-warning" for="B2C">B->C</label>
    
                <input type="radio" class="btn-check" name="modebtngroup" id="B2D">
                <label class="btn btn-outline-warning" for="B2D">B->D</label>
                
                <input type="radio" class="btn-check" name="modebtngroup" id="B2H">
                <label class="btn btn-outline-warning" for="B2H">B->H</label>`;
            var x={id:"A2B"};
            switchLine(x);
            break;
        case "stE":
            document.getElementById("modeOption").innerHTML = `<input type="radio" class="btn-check" name="modebtngroup" id="C2E" checked>
                <label class="btn btn-outline-warning" for="C2E">C->E</label>
    
                <input type="radio" class="btn-check" name="modebtngroup" id="E2D">
                <label class="btn btn-outline-warning" for="E2D">E->D</label>
    
                <input type="radio" class="btn-check" name="modebtngroup" id="E2H">
                <label class="btn btn-outline-warning" for="E2H">E->H</label>`;
            var x={id:"C2E"};
            switchLine(x);
            break;
        case "stF":
            document.getElementById("modeOption").innerHTML = `<input type="radio" class="btn-check" name="modebtngroup" id="D2F" checked>
                <label class="btn btn-outline-warning" for="D2F">D->F</label>
    
                <input type="radio" class="btn-check" name="modebtngroup" id="F2H">
                <label class="btn btn-outline-warning" for="F2H">F->H</label>`;
            var x={id:"D2F"};
            switchLine(x);
            break;
        case "stG":
            document.getElementById("modeOption").innerHTML = `<input type="radio" class="btn-check" name="modebtngroup" id="H2G" checked>
                <label class="btn btn-outline-warning" for="H2G">H->G</label>
    
                <input type="radio" class="btn-check" name="modebtngroup" id="G2A">
                <label class="btn btn-outline-warning" for="G2A">G->A</label>
    
                <input type="radio" class="btn-check" name="modebtngroup" id="G2H">
                <label class="btn btn-outline-warning" for="G2H">G->H</label>`;
            var x={id:"H2G"};
            switchLine(x);
            break;
        default:
            console.log("Station error");
            break;
    }
    bindModeButton();
}

function switchLine(radioButton) {
    switch (radioButton.id) {
        case "A2B":
            document.getElementById("startGrid").innerHTML = GRID_DEF_HTML.A;
            document.getElementById("terminalGrid").innerHTML = GRID_DEF_HTML.B;
            break;
        case "B2C":
            document.getElementById("startGrid").innerHTML = GRID_DEF_HTML.B;
            document.getElementById("terminalGrid").innerHTML = GRID_DEF_HTML.C;
            break;
        case "B2D":
            document.getElementById("startGrid").innerHTML = GRID_DEF_HTML.B;
            document.getElementById("terminalGrid").innerHTML = GRID_DEF_HTML.D;
            break;
        case "B2H":
            document.getElementById("startGrid").innerHTML = GRID_DEF_HTML.B;
            document.getElementById("terminalGrid").innerHTML = GRID_DEF_HTML.H;
            break;

        case "C2E":
            document.getElementById("startGrid").innerHTML = GRID_DEF_HTML.C;
            document.getElementById("terminalGrid").innerHTML = GRID_DEF_HTML.E;
            break;
        case "E2D":
            document.getElementById("startGrid").innerHTML = GRID_DEF_HTML.E;
            document.getElementById("terminalGrid").innerHTML = GRID_DEF_HTML.D;
            break;
        case "E2H":
            document.getElementById("startGrid").innerHTML = GRID_DEF_HTML.E;
            document.getElementById("terminalGrid").innerHTML = GRID_DEF_HTML.H;
            break;
        
        case "D2F":
            document.getElementById("startGrid").innerHTML = GRID_DEF_HTML.D;
            document.getElementById("terminalGrid").innerHTML = GRID_DEF_HTML.F;
            break;
        case "F2H":
            document.getElementById("startGrid").innerHTML = GRID_DEF_HTML.F;
            document.getElementById("terminalGrid").innerHTML = GRID_DEF_HTML.H;
            break;

        case "H2G":
            document.getElementById("startGrid").innerHTML = GRID_DEF_HTML.H;
            document.getElementById("terminalGrid").innerHTML = GRID_DEF_HTML.G;
            break;
        case "G2A":
            document.getElementById("startGrid").innerHTML = GRID_DEF_HTML.G;
            document.getElementById("terminalGrid").innerHTML = GRID_DEF_HTML.A;
            break;
        case "G2H":
            document.getElementById("startGrid").innerHTML = GRID_DEF_HTML.G;
            document.getElementById("terminalGrid").innerHTML = GRID_DEF_HTML.H;
            break;
        default:
            console.log("Mode error");
            break;
    }
    bindGridButton();
    document.getElementById("infrom").value = "";
    document.getElementById("into").value = "";
    lastGridData = null;
    updateGridStatus();
}