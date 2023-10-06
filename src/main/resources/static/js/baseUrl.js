// const baseUrl = window.location.origin + "/YIDAGV";
// const baseUrl = "http://localhost:8080/YIDAGV";
var baseUrl;

if (window.location.href.startsWith('http://127.0.0.1:5500')) {
    // Live Server 环境
    console.log('Running on Live Server');
    baseUrl = "http://localhost:8080/YIDAGV";
    var baseHref = document.createElement("base");
    baseHref.href = "http://127.0.0.1:5500/static/";
    document.head.appendChild(baseHref);
} else {
    // Spring Boot 环境 window.location.href.startsWith('http://localhost:8080/YIDAGV')
    console.log('Running on Spring Boot');
    baseUrl = window.location.origin + "/YIDAGV";
    var baseHref = document.createElement("base");
    baseHref.href = "/YIDAGV/";
    document.head.appendChild(baseHref);
}
