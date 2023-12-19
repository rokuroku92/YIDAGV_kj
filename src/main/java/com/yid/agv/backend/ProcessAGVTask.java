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


    @Scheduled(fixedRate = 4000)
    public void dispatchTasks() {
        if(isRetrying)return;

        for (int i = 1; i <= agvManager.getAgvSize(); i++){
            AGV agv = agvManager.getAgv(i);
            if(agv.getStatus() != AGV.Status.ONLINE) continue;  // AGV未連線則無法派遣
            if(agv.getTask() != null) continue;  // AGV任務中

            boolean iAtStandbyStation = iEqualsStandbyStation(agv.getPlace());
            boolean taskQueueIEmpty = AGVTaskManager.isEmpty(agv.getId());

            if(agv.isILowBattery() && !iAtStandbyStation){  // TODO: 低電量時，派遣回待命點
                goStandbyTask(agv);
            } else if (!taskQueueIEmpty && !agv.isILowBattery()){  // TODO: 正常派遣
                // 這個專案不用寫優先派遣演算法
                AGVQTask goTask = AGVTaskManager.getNewTaskByAGVId(agv.getId());
                agv.setTask(goTask);
                System.out.println("Process dispatch...");
                System.out.println("AGV place: " + agv.getPlace());
                if(dispatchTaskToAGV(agv)){
                    Objects.requireNonNull(goTask).setStatus(1);
                    agv.setTaskStatus(AGV.TaskStatus.PRE_START_STATION);
                    taskListDao.updateTaskListStatus(goTask.getTaskNumber(), 1);
                } else {
                    failedTask(agv);
                    // TODO: 刪除任務，可以直接將agv抱著的task -> null
                }

            } else if (taskQueueIEmpty && !iAtStandbyStation){  // TODO: 派遣回待命點
                goStandbyTask(agv);
            }

        }

    }

    public boolean iEqualsStandbyStation(String place){
        int placeVal = Integer.parseInt(place == null ? "-1" : place);
        if (placeVal == -1) return false;

        List<Integer> standbyTags = stationDao.queryStandbyStations().stream()
                .map(Station::getTag).toList();

        for (int standbyTag : standbyTags) {
            standbyTag = ((standbyTag-1000)%250)+1000;
            if (standbyTag == placeVal
                    || standbyTag+250 == placeVal
                    || standbyTag+500 == placeVal
                    || standbyTag+750 == placeVal)
                return true;
        }

        return false;
    }

    private boolean isRetrying = false;
    public synchronized boolean dispatchTaskToAGV(AGV agv) {
        int retryCount = 0;
        while (retryCount < MAX_RETRY) {
            try {
                // Dispatch the task to the AGV control system
                AGVQTask task = agv.getTask();
                if (task == null) return false;
                String nowPlace = agv.getPlace();
                String url;
                if (task.getTaskNumber().matches("#(SB|LB).*") || agv.getTaskStatus() == AGV.TaskStatus.PRE_TERMINAL_STATION){
                    url = agvUrl + "/task0=" + task.getAgvId() + "&" + task.getModeId() + "&" + nowPlace +
                            "&" + stationIdTagMap.get(task.getTerminalStationId());
                } else if (task.getTaskNumber().startsWith("#YE")|| task.getTaskNumber().startsWith("#RE") || task.getTaskNumber().startsWith("#NE")){
                    url = agvUrl + "/task0=" + task.getAgvId() + "&" + task.getModeId() + "&" + nowPlace +
                            "&" + stationIdTagMap.get(task.getStartStationId()) + "&" + stationIdTagMap.get(task.getTerminalStationId());
                } else {
                    url = agvUrl + "/task0=" + task.getAgvId() + "&" + task.getModeId() + "&" + nowPlace +
                            "&" + stationIdTagMap.get(task.getStartStationId()) + "&" + stationIdTagMap.get(task.getTerminalStationId());
                }

                System.out.println("URL: " + url);

                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String webpageContent = response.body().trim();

                if(webpageContent.equals("OK")){
                    System.out.println("Task number " + task.getTaskNumber() + " has been dispatched.");
                    return true;
                } else if (webpageContent.equals("FAIL")) {
                    System.out.println("發送任務FAIL");
                    isRetrying = true;
                    retryCount++;
                    System.out.println("發送任務回覆失敗，3秒後重新發送");
                    notificationDao.insertMessage(NotificationDao.Title.AGV_SYSTEM, NotificationDao.Status.FAILED_SEND_TASK);
                    System.err.println("Failed to dispatch task, retrying... (Attempt " + retryCount + ")");
                    try {
                        Thread.sleep(3000); // 延遲再重新發送
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    System.err.println("Undefined AGV System message: " + webpageContent);
                    notificationDao.insertMessage(NotificationDao.Title.AGV_SYSTEM, NotificationDao.Status.ERROR_AGV_DATA);
                    return false;
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("發送任務發生意外，3秒後重新發送");
                notificationDao.insertMessage(NotificationDao.Title.AGV_SYSTEM, NotificationDao.Status.FAILED_SEND_TASK);
                isRetrying = true;
                retryCount++;
                System.err.println("Failed to dispatch task, retrying... (Attempt " + retryCount + ")");
                try {
                    Thread.sleep(3000); // 延遲再重新發送
                } catch (InterruptedException ignored) {
                }
            }
        }
        System.err.println("Failed to dispatch task after " + MAX_RETRY + " attempts.");
        System.out.println("任務發送三次皆失敗，已取消任務");
        notificationDao.insertMessage(NotificationDao.Title.AGV_SYSTEM, NotificationDao.Status.FAILED_SEND_TASK_THREE_TIMES);
        isRetrying = false;
        return false;
    }

    public void failedTask(AGV agv){
        AGVQTask task = agv.getTask();
        System.err.println("Failed task:" + task);
        AGVTaskManager.removeTaskByTaskNumber(task.getTaskNumber());
        agv.setTask(null);
    }

    public void completedTask(AGV agv, NotificationDao.Title agvTitle){
        AGVQTask task = agv.getTask();
        if(!task.getTaskNumber().matches("#(SB|LB).*")){
            int analysisId = analysisDao.getTodayAnalysisId().get(task.getAgvId() - 1).getAnalysisId();
            analysisDao.updateTask(analysisDao.queryAnalysisByAnalysisId(analysisId).getTask() + 1, analysisId);
            gridManager.setGridStatus(task.getStartStationId(), Grid.Status.FREE);  // Booked to Free
            gridManager.setGridStatus(task.getTerminalStationId(), Grid.Status.OCCUPIED);  // Booked to Occupied
        }
        System.out.println("Completed task number "+task.getTaskNumber()+".");
        notificationDao.insertMessage(agvTitle, "Completed task "+task.getTaskNumber());
        taskListDao.updateTaskListStatus(task.getTaskNumber(), 100);
        agv.setTaskStatus(AGV.TaskStatus.NO_TASK);
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

        System.out.println("toStandbyTask: " + toStandbyTask);

        taskListDao.insertTaskList(toStandbyTask.getTaskNumber(), formattedDateTime, toStandbyTask.getAgvId(),
                toStandbyTask.getStartStationId(), toStandbyTask.getTerminalStationId(),
                TaskListDao.Mode.DEFAULT, toStandbyTask.getStatus());
        dispatchTaskToAGV(agv);
    }
    public boolean getIsRetrying(){
        return isRetrying;
    }

}