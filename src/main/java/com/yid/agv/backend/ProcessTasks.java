package com.yid.agv.backend;

import com.yid.agv.backend.datastorage.AGVManager;
import com.yid.agv.backend.datastorage.TaskQueue;
import com.yid.agv.model.AgvStatus;
import com.yid.agv.model.QTask;
import com.yid.agv.model.Station;
import com.yid.agv.repository.AnalysisDao;
import com.yid.agv.repository.NotificationDao;
import com.yid.agv.repository.StationDao;
import com.yid.agv.repository.TaskDao;
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
public class ProcessTasks {
    @Value("${agvControl.url}")
    private String agvUrlValue;
    private static String agvUrl;
    @Autowired
    private TaskDao taskDao;
    @Autowired
    private StationDao stationDao;
    @Autowired
    private AnalysisDao analysisDao;
    @Autowired
    private NotificationDao notificationDao;
    @Autowired
    private TaskQueue taskQueue;
    @Autowired
    private AGVManager agvManager;

    private static Map<Integer, Integer> stationIdTagMap;
    private static QTask toStandbyTask;

    @PostConstruct
    public void _init() {
        agvUrl = agvUrlValue;
    }

    private static boolean isRetrying = false;

    @Scheduled(fixedRate = 5000)
    public void dispatchTasks() {
        if(isRetrying)return;

        if(stationIdTagMap == null) stationIdTagMap = stationDao.queryStations().stream().
                collect(Collectors.toMap(Station::getId, Station::getTag));

        if (taskQueue.iDispatch()) {
//            InstantStatus.iStandby = false;
            QTask goTask = taskQueue.peekTaskWithPlace();
            String result = dispatchTaskToAGV(notificationDao, goTask, agvManager.getAgvStatus(1).getPlace());
            if(Objects.requireNonNull(result).equals("OK")){
                taskQueue.updateTaskStatus(taskQueue.getNowTaskNumber(), 1);
                taskDao.updateTaskStatus(taskQueue.getNowTaskNumber(), 1);
            } else if (result.equals("FailedDispatch")) {
                System.out.println("發送任務三次皆失敗，已取消任務");
                notificationDao.insertMessage(1, 19);
                taskQueue.failedTask();
                taskDao.cancelTask(taskQueue.getNowTaskNumber());
            }
        }else if(taskQueue.iGoStandby() && false){
            InstantStatus.iStandbyTask = true;
            InstantStatus.iTask = true;
            goStandbyTaskByAgvId(notificationDao, taskDao, 1, agvManager.getAgvStatus(1));
        }
    }


