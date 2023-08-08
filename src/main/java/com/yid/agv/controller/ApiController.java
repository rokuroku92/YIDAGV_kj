package com.yid.agv.controller;

import com.google.gson.Gson;
import com.yid.agv.backend.datastorage.AGVManager;
import com.yid.agv.backend.datastorage.StationManager;
import com.yid.agv.model.*;
import com.yid.agv.service.AnalysisService;

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

    @Autowired
    private HomePageService homePageService;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private AGVManager agvManager;
    @Autowired
    private StationManager stationManager;

    @GetMapping(value = "/homepage/agvlist", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getAGVList(){
        List<AGVId> list = homePageService.queryAGVList();
        return gson.toJson(list);
    }
    @GetMapping(value = "/homepage/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getTasksJson() {
//        return gson.toJson(TaskQueue.getInstance().getTaskQueue());
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

    @GetMapping(value = "/homepage/agv", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAGVJson() {
        return new Gson().toJson(agvManager.getAgvStatusCopyArray());
    }

    @GetMapping(value = "/homepage/station", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getStationJson() {
        return new Gson().toJson(stationManager.getStationStatusCopyArray());
    }

    @GetMapping(value = "/analysis/yyyymm", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAnalysisYearsAndMonths(){
        List<Map<String, Object>> list = analysisService.getAnalysisYearsAndMonths();
        return gson.toJson(list);
    }
    // 範例路徑 /analysis/mode?agvId=1&value=202212
    // 範例路徑 /analysis/mode?agvId=2&value=202301
    // 範例路徑 /analysis/mode?agvId=3&value=recently
    @RequestMapping(value = "/analysis/mode", produces = MediaType.APPLICATION_JSON_VALUE)
    public String queryAnalysisByAGVAndYearAndMonth(@RequestParam("value") String value, @RequestParam("agvId") Integer agvId){
        // value=202212, 202301, recently
        int year, month;
        List<Analysis> list;
        switch (value) {
            case "recently" -> list = analysisService.queryAnalysisRecentlyByAGV(agvId);
            case "all" -> list = analysisService.queryAnalysisByAGV(agvId);
            default -> {
                year = Integer.parseInt(value.substring(0, 4));
                month = Integer.parseInt(value.substring(4, 6));
                list = analysisService.queryAnalysisByAGVAndYearAndMonth(agvId, year, month);
            }
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
