package com.yid.agv.controller;

import com.google.gson.Gson;
import com.yid.agv.backend.InstantStatus;
import com.yid.agv.backend.ProcessTasks;
import com.yid.agv.model.*;
import com.yid.agv.model.AgvStatus;
import com.yid.agv.service.AnalysisService;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.yid.agv.service.HomePageService;
import com.yid.agv.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final Gson gson = new Gson();
    private static AgvStatus[] agvStatuses;
    private static List<Task> tasks;

    @Autowired
    private HomePageService homePageService;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private TaskService taskService;

    @GetMapping(value = "/homepage/agvlist", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getAGVList(){
        List<AGVId> list = homePageService.queryAGVList();
        return gson.toJson(list);
    }

//    @GetMapping(value = "/homepage/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
//    public String getTasksJson() {
////            tasks = homePageService.queryTodayTasks();
//        tasks = taskService.queryAllTasks();
//        return gson.toJson(tasks);
//    }
    @GetMapping(value = "/homepage/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getTasksJson() {
//        return gson.toJson(ProcessTasks.getTaskQueue());
        Queue<QTask> q = new ArrayDeque<>();
        QTask t = new QTask();
        t.setTaskNumber("#202308010001");
        t.setAgvId(1);
        t.setModeId(1);
        t.setStartStationId(6);
        t.setTerminalStationId(12);
        t.setNotificationStationId(13);
        t.setStatus(0);
        QTask t2 = new QTask();
        t2.setTaskNumber("#202308010002");
        t2.setAgvId(1);
        t2.setModeId(1);
        t2.setStartStationId(7);
        t2.setTerminalStationId(11);
        t2.setNotificationStationId(11);
        t2.setStatus(1);
        q.offer(t);
        q.offer(t2);
        return gson.toJson(q);
    }

    @GetMapping(value = "/homepage/tasks/today", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getTodayTask(){
        List<Task> list = taskService.queryTodayTasks();
        return gson.toJson(list);
    }

    @GetMapping(value = "/homepage/tasks/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAllTask(){
        List<Task> list = taskService.queryAllTasks();
        return gson.toJson(list);
    }

    @GetMapping(value = "/homepage/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getNotifications(){
        List<Notification> list = homePageService.queryNotifications();
        return gson.toJson(list);
    }

    @GetMapping(value = "/homepage/notification/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAllNotifications(){
        List<Notification> list = homePageService.queryAllNotifications();
        return gson.toJson(list);
    }
    @GetMapping(value = "/homepage/agvStatusData", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getAgvStatusData(){
        List<MessageData> list = homePageService.queryMessageData();
        return gson.toJson(list);
    }
    @GetMapping(value = "/homepage/stationsData", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getStationsData(){
        List<Station> list = homePageService.queryStations();
        return gson.toJson(list);
    }

    @GetMapping(value = "/homepage/notificationStationsData", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getNotificationStationsData(){
        List<NotificationStation> list = homePageService.queryNotificationStations();
        return gson.toJson(list);
    }

    @GetMapping(value = "/homepage/modes", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getModes(){
        List<Mode> list = homePageService.queryModes();
        return gson.toJson(list);
    }

//    static int tempId;
//    @GetMapping(value = "/homepage/agv", produces = MediaType.APPLICATION_JSON_VALUE)
//    public String getAGVJson() {
//        if(agvStatuses == null){
//            agvStatuses = new AgvStatus[homePageService.queryAGVList().size()];
//            for (int i = 0; i < agvStatuses.length; i++) {
//                agvStatuses[i] = new AgvStatus();
//            }
//        }
//        LocalDate currentDate = LocalDate.now();
//        int year = currentDate.getYear();
//        int month = currentDate.getMonthValue();
//        int day = currentDate.getDayOfMonth();
//        for(int i=0;i<agvStatuses.length;i++){
////            agvs[i].setStatus((int) (Math.random() * 10) % 3);
//            agvStatuses[i].setStatus(0);
//            agvStatuses[i].setPlace("Station"+(tempId++%4+1));
//            agvStatuses[i].setTask("#"+year+month+day+String.format("%04d", tempId-3));
//            agvStatuses[i].setBattery(100-tempId%100);
//            agvStatuses[i].setSignal(100-tempId%100);
//            tempId+=2;
//        }
//
//        //        System.out.println(jsonString);
//        return new Gson().toJson(agvStatuses);
//    }

    @GetMapping(value = "/homepage/agv", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAGVJson() {
        return new Gson().toJson(InstantStatus.agvStatuses);
    }

    @GetMapping(value = "/homepage/station", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getStationJson() {
        return new Gson().toJson(InstantStatus.stationStatuses);
    }

    @GetMapping(value = "/analysis/yyyymm", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAnalysisesYearsAndMonths(){
        List<Map<String, Object>> list = analysisService.getAnalysisesYearsAndMonths();
        return gson.toJson(list);
    }
    // 範例路徑 /analysis/mode?agvId=1&value=202212
    // 範例路徑 /analysis/mode?agvId=2&value=202301
    // 範例路徑 /analysis/mode?agvId=3&value=recently
    @RequestMapping(value = "/analysis/mode", produces = MediaType.APPLICATION_JSON_VALUE)
    public String queryAnalysisesByAGVAndYearAndMonth(@RequestParam("value") String value, @RequestParam("agvId") Integer agvId){
        // value=202212, 202301, recently
        int year, month;
        List<Analysis> list;
        switch (value) {
            case "recently":
                list = analysisService.queryAnalysisesRecentlyByAGV(agvId);
                break;
            case "all":
                list = analysisService.queryAnalysisesByAGV(agvId);
                break;
            default:
                year = Integer.parseInt(value.substring(0, 4));
                month = Integer.parseInt(value.substring(4,6));
                list = analysisService.queryAnalysisesByAGVAndYearAndMonth(agvId, year, month);
                break;
        }
        return gson.toJson(list);
    }
    @RequestMapping(value = "/sendTask", produces = MediaType.TEXT_PLAIN_VALUE)
    public String sendTask(@RequestParam("time") String time, @RequestParam("agv") String agv,
                           @RequestParam("start") String start, @RequestParam("notification") String notification,
                           @RequestParam("mode") String mode){
        return taskService.insertTaskAndAddTask(time, agv, start, notification, mode) ? "OK" : "FAIL";
    }

    @RequestMapping(value = "/cancelTask", produces = MediaType.TEXT_PLAIN_VALUE)
    public String cancelTask(@RequestParam("taskNumber") String taskNumber){
        taskNumber = "#" + taskNumber;
        System.out.println("taskNumber: "+taskNumber);
        return taskService.cancelTask(taskNumber) ? "OK" : "FAIL";
    }
}
