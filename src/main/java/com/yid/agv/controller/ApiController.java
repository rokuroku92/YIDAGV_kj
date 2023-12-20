package com.yid.agv.controller;

import com.google.gson.Gson;
import com.yid.agv.backend.agv.AGV;
import com.yid.agv.backend.agvtask.AGVQTask;
import com.yid.agv.backend.station.Grid;
import com.yid.agv.backend.station.GridManager;
import com.yid.agv.dto.SettingRequest;
import com.yid.agv.dto.TaskRequest;
import com.yid.agv.model.Analysis;
import com.yid.agv.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {
    private final Gson gson = new Gson();

    @Autowired
    private HomePageService homePageService;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private GridService gridService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private GridManager gridManager;

    @GetMapping(value = "/homepage/agvlist", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getAGVList(){
        return gson.toJson(homePageService.queryAGVList());
    }

    @GetMapping(value = "/task/now", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getTasksJson() {
        return gson.toJson(taskService.queryUnCompletedTasks());
    }

    @GetMapping(value = "/task/tasks", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getTasks(){
        return gson.toJson(taskService.queryTaskLists());
    }

    @GetMapping(value = "/task/all", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getAllTask(){
        return gson.toJson(taskService.queryAllTaskLists());
    }

    @GetMapping(value = "/homepage/notifications", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getNotificationsL(){
        return gson.toJson(homePageService.queryNotificationsL());
    }

    @GetMapping(value = "/history/notifications", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getNotifications(){
        return gson.toJson(homePageService.queryNotifications());
    }

    @GetMapping(value = "/history/notifications/today", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getTodayNotifications(){
        return gson.toJson(homePageService.queryTodayNotifications());
    }

    @GetMapping(value = "/history/notifications/all", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getAllNotifications(){
        return gson.toJson(homePageService.queryAllNotifications());
    }

    @GetMapping(value = "/grid/status", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getStationsData(){
        return gson.toJson(gridService.getGridsStatus());
    }
    @RequestMapping(value = "/grid/setStatus", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    public String setGridStatus(@RequestParam("stationName") String stationName, @RequestParam("mode") String mode){
        Grid.Status status = null;
        switch (mode){
            case "clear" -> status = Grid.Status.FREE;
            case "occupied" -> status = Grid.Status.OCCUPIED;
        }
        return status == null ? "模式錯誤！！" : gridService.setGridStatus(stationName, status);
    }

    @GetMapping(value = "/homepage/modes", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getModes(){
        return gson.toJson(homePageService.queryModes());
    }
    @GetMapping(value = "/homepage/agv", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAGVJson() {
//        AGV[] result = new AGV[1];
//        AGV testAgv = new AGV(1);
//        testAgv.setStatus(AGV.Status.ONLINE);
//        testAgv.setBattery(90);
//        testAgv.setSignal(88);
//        testAgv.setTaskStatus(AGV.TaskStatus.PRE_TERMINAL_STATION);
//        testAgv.setILowBattery(false);
//        testAgv.setLowBatteryCount(0);
//        testAgv.setReDispatchCount(0);
//        testAgv.setTagError(false);
//        testAgv.setFixAgvTagErrorCompleted(false);
//        testAgv.setTagErrorDispatchCompleted(false);
//        testAgv.setLastTaskBuffer(false);
//        testAgv.setObstacleCount(0);
//        AGVQTask task = new AGVQTask();
//        task.setAgvId(1);
//        task.setModeId(1);
//        task.setStatus(0);
//        task.setTaskNumber("#TT20231124103455");
//        task.setStartStationId(1);
//        task.setStartStation("A-1");
//        task.setTerminalStationId(8);
//        task.setTerminalStation("B-1");
//        testAgv.setTask(task);
//
//        result[0] = testAgv;
//        return gson.toJson(result);
        return new Gson().toJson(homePageService.getAgv());
    }

    @GetMapping(value = "/analysis/yyyymm", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAnalysisYearsAndMonths(){
        return gson.toJson(analysisService.getAnalysisYearsAndMonths());
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

    @RequestMapping(value = "/cancelTask", produces = MediaType.TEXT_PLAIN_VALUE)
    public String cancelTask(@RequestParam("taskNumber") String taskNumber){
        taskNumber = "#" + taskNumber;
//        System.out.println("taskNumber: "+taskNumber);
        return taskService.cancelTask(taskNumber) ? "OK" : "FAIL";
    }

    @PostMapping(value = "/sendtask")
    public String handleTaskList(@RequestBody TaskRequest jsonData){
        return taskService.addTask(jsonData);
//        System.out.println(jsonData);
//        return "OK";
    }
    @GetMapping(value = "/getConfig")
    public String getConfig() throws IOException {
        return gson.toJson(settingService.getConfig());
    }
    @PostMapping(value = "/setConfig")
    public String setConfig(@RequestBody SettingRequest settingRequest){
        return settingService.updateConfig(settingRequest);
    }
}
