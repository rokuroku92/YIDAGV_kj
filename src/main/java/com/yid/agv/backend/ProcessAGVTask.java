package com.yid.agv.backend;

import com.yid.agv.backend.agv.AGV;
import com.yid.agv.backend.agv.AGVManager;
import com.yid.agv.backend.station.Grid;
import com.yid.agv.backend.station.GridManager;
import com.yid.agv.backend.agvtask.AGVTaskManager;
import com.yid.agv.backend.agvtask.AGVQTask;
import com.yid.agv.model.Station;
import com.yid.agv.repository.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProcessAGVTask {

    private static final Logger log = LoggerFactory.getLogger(ProcessAGVTask.class);

    @Value("${agvControl.url}")
    private String agvUrl;
    @Value("${http.max_retry}")
    private int MAX_RETRY;
    @Autowired
    private StationDao stationDao;
    @Autowired
    private AnalysisDao analysisDao;
    @Autowired
    private TaskListDao taskListDao;
    @Autowired
    private NotificationDao notificationDao;
    @Autowired
    private AGVTaskManager AGVTaskManager;
    @Autowired
    private AGVManager agvManager;
    @Autowired
    private GridManager gridManager;

    private static Map<Integer, Integer> stationIdTagMap;

    @PostConstruct
    public void initialize() {
        stationIdTagMap = stationDao.queryStations().stream().
                collect(Collectors.toMap(Station::getId, Station::getTag));
    }


    @Scheduled(fixedRate = 1000)
    public void dispatchTasks() {
        if(isRetrying)return;

        agvManager.getAgvs().forEach(agv -> {
            if(agv.getStatus() != AGV.Status.ONLINE) return;  // AGV未連線則無法派遣
            if(agv.getTask() != null) return;  // AGV任務中

            boolean iAtStandbyStation = iEqualsStandbyStation(agv.getPlace());
            boolean taskQueueIEmpty = AGVTaskManager.isEmpty(agv.getId());

            if(agv.isILowBattery() && !iAtStandbyStation){  // 低電量時，派遣回待命點
                goStandbyTask(agv);
            } else if (!taskQueueIEmpty && !agv.isILowBattery()){  // 正常派遣
                // 這個專案不用寫優先派遣邏輯
                AGVQTask goTask = AGVTaskManager.peekNewTaskByAGVId(agv.getId());
                log.info("Process dispatch...");
                String result = dispatchTaskToAGV(agv, goTask);
                switch (result) {
                    case "OK" -> {
                        AGVTaskManager.getNewTaskByAGVId(agv.getId());
                        agv.setTask(goTask);
                        Objects.requireNonNull(goTask).setStatus(1);
                        agv.setTaskStatus(AGV.TaskStatus.PRE_START_STATION);
                        taskListDao.updateTaskListStatus(goTask.getTaskNumber(), 1);
                    }
                    case "BUSY" -> {}
                    case "FAIL" -> {
                        AGVTaskManager.getNewTaskByAGVId(agv.getId());
                        agv.setTask(goTask);
                        failedTask(agv);
                    }
                    default -> log.warn("dispatchTaskToAGV result exception: " + result);
                }
            } else if (taskQueueIEmpty && !iAtStandbyStation){  // 派遣回待命點
                goStandbyTask(agv);
            }
        });

    }

    public boolean iEqualsStandbyStation(String place){
        int placeVal = Integer.parseInt(place == null ? "-1" : place);
        if (placeVal == -1) return false;

        List<Integer> standbyTags = stationDao.queryStandbyStations().stream()
                .map(Station::getTag).toList();

        for (int standbyTag : standbyTags) {
            standbyTag = standbyTag/1000*1000 + (standbyTag%250);
            if (standbyTag == placeVal
                    || standbyTag+250 == placeVal
                    || standbyTag+500 == placeVal
                    || standbyTag+750 == placeVal)
                return true;
        }

        return false;
    }

    private boolean isRetrying = false;
    public synchronized String dispatchTaskToAGV(AGV agv, AGVQTask task) {
        int retryCount = 0;
        while (retryCount < MAX_RETRY) {
            try {
                // Dispatch the task to the Traffic Control
                if (task == null) return null;

                String nowPlace = agv.getPlace();
                if(nowPlace.equals(task.getTerminalStation())){  // 主要是為了防止派遣回待命點時，出現無限輪迴。
                    return "FAIL";
                }
                String url;
                if (task.getTaskNumber().matches("#(SB|LB).*") || agv.getTaskStatus() == AGV.TaskStatus.PRE_TERMINAL_STATION){
                    url = agvUrl + "/task0=" + task.getAgvId() + "&" + task.getModeId() + "&" + nowPlace +
                            "&" + stationIdTagMap.get(task.getTerminalStationId());
                } else if (task.getTaskNumber().startsWith("#YE") || task.getTaskNumber().startsWith("#RE") || task.getTaskNumber().startsWith("#NE")){
                    url = agvUrl + "/task0=" + task.getAgvId() + "&" + task.getModeId() + "&" + nowPlace +
                            "&" + stationIdTagMap.get(task.getStartStationId()) + "&" + stationIdTagMap.get(task.getTerminalStationId());
                } else {
                    url = agvUrl + "/task0=" + task.getAgvId() + "&" + task.getModeId() + "&" + nowPlace +
                            "&" + stationIdTagMap.get(task.getStartStationId()) + "&" + stationIdTagMap.get(task.getTerminalStationId());
                }

                log.info("URL: " + url);

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String webpageContent = response.body().trim();

                switch (webpageContent) {
                    case "OK" -> {
                        log.info("Task number " + task.getTaskNumber() + " has been dispatched.");
                        return "OK";
                    }
                    case "BUSY" -> {
                        log.info("Send task failed: BUSY");
                        return "BUSY";
                    }
                    case "FAIL" -> {
                        isRetrying = true;
                        retryCount++;
                        notificationDao.insertMessage(NotificationDao.Title.AGV_SYSTEM, NotificationDao.Status.FAILED_SEND_TASK);
                        log.warn("Failed to dispatch task, retrying... (Attempt " + retryCount + ")");
                        try {
                            Thread.sleep(3000); // 延遲再重新發送
                        } catch (InterruptedException ignored) {
                        }
                    }
                    default -> {
                        log.warn("TrafficControl result exception: " + webpageContent);
                        notificationDao.insertMessage(NotificationDao.Title.AGV_SYSTEM, NotificationDao.Status.ERROR_AGV_DATA);
                        return "FAIL";
                    }
                }
            } catch (IOException | InterruptedException e) {
                log.warn("發送任務發生意外，3秒後重新發送");
                notificationDao.insertMessage(NotificationDao.Title.AGV_SYSTEM, NotificationDao.Status.FAILED_SEND_TASK);
                isRetrying = true;
                retryCount++;
                log.warn("Failed to dispatch task, retrying... (Attempt " + retryCount + ")");
                try {
                    Thread.sleep(3000); // 延遲再重新發送
                } catch (InterruptedException ignored) {
                }
            }
        }
        log.warn("Failed to dispatch task after " + MAX_RETRY + " attempts.");
        log.warn("任務發送三次皆失敗，已取消任務");
        failedTask(agv);
        notificationDao.insertMessage(NotificationDao.Title.AGV_SYSTEM, NotificationDao.Status.FAILED_SEND_TASK_THREE_TIMES);
        isRetrying = false;
        return "FAIL";
    }

    public synchronized String dispatchTaskToAGV(AGV agv) {
        return dispatchTaskToAGV(agv, agv.getTask());
    }

    public void failedTask(AGV agv){
        AGVQTask task = agv.getTask();
        log.info("Failed task:" + task);
        gridManager.setGridStatus(task.getStartStationId(), Grid.Status.OCCUPIED);
        gridManager.setGridStatus(task.getTerminalStationId(), Grid.Status.FREE);
        notificationDao.insertMessage(agv.getTitle(), "Failed task "+task.getTaskNumber());
        taskListDao.cancelTaskList(task.getTaskNumber());
        agv.setTaskStatus(AGV.TaskStatus.NO_TASK);
        agv.setReDispatchCount(0);
        agv.setTask(null);
    }

    public void completedTask(AGV agv){
        AGVQTask task = agv.getTask();
        if(!task.getTaskNumber().matches("#(SB|LB).*")){
            int analysisId = analysisDao.getTodayAnalysisId().get(task.getAgvId() - 1).getAnalysisId();
            analysisDao.updateTask(analysisDao.queryAnalysisByAnalysisId(analysisId).getTask() + 1, analysisId);
            gridManager.setGridStatus(task.getStartStationId(), Grid.Status.FREE);  // Booked to Free
            gridManager.setGridStatus(task.getTerminalStationId(), Grid.Status.OCCUPIED);  // Booked to Occupied
        }
        log.info("Completed task number "+task.getTaskNumber()+".");
        notificationDao.insertMessage(agv.getTitle(), "Completed task "+task.getTaskNumber());
        taskListDao.updateTaskListStatus(task.getTaskNumber(), 100);
        agv.setTaskStatus(AGV.TaskStatus.NO_TASK);
        agv.setReDispatchCount(0);
        agv.setTask(null);
    }

    public void goStandbyTask(AGV agv){
//        int place = Integer.parseInt(agv.getPlace());
        // 這個專案待命點不用選擇(低電量時)
//        String agvIdPrefix = String.valueOf(agv.getId());
        List<Station> standbyStations = stationDao.queryStandbyStations();

        Optional<Integer> standbyStation = standbyStations.stream()
//                .filter(station -> station.getName().startsWith(agvIdPrefix))
                .map(Station::getId)
                .findFirst();

        if(standbyStation.isEmpty()) {
            throw new RuntimeException();
        }

        String standbyStationName = gridManager.getGridNameByStationId(standbyStation.get());

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formattedDateTime = now.format(formatter);

        String taskNumber = agv.isILowBattery() ? "#LB" + agv.getId() + formattedDateTime : "#SB" + agv.getId() + formattedDateTime;

        AGVQTask toStandbyTask = new AGVQTask();
        toStandbyTask.setAgvId(agv.getId());
        toStandbyTask.setModeId(1);
        toStandbyTask.setStatus(1);
        toStandbyTask.setTaskNumber(taskNumber);
        toStandbyTask.setStartStationId(standbyStation.get());
        toStandbyTask.setStartStation(standbyStationName);
        toStandbyTask.setTerminalStationId(standbyStation.get());
        toStandbyTask.setTerminalStation(standbyStationName);

        agv.setTask(toStandbyTask);

        log.info("toStandbyTask: " + toStandbyTask);

        taskListDao.insertTaskList(toStandbyTask.getTaskNumber(), formattedDateTime, toStandbyTask.getAgvId(),
                toStandbyTask.getStartStationId(), toStandbyTask.getTerminalStationId(),
                TaskListDao.Mode.DEFAULT, toStandbyTask.getStatus());
        dispatchTaskToAGV(agv);
    }
    public boolean getIsRetrying(){
        return isRetrying;
    }

}