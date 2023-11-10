package com.yid.agv.controller;

import com.google.gson.Gson;
import com.yid.agv.backend.station.Grid;
import com.yid.agv.backend.station.GridManager;
import com.yid.agv.dto.TaskListRequest;
import com.yid.agv.model.AGVId;
import com.yid.agv.model.Analysis;
import com.yid.agv.service.AnalysisService;

import com.yid.agv.service.GridService;
import com.yid.agv.service.HomePageService;
import com.yid.agv.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://127.0.0.1:5500")
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
    private GridManager gridManager;

    @GetMapping(value = "/homepage/agvlist", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getAGVList(){
        return gson.toJson(homePageService.queryAGVList());
    }

    @GetMapping(value = "/homepage/nowtasks", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getTasksJson() {
        return gson.toJson(taskService.queryNowTaskLists());
    }

    @GetMapping(value = "/history/tasks", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getTodayTask(){
        return gson.toJson(taskService.queryTaskLists());
    }

    @GetMapping(value = "/history/tasks/all", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getAllTask(){
        return gson.toJson(taskService.queryAllTaskLists());
    }

    @GetMapping(value = "/homepage/iElevatorObstacleAlarm", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getIAlarm(){
        return homePageService.getElevatorObstacleAlarm() ? "1" : "0";
    }

    @GetMapping(value = "/homepage/notifications", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getTodayNotifications(){
        return gson.toJson(homePageService.queryTodayNotifications());
    }

    @GetMapping(value = "/history/notifications", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getNotifications(){
        return gson.toJson(homePageService.queryNotifications());
    }

    @GetMapping(value = "/history/notifications/all", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getAllNotifications(){
        return gson.toJson(homePageService.queryAllNotifications());
    }

    @GetMapping(value = "/homepage/messageData", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getAgvStatusData(){
        return gson.toJson(homePageService.queryMessageData());
    }

    @GetMapping(value = "/grid/status", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getStationsData(){
        return gson.toJson(gridService.getGridsStatus());
    }

    @GetMapping(value = "/homepage/modes", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String getModes(){
        return gson.toJson(homePageService.queryModes());
    }
    @GetMapping(value = "/homepage/agv", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAGVJson() {
        return """
                [
                  {
                    "id": 1,
                    "status": "ONLINE",
                    "battery": 90,
                    "signal": 100,
                    "taskStatus": "PRE_TERMINAL_STATION",
                    "iLowBattery": false,
                    "lowBatteryCount": 0,
                    "reDispatchCount": 0,
                    "tagError": false,
                    "fixAgvTagErrorCompleted": false,
                    "tagErrorDispatchCompleted": false,
                    "lastTaskBuffer": false,
                    "obstacleCount": 0
                  },
                  {
                    "id": 2,
                    "status": "OBSTACLE",
                    "battery": 60,
                    "signal": 30,
                    "taskStatus": "PRE_START_STATION",
                    "iLowBattery": false,
                    "lowBatteryCount": 0,
                    "reDispatchCount": 0,
                    "tagError": false,
                    "fixAgvTagErrorCompleted": false,
                    "tagErrorDispatchCompleted": false,
                    "lastTaskBuffer": false,
                    "obstacleCount": 0
                  },
                  {
                    "id": 3,
                    "status": "ONLINE",
                    "battery": 40,
                    "signal": 60,
                    "taskStatus": "NO_TASK",
                    "iLowBattery": false,
                    "lowBatteryCount": 0,
                    "reDispatchCount": 0,
                    "tagError": false,
                    "fixAgvTagErrorCompleted": false,
                    "tagErrorDispatchCompleted": false,
                    "lastTaskBuffer": false,
                    "obstacleCount": 0
                  }
                ]
                """;
//        return new Gson().toJson(homePageService.getAgv());
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
        System.out.println("taskNumber: "+taskNumber);
        return taskService.cancelTask(taskNumber) ? "OK" : "FAIL";
    }

    @PostMapping(value = "/sendtasklist")
    public String handleTaskList(@RequestBody TaskListRequest jsonData){
        System.out.println(jsonData);
        String area = null;
        switch (jsonData.getMode()) {
            case 1 -> area = "3-" + jsonData.getTerminal();
            case 2 -> area = "2-" + jsonData.getTerminal();
            case 3 -> area = "1-" + jsonData.getTerminal();
        }
        List<Grid> availableGrids = gridManager.getAvailableGrids(area);
        System.out.println(availableGrids.size());
        if(availableGrids.size() < jsonData.getTasks().size()){
            return "終點區域格位已滿";
        }
        return "YES";
//        return taskService.addTaskList(jsonData);
    }
}
