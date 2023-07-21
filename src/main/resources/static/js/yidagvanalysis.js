var baseUrl = window.location.origin + "/YIDAGV";
var myChart;
function init(){
    let ctx = document.getElementById("myChart");
    myChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [

                ],
                datasets: [{
                    label:"稼動率",
                    data: [

                    ],
                    lineTension: 0,
                    backgroundColor: 'transparent',
                    borderColor: '#007bff',
                    borderWidth: 3,
                    pointBackgroundColor: '#007bff'
                },
                {
                    label:"任務數",
                    data: [

                    ],
                    lineTension: 0,
                    backgroundColor: 'transparent',
                    borderColor: '#FFB957',
                    borderWidth: 3,
                    pointBackgroundColor: '#FFB957'
                    }]
            },
            options: {
                plugins: {
                    legend: {
                        display: false,
                    }
                },
            }
        });
    getYearsAndMonths();
}

function getYearsAndMonths(){
    var xhr = new XMLHttpRequest();
    xhr.open('GET', baseUrl+"/api/analysis/yyyymm", true);
    xhr.send();
    xhr.onload = function(){
          if(xhr.status == 200){
              var data = JSON.parse(this.responseText);
              var html = "";
              for(let i=0;i<data.length;i++){
                  var yyyy = data[i].year+"";
                  var mm = data[i].month+"";
                  mm = (mm.length == 1) ? '0'+mm : mm;
                  html+="<li><a class=\"dropdown-item\" onclick=\"reSet("+yyyy+mm+")\">"+data[i].year+"/"+data[i].month+"</a></li>";
              }
              html+="<li><a class=\"dropdown-item\" onclick=\"reSet('all')\">全部</a></li>";
              document.getElementById("yearsandmonths").innerHTML = html;
              reSet('recently');
          }
    };
}

function reSet(x){
    var xhr = new XMLHttpRequest();
    xhr.open('GET', baseUrl + "/api/analysis/mode?agvId=1&value=" + x, true);
    xhr.send();
    xhr.onload = function(){
      if(xhr.status == 200){
          var data = JSON.parse(this.responseText);
          console.log(data);
          test(data);
      }
    };
}

