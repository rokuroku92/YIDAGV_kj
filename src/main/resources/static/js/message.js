var baseUrl = window.location.origin + "/YIDAGV";

window.onload = function(){
    getMessage();
};

function getMessage() {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', baseUrl + "/api/homepage/notifications", true);
    xhr.send();
    xhr.onload = function(){
        if(xhr.status == 200){
            let data = JSON.parse(this.responseText);
            addMessage(data);
        }
    };
}
function addMessage(data){
    let messageHTML = "";
    for(let i=0;i<data.length;i++){
        var datetime = data[i].createTime;
        var year = datetime.substring(0, 4);
        var month = datetime.substring(4, 6);
        var day = datetime.substring(6, 8);
        var hour = datetime.substring(8, 10);
        var minute = datetime.substring(10, 12);
        var second = datetime.substring(12, 14);
        let level;
        switch (data[i].level) {
            case 1:
                level = "info";
                break;
            case 2:
                level = "warning";
                break;
            case 3:
                level = "danger";
                break;
            default:
                level = "info";
                break;
        }
        messageHTML += '<div class="row"><div class="col message"><div class="nfStatus '+level+'"></div><div class="messageContentDiv">' +
								'<label class="messageTitle">'+data[i].name+'</label>' +
								'<label class="messageContent">'+data[i].content+'</label>' +
								'<label class="messageTime">'+year+"/"+month+"/"+day+'&nbsp;'+hour+":"+minute+":"+second+'</label></div></div></div>';
    }
    document.getElementById("notification").innerHTML = messageHTML;
}