    public static synchronized String dispatchTaskToAGV(NotificationDao notificationDao, QTask task, String nowPlace) {
        final int MAX_RETRY = 3; // 最大重試次數
        int retryCount = 0;
        while (retryCount < MAX_RETRY) {
            try {
                if (task != null) {
                    // TODO: Dispatch the task to the AGV control system
                    String url = agvUrl + "/task0=" + task.getAgvId() + "&" + task.getModeId() + "&" + nowPlace +
                            "&" + stationIdTagMap.get(task.getStartStationId()) + "&" + stationIdTagMap.get(task.getTerminalStationId());
                    // TODO: 看有沒有需要分成不一樣站點數的網址
//                    switch (task.getModeId()) {
//                        case 1 -> url = agvUrl + "/task0=" + task.getAgvId() + "&" + task.getModeId() + "&" + InstantStatus.agvStatuses[0].getPlace() +
//                                            "&" + stationIdTagMap.get(task.getStartStationId()) + "&" + stationIdTagMap.get(task.getTerminalStationId());
//                        case 2 -> url = agvUrl + "/task0=" + task.getAgvId() + "&" + task.getModeId() + "&" + InstantStatus.agvStatuses[0].getPlace() +
//                                            "&" + stationIdTagMap.get(task.getStartStationId()) + "&" + stationIdTagMap.get(task.getTerminalStationId());
//                    }
                    HttpClient httpClient = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .GET()
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    String webpageContent = response.body().trim();

                    System.out.println("Task number " + task.getTaskNumber() + " has been dispatched.");
                    if(webpageContent.equals("OK")){
                        return "OK";
                    } else if (webpageContent.equals("FAIL")) {
                        System.out.println("發送任務FAIL");
                        isRetrying = true;
                        retryCount++;
                        if (retryCount < MAX_RETRY) {
                            System.err.println("Failed to dispatch task, retrying... (Attempt " + retryCount + ")");
                            try {
                                Thread.sleep(3000); // 延遲再重新發送
                            } catch (InterruptedException ignored) {
                            }
                        } else {
                            System.err.println("Failed to dispatch task after " + MAX_RETRY + " attempts.");
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("發送任務失敗3秒後重新發送");
                // 重新發送前增加延遲
                notificationDao.insertMessage(1, 18);
                isRetrying = true;
                retryCount++;
                if (retryCount < MAX_RETRY) {
                    System.err.println("Failed to dispatch task, retrying... (Attempt " + retryCount + ")");
                    try {
                        Thread.sleep(3000); // 延遲再重新發送
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    System.err.println("Failed to dispatch task after " + MAX_RETRY + " attempts.");
                }
            }
        }
        isRetrying = false;
        return "FailedDispatch";
    }

    public static void failedTask(TaskQueue taskQueue, NotificationDao notificationDao, TaskDao taskDao){
        System.out.println("任務執行三次皆失敗，已取消任務");
        notificationDao.insertMessage(1, 17);
        taskDao.cancelTask(taskQueue.getNowTaskNumber());
        taskQueue.removeTaskByTaskNumber(taskQueue.getNowTaskNumber());
        taskQueue.setNowTaskNumber(null);
    }

    public static void completedTask(TaskQueue taskQueue, AnalysisDao analysisDao, TaskDao taskDao){
        QTask cTask = taskQueue.getTaskByTaskNumber(taskQueue.getNowTaskNumber());
        taskQueue.setBookedStation(Objects.requireNonNull(cTask, "起始站為空").getStartStationId(), 0);
        taskQueue.setBookedStation(cTask.getTerminalStationId(), 4);
        int analysisId = analysisDao.getTodayAnalysisId().get(cTask.getAgvId() - 1).getAnalysisId();
        analysisDao.updateTask(analysisDao.queryAnalysisByAnalysisId(analysisId).getTask() + 1, analysisId);
        System.out.println("Completed task number "+cTask.getTaskNumber()+".");
        taskDao.updateTaskStatus(cTask.getTaskNumber(), 100);
        taskQueue.completedTask();
    }


    public static void goStandbyTaskByAgvId(NotificationDao notificationDao, TaskDao taskDao, int agvId, AgvStatus agvStatus){
        int place = Integer.parseInt(agvStatus.getPlace());
        int standbyStation;
        if (place >= 1001 && place <= 1050){
            standbyStation = 16;
        } else if (place > 1050 && place <= 1100) {
            standbyStation = 17;
        } else {
            standbyStation = 16;
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formattedDateTime = now.format(formatter);

        toStandbyTask = new QTask();
        toStandbyTask.setAgvId(agvId);
        toStandbyTask.setModeId(1);
        toStandbyTask.setStatus(0);
        toStandbyTask.setTaskNumber("#SB"+formattedDateTime);
        toStandbyTask.setStartStationId(standbyStation);
        toStandbyTask.setTerminalStationId(standbyStation);
        toStandbyTask.setNotificationStationId(16);
        System.out.println("toStandbyTask.getTaskNumber(): "+toStandbyTask.getTaskNumber());
        System.out.println("formattedDateTime: "+formattedDateTime);
        System.out.println("toStandbyTask.getAgvId(): "+toStandbyTask.getAgvId());
        System.out.println("toStandbyTask.getStartStationId(): "+toStandbyTask.getStartStationId());
        System.out.println("toStandbyTask.getTerminalStationId(): "+toStandbyTask.getTerminalStationId());
        System.out.println("toStandbyTask.getNotificationStationId(): "+toStandbyTask.getNotificationStationId());
        System.out.println("toStandbyTask.getModeId(): "+toStandbyTask.getModeId());

        taskDao.insertTask(toStandbyTask.getTaskNumber(), formattedDateTime, Integer.toString(toStandbyTask.getAgvId()),
                Integer.toString(toStandbyTask.getStartStationId()), Integer.toString(toStandbyTask.getTerminalStationId()),
                Integer.toString(toStandbyTask.getNotificationStationId()), Integer.toString(toStandbyTask.getModeId()));
        dispatchTaskToAGV(notificationDao, toStandbyTask, agvStatus.getPlace());
    }

    public static void failedGoStandbyTask(TaskDao taskDao){
        System.out.println("Failed task number "+toStandbyTask.getTaskNumber()+".");
        taskDao.updateTaskStatus(toStandbyTask.getTaskNumber(), -1);
        toStandbyTask = null;
    }

    public static void completedGoStandbyTask(TaskDao taskDao){
        System.out.println("Completed task number "+toStandbyTask.getTaskNumber()+".");
        taskDao.updateTaskStatus(toStandbyTask.getTaskNumber(), 100);
        toStandbyTask = null;
    }

}