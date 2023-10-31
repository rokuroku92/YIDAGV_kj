package com.yid.agv.backend;

import com.yid.agv.backend.agv.AGVManager;
import com.yid.agv.backend.task.TaskQueue;
import com.yid.agv.backend.agv.AgvStatus;
import com.yid.agv.backend.task.QTask;
import com.yid.agv.model.Station;
import com.yid.agv.repository.AnalysisDao;
import com.yid.agv.repository.NotificationDao;
import com.yid.agv.repository.StationDao;
import com.yid.agv.repository.TaskDao;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${http.max_retry}")
    private int maxRetry;
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

    private static int MAX_RETRY;

    private static Map<Integer, Integer> stationIdTagMap;
    private static QTask toStandbyTask;


    @PostConstruct
    public void _init() {
        agvUrl = agvUrlValue;
        MAX_RETRY = maxRetry;
        stationIdTagMap = stationDao.queryStations().stream().
                collect(Collectors.toMap(Station::getId, Station::getTag));
    }

    private static boolean isRetrying = false;

//    @Scheduled(fixedRate = 5000)
    public void dispatchTasks() {
        if(isRetrying || InstantStatus.iTask)return;
        if(agvManager.getAgvStatus(1).getStatus() != 2) return;  // AGV未連線則無法派遣 TODO: 改成2，原4

        if (InstantStatus.getAgvLowBattery()[0] && !taskQueue.iEqualsStandbyStation()){
            InstantStatus.iStandbyTask = true;
            goStandbyTaskByAgvId(notificationDao, taskDao, 1, agvManager.getAgvStatus(1), true);
        } else if (taskQueue.iDispatch()) {
//            InstantStatus.iStandby = false;
            QTask goTask = taskQueue.peekTaskWithPlace();
            System.out.println("Process dispatch...");
            System.out.println(agvManager.getAgvStatus(1).getPlace());
            String result = dispatchTaskToAGV(notificationDao, goTask, agvManager.getAgvStatus(1).getPlace(), 1);
            if(Objects.requireNonNull(result).equals("OK")){
                taskQueue.updateTaskStatus(taskQueue.getNowTaskNumber(), 1);
                InstantStatus.startStation = goTask.getStartStationId();
                InstantStatus.terminalStation = goTask.getTerminalStationId();
            } else if (result.equals("FailedDispatch")) {
                System.out.println("發送任務三次皆失敗，已取消任務");
                notificationDao.insertMessage(NotificationDao.Title.AGV_SYSTEM, NotificationDao.Status.FAILED_SEND_TASK_THREE_TIMES);
                taskQueue.failedTask();
            }
        }else if(taskQueue.isEmpty() && !taskQueue.iEqualsStandbyStation()){
            InstantStatus.iStandbyTask = true;
            goStandbyTaskByAgvId(notificationDao, taskDao, 1, agvManager.getAgvStatus(1), false);
        }
    }


    public static synchronized String dispatchTaskToAGV(NotificationDao notificationDao, QTask task, String nowPlace, int mode) {
        int retryCount = 0;
        while (retryCount < MAX_RETRY) {
            try {
                if (task != null) {
//                    if(nowPlace.equals("1001")) nowPlace = "1501";
                    // Dispatch the task to the AGV control system
                    String url;
                    if (task.getStartStationId() == 16 || task.getStartStationId() == 17 || mode == 2){
                        url = agvUrl + "/task0=" + task.getAgvId() + "&" + task.getModeId() + "&" + nowPlace +
                                "&" + stationIdTagMap.get(task.getTerminalStationId());
                    } else {
                        url = agvUrl + "/task0=" + task.getAgvId() + "&" + task.getModeId() + "&" + nowPlace +
                                "&" + stationIdTagMap.get(task.getStartStationId()) + "&" + stationIdTagMap.get(task.getTerminalStationId());
                    }

                    System.out.println("URL: " + url);

                    // TODO: 看有沒有需要分成不一樣站點數的網址

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
                    } else {
//                        return "OK";
                        return "FailedDispatch";
                    }
                } else {
                    return "Task null";
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("發送任務失敗3秒後重新發送");
                // 重新發送前增加延遲
                notificationDao.insertMessage(NotificationDao.Title.AGV_SYSTEM, NotificationDao.Status.FAILED_SEND_TASK);
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
        notificationDao.insertMessage(NotificationDao.Title.AGV_SYSTEM, NotificationDao.Status.FAILED_EXECUTION_TASK_THREE_TIMES);
        taskDao.cancelTask(taskQueue.getNowTaskNumber());
        taskQueue.removeTaskByTaskNumber(taskQueue.getNowTaskNumber());
        taskQueue.setNowTaskNumber("");
    }

    public static void completedTask(TaskQueue taskQueue, AnalysisDao analysisDao){
        QTask cTask = taskQueue.getTaskByTaskNumber(taskQueue.getNowTaskNumber());
        taskQueue.setBookedStation(Objects.requireNonNull(cTask, "起始站為空").getStartStationId(), 0);
        taskQueue.setBookedStation(cTask.getTerminalStationId(), 4);
        int analysisId = analysisDao.getTodayAnalysisId().get(cTask.getAgvId() - 1).getAnalysisId();
        analysisDao.updateTask(analysisDao.queryAnalysisByAnalysisId(analysisId).getTask() + 1, analysisId);
        System.out.println("Completed task number "+cTask.getTaskNumber()+".");
        taskQueue.completedTask();
        taskQueue.setNowTaskNumber("");
    }

    private static final int[] stationTag1 = new int[]{1501, 1252, 1254, 1256, 1258, 1260};
    private static final int[] stationTag2 = new int[]{1524, 1513, 1515, 1517, 1771, 1773};
    public static void goStandbyTaskByAgvId(NotificationDao notificationDao, TaskDao taskDao, int agvId, AgvStatus agvStatus, boolean lowBattery){
        int place = Integer.parseInt(agvStatus.getPlace());
        int standbyStation = -1;

        for (int tag: stationTag1) {
            if (place == tag) {
                standbyStation = 16;
                break;
            }
        }
        if(standbyStation == -1){
            for (int tag: stationTag2) {
                if (place == tag) {
                    standbyStation = 17;
                    break;
                }
            }
        }
        if(standbyStation == -1 || lowBattery){
            standbyStation = 16;
        }


        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formattedDateTime = now.format(formatter);

        toStandbyTask = new QTask();
        if(!lowBattery) {
            toStandbyTask.setAgvId(agvId);
            toStandbyTask.setModeId(0);
            toStandbyTask.setStatus(0);
            toStandbyTask.setTaskNumber("#SB" + formattedDateTime);
            toStandbyTask.setStartStationId(standbyStation);
            toStandbyTask.setTerminalStationId(standbyStation);
            toStandbyTask.setNotificationStationId(16);
        } else {
            toStandbyTask.setAgvId(agvId);
            toStandbyTask.setModeId(0);
            toStandbyTask.setStatus(0);
            toStandbyTask.setTaskNumber("#LB" + formattedDateTime);
            toStandbyTask.setStartStationId(standbyStation);
            toStandbyTask.setTerminalStationId(standbyStation);
            toStandbyTask.setNotificationStationId(16);
        }
        System.out.println("toStandbyTask.getTaskNumber(): "+toStandbyTask.getTaskNumber());
        System.out.println("formattedDateTime: "+formattedDateTime);
        System.out.println("toStandbyTask.getAgvId(): "+toStandbyTask.getAgvId());
        System.out.println("toStandbyTask.getStartStationId(): "+toStandbyTask.getStartStationId());
        System.out.println("toStandbyTask.getTerminalStationId(): "+toStandbyTask.getTerminalStationId());
        System.out.println("toStandbyTask.getNotificationStationId(): "+toStandbyTask.getNotificationStationId());
        System.out.println("toStandbyTask.getModeId(): "+toStandbyTask.getModeId());

//        taskDao.insertTask(toStandbyTask.getTaskNumber(), formattedDateTime, Integer.toString(toStandbyTask.getAgvId()),
//                Integer.toString(toStandbyTask.getStartStationId()), Integer.toString(toStandbyTask.getTerminalStationId()),
//                Integer.toString(toStandbyTask.getNotificationStationId()), Integer.toString(toStandbyTask.getModeId()));
        dispatchTaskToAGV(notificationDao, toStandbyTask, agvStatus.getPlace(), 1);
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
    public static boolean getIsRetrying(){
        return isRetrying;
    }
}