function test(data) {
//                var len = myChart.data.labels.length;
    var task_sum = 0;
    var work_sum = 0;
    var open_sum = 0;
    var x=0;
    myChart.data.labels = [];
    myChart.data.datasets[0].data = [];
    myChart.data.datasets[1].data = [];
    var html = "";
    for(let i=0 ; i < data.length ; i++) {
        myChart.data.labels.push(data[i].month + '/' + data[i].day);
        myChart.data.datasets[0].data.push(((data[i].workingHours/data[i].openHours)*100).toString().substring(0,2));
        myChart.data.datasets[1].data.push(data[i].task);
        task_sum += data[i].task;
        work_sum += data[i].workingHours;
        open_sum += data[i].openHours;
        if(data[i].task>0)x++;
        let week = "";
        let datawd = "";
        switch(data[i].week){
            case 1:
              week = "(一)";
              datawd = "f";
              break;
            case 2:
              week = "(二)";
              datawd = "f";
              break;
            case 3:
              week = "(三)";
              datawd = "f";
              break;
            case 4:
              week = "(四)";
              datawd = "f";
              break;
            case 5:
              week = "(五)";
              datawd = "f";
              break;
            case 6:
              week = "(六)";
              datawd = "t";
              break;
            case 7:
              week = "(日)";
              datawd = "t";
              break;
        }
        // 列印之表格
        html += "<tr id=\""+data[i].month.toString()+data[i].day.toString()+"\"><th scope=\"row\">"+data[i].month + '/' + data[i].day+week+"</th><td>"+data[i].task+"</td><td>"+
              ((data[i].workingHours/data[i].openHours)*100).toString().substring(0,2)+"%</td><td>"+data[i].workingHours+"</td><td>"+data[i].openHours+"</td>"+
              "<td><input class=\"hh\" data-wd=\""+datawd+"\" type=\"checkbox\" id=\""+data[i].month.toString()+data[i].day.toString()+"d\" checked/></td></th>";
    }
    myChart.update();
    document.getElementById("task_sum").value = task_sum;
    document.getElementById("task").value = String((task_sum/x)*100).substring(0,2);
    document.getElementById("open_sum").value = String(Math.floor(open_sum/60))+"hr";
    document.getElementById("work_sum").value = String(Math.floor(work_sum/60))+"hr";
    document.getElementById("work").value = String(work_sum/x).substring(0,4)+"hr";
    document.getElementById("rate").value = String((work_sum/open_sum)*100).substring(0,2)+"%";
    document.getElementById("pt").innerHTML = html;
    console.log(myChart.data);
    // 選取方塊
    $(function(){
    // 全選 or 全取消
        $('#checkAll').click(function(event) {
            var tr_checkbox = $('table tbody tr').find('input[type=checkbox]');
            tr_checkbox.prop('checked', $(this).prop('checked'));
            // 阻止向上冒泡，以防再次觸發點擊操作
            event.stopPropagation();
        });
        $('#cancelweekend').click(function(event) {
            var tr_checkbox = $('table tbody tr').find('input[data-wd="t"]');
            tr_checkbox.prop('checked', !$(this).prop('checked'));
            var tbr = $('table tbody tr');
            var tbrr = $('.hh');
            $('#checkAll').prop('checked', tbr.find('input[type=checkbox]:checked').length == tbrr.length ? true : false);
            // 阻止向上冒泡，以防再次觸發點擊操作
            event.stopPropagation();
        });
        // 點擊表格每一行的checkbox，表格所有選中的checkbox數 = 表格行數時，則將表頭的‘checkAll’單選框置為選中，否則置為未選中
        $('table tbody tr').find('input[type=checkbox]').click(function(event) {
            var tbr = $('table tbody tr');
            var tbrr = $('.hh');
            $('#checkAll').prop('checked', tbr.find('input[type=checkbox]:checked').length == tbrr.length ? true : false);
            // 阻止向上冒泡，以防再次觸發點擊操作
            event.stopPropagation();
            });
        // 點擊表格行(行内任意位置)，觸發選中或取消選中該行的checkbox
        $('table tbody tr').click(function() {
            $(this).find('input[type=checkbox]').click();
        });

    });
}
//  列印
function printOut() {
    $('.hh').each(function(index, elem) {
      if (!$(elem).prop('checked')) {
        console.log($(this));
        $(this).parent().parent().remove();
      }
    });
    $('input[type=checkbox]').remove();

    var printOutContent = document.getElementById("printt").innerHTML;
    var printOutContent1 = document.getElementById("summ").innerHTML;
    document.body.innerHTML = printOutContent+printOutContent1;
    var table = document.getElementById("pt");
    var len = document.getElementById("pt").rows.length;
    var llen=0;
    console.log(len);
    for(let i=0;i<len;i++)
        if(table.rows[i].innerHTML != "")llen++;
    console.log(llen);
    var task_sum = 0;
    var rate_sum = 0;
    var work_sum = 0;
    var open_sum = 0;
    for(let i=0;i<len;i++){
        if(table.rows[i].innerHTML != ""){
          task_sum += Number(table.rows[i].cells[1].innerHTML);
          rate_sum += Number(String(table.rows[i].cells[2].innerHTML).replace("%",""));
          work_sum += Number(table.rows[i].cells[3].innerHTML);
          open_sum += Number(table.rows[i].cells[4].innerHTML);
        }
    }
    console.log("task_sum: "+task_sum);
    console.log("rate_sum "+rate_sum);
    console.log("open_sum "+open_sum);
    document.getElementById("task_sum").value = task_sum;
    document.getElementById("task").value = String((task_sum/len)*100).substring(0,2);
    document.getElementById("open_sum").value = String(open_sum)+"hr";
    document.getElementById("work_sum").value = String(work_sum)+"hr";
    document.getElementById("work").value = String(work_sum/len).substring(0,4)+"hr";
    document.getElementById("rate").value = String(Math.round(rate_sum/len)).substring(0,2)+"%";
    window.print();
    window.location.reload();
}
function myexcel(){
    $('input[type=checkbox]').remove();
    $('#logog').remove();
    var html = '<meta http-equiv="content-type" content="application/vnd.ms-excel; charset=UTF-8" /><title>Excel</title>';
    html += '';
    html += document.getElementById('printt').innerHTML + '';
    window.open('data:application/vnd.ms-excel,' + encodeURIComponent(html));
    window.location.reload();
}