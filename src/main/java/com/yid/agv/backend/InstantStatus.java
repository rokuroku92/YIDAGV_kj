package com.yid.agv.backend;

import com.yid.agv.backend.datastorage.AGVManager;
import com.yid.agv.backend.datastorage.StationManager;
import com.yid.agv.backend.datastorage.TaskQueue;
import com.yid.agv.model.AgvStatus;
import com.yid.agv.model.QTask;
import com.yid.agv.model.Station;
import com.yid.agv.model.StationStatus;
import com.yid.agv.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class InstantStatus {

    @Autowired
    private TestFakeData testFakeData;
    @Value("${agvControl.url}")
    private String agvUrl;
    @Autowired
    private AGVIdDao agvIdDao;
    @Autowired
    private StationDao stationDao;
    @Autowired
    private TaskDao taskDao;
    @Autowired
    private NotificationDao notificationDao;
    @Autowired
    private AnalysisDao analysisDao;
    @Autowired
    private AGVManager agvManager;
    @Autowired
    private TaskQueue taskQueue;
    @Autowired
    private StationManager stationManager;

    @Autowired
    @Qualifier("threadPoolTaskExecutorOfReDispatch")
    private ThreadPoolTaskExecutor reDispatchExecutor;  // 專門給dispatchTaskToAGV

    @Autowired
    @Qualifier("threadPoolTaskExecutorOfSendCaller")
    private ThreadPoolTaskExecutor sendCallerExecutor;  // 專門給caller

    private static Map<Integer, Integer> stationIdTagMap;
    private int reDispatch = 0;
    public static boolean iTask = false;
    public static boolean iStandbyTask = false;

    private boolean[] iOffline;  // 用來阻擋，避免重複計入資料庫。
    private String[] lastAgvStatusData;  // 用來阻擋，避免重複計入資料庫。

    @PostConstruct
    public void __init() {
        stationIdTagMap = stationDao.queryStations().stream()
                .collect(Collectors.toMap(Station::getId, Station::getTag));
        lastAgvStatusData = new String[agvIdDao.queryAGVList().size()];
        iOffline = new boolean[agvIdDao.queryAGVList().size()];
        Arrays.fill(lastCaller, -1);
        for (int i = 1; i <= 15; i++) {
            callerStationStatusMap.put(i, 0);
        }
    }

    @Scheduled(fixedRate = 1000) // 每秒執行
    public void updateAgvStatuses() {
        // 抓取AGV狀態，並更新到agvStatuses
//        String[] agvStatusData = testFakeData.crawlAGVStatus().orElse(new String[0]); // TODO: Fake data
        String[] agvStatusData = crawlAGVStatus().orElse(new String[0]);
        for (int i = 0; i < agvManager.getAgvLength() && agvStatusData.length != 0; i++) {
            AgvStatus agvStatus = agvManager.getAgvStatus(i+1);
            String[] data = agvStatusData[i].split(",");
            NotificationDao.Title agvTitle = NotificationDao.Title.AGV_1; // TODO: 目前只有一台車

            updateAGVBasicStatus(agvStatus, data, agvTitle, i);
            updateTaskStatus(data[1].trim());
        }
    }

    public enum TaskProgress{PRE_START_STATION, PRE_TERMINAL_STATION, COMPLETED}
    public static TaskProgress taskProgress = TaskProgress.PRE_START_STATION;
    public static int startStation;
    public static int terminalStation;
    private void updateTaskStatus(String place){
        switch (taskProgress){
            case PRE_START_STATION -> {
                if (startStation!=0 && place.equals(Integer.toString(stationIdTagMap.get(startStation)))){
                    taskQueue.setBookedStation(startStation,0);
                    taskProgress = TaskProgress.PRE_TERMINAL_STATION;
                }
            }
            case PRE_TERMINAL_STATION -> {
                if (terminalStation!=0 && place.equals(Integer.toString(stationIdTagMap.get(terminalStation)))){
                    taskQueue.setBookedStation(terminalStation,0);
                    taskProgress = TaskProgress.COMPLETED;
                }
            }
        }
    }


    private void updateAGVBasicStatus(AgvStatus agvStatus, String[] data, NotificationDao.Title agvTitle, int i){
        if(data[0].trim().equals("-1")){  // 車號為-1時，判定為AGV離線
            if(!iOffline[i]){
                updateAGVOfflineStatus(agvStatus, agvTitle, i);
                iOffline[i] = true;
            }
        } else {
            updateAGVOnlineStatus(agvStatus, data, agvTitle, i);
            iOffline[i] = false;
        }
    }

    private void updateAGVOfflineStatus(AgvStatus agvStatus, NotificationDao.Title agvTitle, int i){
        agvStatus.setStatus(AgvStatus.Status.OFFLINE);
        notificationDao.insertMessage(agvTitle, NotificationDao.Status.OFFLINE);
        CountUtilizationRate.isPoweredOn[i] = false;
        CountUtilizationRate.isWorking[i] = false;
    }

    private void updateAGVOnlineStatus(AgvStatus agvStatus, String[] data, NotificationDao.Title agvTitle, int i){
        CountUtilizationRate.isPoweredOn[i] = true;

        // data[1] 位置
        agvStatus.setPlace(data[1].trim());
        // data[2] 訊號
        agvStatus.setSignal(Integer.parseInt(data[2].trim()));
        // data[3] 電量
        agvStatus.setBattery(Integer.parseInt(data[3].trim()));

        // data[5] agv狀態
        if (!Objects.equals(lastAgvStatusData[i], data[5].trim())) {
            updateAgvStatus(agvStatus, data, agvTitle);
            lastAgvStatusData[i] = data[5].trim();
        }

        if(agvStatus.getStatus() == 2) {
            // data[4] 任務狀態
            boolean[] taskStatus = parseAGVStatus(Integer.parseInt(data[4].trim()));
            if (taskStatus[7]) {
                handleFailedTask(agvStatus);
            } else {
                if (taskStatus[0]) {
                    handleExecutingTask(agvStatus, i);
                } else {
                    handleCompletedTask(agvStatus, i);
                }
            }
        }


    }

    private void handleFailedTask(AgvStatus agvStatus){
        agvStatus.setTask("任務執行失敗");
        if (!iStandbyTask){
            if(reDispatch < 3) {
                if(!ProcessTasks.getIsRetrying()){
                    agvStatus.setTask("重新執行任務");
                    notificationDao.insertMessage(NotificationDao.Title.AGV_SYSTEM, NotificationDao.Status.FAILED_EXECUTION_TASK);
                    doReDispatch(agvStatus.getPlace());
                    reDispatch++;
                }
            } else if (reDispatch == 3) {
                ProcessTasks.failedTask(taskQueue, notificationDao, taskDao);
                reDispatch=0;
                iTask = false;
            }
        } else {
            notificationDao.insertMessage(NotificationDao.Title.AGV_SYSTEM, NotificationDao.Status.FAILED_EXECUTION_TASK);
            ProcessTasks.failedGoStandbyTask(taskDao);
            iStandbyTask = false;
            iTask = false;
        }
    }

    @Async
    private void doReDispatch(String place){
        reDispatchExecutor.execute(() ->
                ProcessTasks.dispatchTaskToAGV(notificationDao, taskQueue.getTaskByTaskNumber(taskQueue.getNowTaskNumber()), place)
        );
    }

    private void handleExecutingTask(AgvStatus agvStatus, int i){
        if(!iStandbyTask) {
            agvStatus.setTask(Optional.ofNullable(taskQueue.getNowTaskNumber()).orElse(""));
            CountUtilizationRate.isWorking[i] = true;
        }
        iTask = true;
    }

    private void handleCompletedTask(AgvStatus agvStatus, int i){
        agvStatus.setTask("");
        CountUtilizationRate.isWorking[i] = false;
        if (iTask) {
            // 任務完成(task完成)
            if (!iStandbyTask) {
                QTask cTask = taskQueue.getTaskByTaskNumber(taskQueue.getNowTaskNumber());
                callerStationStatus[cTask.getNotificationStationId()-1] = true;
                callerStationStatusMap.put(cTask.getTerminalStationId(), cTask.getNotificationStationId());
                ProcessTasks.completedTask(taskQueue, analysisDao);
                // 這邊可以判斷是否還有任務，直接派回待命點，執行效率會更好，但寫在ProcessTasks邏輯較正確，目前實作在ProcessTasks。
                iTask = false;
            } else {
                iTask = false;
                iStandbyTask = false;
                ProcessTasks.completedGoStandbyTask(taskDao);
            }
            reDispatch = 0;
        }
    }

    private void updateAgvStatus(AgvStatus agvStatus, String[] data, NotificationDao.Title agvTitle){
        switch (Integer.parseInt(data[5].trim()) / 10) {
            case 0 -> {
                switch (Integer.parseInt(data[5].trim()) % 10) {
                    case 0 -> {
                        // AGV 重新啟動
                        agvStatus.setStatus(AgvStatus.Status.REBOOT);
                        notificationDao.insertMessage(agvTitle, NotificationDao.Status.REBOOT);
                    }
                    case 1 -> {
                        // AGV 手動模式
                        agvStatus.setStatus(AgvStatus.Status.MANUAL);
                        notificationDao.insertMessage(agvTitle, NotificationDao.Status.MANUAL);
                    }
                    case 2 -> {
                        // AGV 連線中(自動上位模式)
                        agvStatus.setStatus(AgvStatus.Status.ONLINE);
                        notificationDao.insertMessage(agvTitle, NotificationDao.Status.ONLINE);
                    }
                    default -> {
                        // 系統異常資料
                        agvStatus.setStatus(AgvStatus.Status.ERROR_AGV_DATA);
                        System.out.println("異常agv狀態資料");
                        notificationDao.insertMessage(NotificationDao.Title.AGV_SYSTEM, NotificationDao.Status.ERROR_AGV_DATA);
                    }
                }
            }
            case 1 -> {
                // AGV 緊急停止
                agvStatus.setStatus(AgvStatus.Status.STOP);
                notificationDao.insertMessage(agvTitle, NotificationDao.Status.STOP);
            }
            case 2 -> {
                // AGV 出軌
                agvStatus.setStatus(AgvStatus.Status.DERAIL);
                notificationDao.insertMessage(agvTitle, NotificationDao.Status.DERAIL);
            }
            case 3 -> {
                // AGV 發生碰撞
                agvStatus.setStatus(AgvStatus.Status.COLLIDE);
                notificationDao.insertMessage(agvTitle, NotificationDao.Status.COLLIDE);
            }
            case 4 -> {
                // AGV 前有障礙
                agvStatus.setStatus(AgvStatus.Status.OBSTACLE);
                notificationDao.insertMessage(agvTitle, NotificationDao.Status.OBSTACLE);
            }
            case 5 -> {
                // AGV 轉向角度過大
                agvStatus.setStatus(AgvStatus.Status.EXCESSIVE_TURN_ANGLE);
                notificationDao.insertMessage(agvTitle, NotificationDao.Status.EXCESSIVE_TURN_ANGLE);
            }
            case 6 -> {
                // AGV 卡號錯誤
                agvStatus.setStatus(AgvStatus.Status.WRONG_TAG_NUMBER);
                notificationDao.insertMessage(agvTitle, NotificationDao.Status.WRONG_TAG_NUMBER);
            }
            case 7 -> {
                // AGV 未知卡號
                agvStatus.setStatus(AgvStatus.Status.UNKNOWN_TAG_NUMBER);
                notificationDao.insertMessage(agvTitle, NotificationDao.Status.UNKNOWN_TAG_NUMBER);
            }
            case 8 -> {
                // AGV 異常排除
                agvStatus.setStatus(AgvStatus.Status.EXCEPTION_EXCLUSION);
                notificationDao.insertMessage(agvTitle, NotificationDao.Status.EXCEPTION_EXCLUSION);
            }
            case 9 -> {
                // AGV 感知器偵測異常
                agvStatus.setStatus(AgvStatus.Status.SENSOR_ERROR);
                notificationDao.insertMessage(agvTitle, NotificationDao.Status.SENSOR_ERROR);
            }
            case 10 -> {
                // AGV 充電異常
                agvStatus.setStatus(AgvStatus.Status.CHARGE_ERROR);
                notificationDao.insertMessage(agvTitle, NotificationDao.Status.CHARGE_ERROR);
            }
            default -> {
                // 系統異常資料
                agvStatus.setStatus(AgvStatus.Status.ERROR_AGV_DATA);
                System.out.println("異常agv狀態資料");
                notificationDao.insertMessage(NotificationDao.Title.AGV_SYSTEM, NotificationDao.Status.ERROR_AGV_DATA);
            }
        }
    }


    private static final HashMap<Integer, Integer> callerStationStatusMap = new HashMap<>();
    private final boolean[] callerStationStatus = new boolean[15];
    private final boolean[] iCallerConn = new boolean[14];
    @Scheduled(fixedRate = 1000) // 每秒執行
    public void updateStationStatuses() {
        // 抓取Station狀態與Booking，並更新到stationStatuses
        StationStatus[] notBookedStationStatuses = new StationStatus[15];

//        String[] stationValue= testFakeData.crawlStationStatus().orElse(new String[0]);  // TODO: Fake data
        String[] callerValue = crawlCallerStatus().orElse(new String[0]);

        if(callerValue.length != 15) return;

        boolean[] callerStatus = new boolean[15];

        for (int i = 0; i < 5; i++) {
            notBookedStationStatuses[i] = new StationStatus();
            notBookedStationStatuses[i].setStatus(StationStatus.Status.DISABLE);
        }

        for (int i = 0; i < 14; i++) {
            if(!Objects.equals(callerValue[i].split(",")[1], "-1")){
                if(!iCallerConn[i]){
                    iCallerConn[i] = true;
//                    switch (i){
//                        case 0 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_1, NotificationDao.Status.ONLINE);
//                        case 1 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_2, NotificationDao.Status.ONLINE);
//                        case 2 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_3, NotificationDao.Status.ONLINE);
//                        case 3 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_4, NotificationDao.Status.ONLINE);
//                        case 4 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_5, NotificationDao.Status.ONLINE);
//                        case 5 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_6, NotificationDao.Status.ONLINE);
//                        case 6 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_7, NotificationDao.Status.ONLINE);
//                        case 7 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_8, NotificationDao.Status.ONLINE);
//                        case 8 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_9, NotificationDao.Status.ONLINE);
//                        case 9 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_10, NotificationDao.Status.ONLINE);
//                        case 10 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_11, NotificationDao.Status.ONLINE);
//                        case 11 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_12, NotificationDao.Status.ONLINE);
//                        case 12 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_13, NotificationDao.Status.ONLINE);
//                        case 13 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_14, NotificationDao.Status.ONLINE);
//                    }
                }
            } else {
                if(iCallerConn[i]){
                    iCallerConn[i] = false;
//                    switch (i){
//                        case 0 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_1, NotificationDao.Status.OFFLINE);
//                        case 2 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_3, NotificationDao.Status.OFFLINE);
//                        case 4 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_5, NotificationDao.Status.OFFLINE);
//                        case 3 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_4, NotificationDao.Status.OFFLINE);
//                        case 1 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_2, NotificationDao.Status.OFFLINE);
//                        case 5 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_6, NotificationDao.Status.OFFLINE);
//                        case 6 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_7, NotificationDao.Status.OFFLINE);
//                        case 7 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_8, NotificationDao.Status.OFFLINE);
//                        case 8 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_9, NotificationDao.Status.OFFLINE);
//                        case 9 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_10, NotificationDao.Status.OFFLINE);
//                        case 10 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_11, NotificationDao.Status.OFFLINE);
//                        case 11 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_12, NotificationDao.Status.OFFLINE);
//                        case 12 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_13, NotificationDao.Status.OFFLINE);
//                        case 13 -> notificationDao.insertMessage(NotificationDao.Title.CALLER_14, NotificationDao.Status.OFFLINE);
//                    }
                }
                lastCaller[i] = -1;
            }
        }

        // 讀取叫車器資料(寫死)，只讀6, 7, 13, 14 叫車器
        int callerIndex = 5;
        boolean[] dataValue;
        if(iCallerConn[5]){
            dataValue = parseCallerStatus(Long.parseLong(callerValue[5].split(",")[1]));
            for (int j = 0; j < 4; j++) {
                notBookedStationStatuses[callerIndex] = new StationStatus();
                if (dataValue[j]) {
                    notBookedStationStatuses[callerIndex].setStatus(StationStatus.Status.OWN_PALLET);
                } else {
                    notBookedStationStatuses[callerIndex].setStatus(StationStatus.Status.NOT_OWN_PALLET);
                }
                callerIndex++;
            }
        } else {
            for (int j = 0; j < 4; j++) {
                notBookedStationStatuses[callerIndex] = new StationStatus();
                notBookedStationStatuses[callerIndex].setStatus(StationStatus.Status.DISABLE);
                callerIndex++;
            }
            System.out.println("6號叫車器失去連線"); // TODO: Insert database.
        }

        if(iCallerConn[6]) {
            dataValue = parseCallerStatus(Long.parseLong(callerValue[6].split(",")[1]));
            notBookedStationStatuses[callerIndex] = new StationStatus();
            if (dataValue[0]) {
                notBookedStationStatuses[callerIndex].setStatus(StationStatus.Status.OWN_PALLET);
            } else {
                notBookedStationStatuses[callerIndex].setStatus(StationStatus.Status.NOT_OWN_PALLET);
            }
        } else {
            notBookedStationStatuses[callerIndex] = new StationStatus();
            notBookedStationStatuses[callerIndex].setStatus(StationStatus.Status.DISABLE);
            System.out.println("7號叫車器失去連線"); // TODO: Insert database.
        }
        callerIndex++;

        if(iCallerConn[12]) {
            dataValue = parseCallerStatus(Long.parseLong(callerValue[12].split(",")[1]));
            for (int j = 0; j < 4; j++) {
                notBookedStationStatuses[callerIndex] = new StationStatus();
                if (dataValue[j]) {
                    notBookedStationStatuses[callerIndex].setStatus(StationStatus.Status.OWN_PALLET);
                } else {
                    notBookedStationStatuses[callerIndex].setStatus(StationStatus.Status.NOT_OWN_PALLET);
                }
                callerIndex++;
            }
        } else {
            for (int j = 0; j < 4; j++) {
                notBookedStationStatuses[callerIndex] = new StationStatus();
                notBookedStationStatuses[callerIndex].setStatus(StationStatus.Status.DISABLE);
                callerIndex++;
            }
            System.out.println("13號叫車器失去連線"); // TODO: Insert database.
        }
        if(iCallerConn[13]) {
            dataValue = parseCallerStatus(Long.parseLong(callerValue[13].split(",")[1]));
            notBookedStationStatuses[callerIndex] = new StationStatus();
            if (dataValue[0]) {
                notBookedStationStatuses[callerIndex].setStatus(StationStatus.Status.OWN_PALLET);
            } else {
                notBookedStationStatuses[callerIndex].setStatus(StationStatus.Status.NOT_OWN_PALLET);
            }
        } else {
            notBookedStationStatuses[callerIndex] = new StationStatus();
            notBookedStationStatuses[callerIndex].setStatus(StationStatus.Status.DISABLE);
            System.out.println("14號叫車器失去連線"); // TODO: Insert database.
        }

        // 處理 notBookedStationStatuses
        for(int i = 0; i < 15; i++) {
            int processBookedStation = taskQueue.getBookedStationStatusByStation(i+1);
            int notBookedStatus = notBookedStationStatuses[i].getStatus();
            StationStatus status = stationManager.getStationStatus(i+1);
            if(processBookedStation == 1 && notBookedStatus == 0){
                // 錯誤，任務起始站棧板離開。
                System.out.println("錯誤，任務起始站棧板離開。");
                status.setStatus(StationStatus.Status.UNEXPECTED_PALLET);
                callerStatus[i] = true;  // TODO: callStationAlarm
            }else if(processBookedStation == 1 && notBookedStatus == 1){
                status.setStatus(StationStatus.Status.BOOKING);
            }else if(processBookedStation == 2 && notBookedStatus == 1){
                // 錯誤，任務終點站上有其他棧板。
                System.out.println("錯誤，任務終點站上有其他棧板。");
                status.setStatus(StationStatus.Status.UNEXPECTED_PALLET);
                callerStatus[i] = true;  // TODO: callStationAlarm
            }else if(processBookedStation == 2 && notBookedStatus == 0){
                status.setStatus(StationStatus.Status.BOOKING);
            }else if(processBookedStation == 4 && notBookedStatus == 0){
                taskQueue.setBookedStation(i+1, 0);
                callerStationStatus[callerStationStatusMap.get(i+1)-1] = false;
                callerStationStatusMap.put(i+1, 0);
            }else if(processBookedStation == 4 && notBookedStatus == 1){
                status.setStatus(StationStatus.Status.COMPLETED);
            }else {
                switch (notBookedStatus){
                    case 0 -> status.setStatus(StationStatus.Status.NOT_OWN_PALLET);
                    case 1 -> status.setStatus(StationStatus.Status.OWN_PALLET);
                    case 6 -> status.setStatus(StationStatus.Status.DISABLE);
                }
            }
        }
        callerProcess(iCallerConn, callerStatus, callerStationStatus);

    }

    private boolean iCon = true;
    public Optional<String[]> crawlAGVStatus() {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(agvUrl + "/cars"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String webpageContent = response.body();
            String[] data = Arrays.stream(webpageContent.split("<br>"))
                    .map(String::trim)
                    .toArray(String[]::new);
            iCon=true;
            return Optional.of(data);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("AGV 控制系統未連線");
            CountUtilizationRate.isPoweredOn = new boolean[agvIdDao.queryAGVList().size()];
            CountUtilizationRate.isWorking = new boolean[agvIdDao.queryAGVList().size()];
            if(iCon){
                notificationDao.insertMessage(NotificationDao.Title.AGV_SYSTEM, NotificationDao.Status.OFFLINE);
                iCon=false;
            }
        }

        return Optional.empty();
    }


    public Optional<String[]> crawlCallerStatus() {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://192.168.0.100:20100/callers"))
                .uri(URI.create(agvUrl + "/callers"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String webpageContent = response.body();
            String[] data = Arrays.stream(webpageContent.split("<br>"))
                    .map(String::trim)
                    .toArray(String[]::new);

            return Optional.of(data);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private final HttpClient httpClientForSendCallerStatus = HttpClient.newHttpClient();
    private void callStationCaller(int id, int value) {
        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://192.168.0.100:20100/caller=" + id + "&" + value + "&output"))
                .uri(URI.create(agvUrl + "/caller=" + id + "&" + value + "&output"))
                .GET()
                .build();
        try {
            httpClientForSendCallerStatus.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void callNotificationCaller(int id, int value) {
        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://192.168.0.100:20100/caller=" + id + "&" + value + "&toggle"))
                .uri(URI.create(agvUrl + "/caller=" + id + "&" + value + "&output"))
                .GET()
                .build();
        try {
            httpClientForSendCallerStatus.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void callerProcess(boolean[] iCallerConn, boolean[] callerStatus, boolean[] callerStationStatus){
        for (int i = 5; i < 10; i++) {
            if(iCallerConn[i-5]) {
                if (callerStationStatus[i]) {
                    doSendCaller(i - 4, 2);
                } else {
                    doSendCaller(i - 4, 0);
                }
            }
        }
        StringBuilder stringBuilder;
        if(iCallerConn[5]) {
            stringBuilder = new StringBuilder();
            for (int i = 8; i > 4; i--) {
                stringBuilder.append(callerStatus[i] ? '1' : '0');
            }
            int command1 = Integer.parseInt(stringBuilder.toString(), 2);
            doSendCaller(6, command1);
        }
        if(iCallerConn[6]) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(callerStatus[9] ? '1' : '0');
            stringBuilder.append('0');
            stringBuilder.append('0');
            stringBuilder.append('0');
            int command2 = Integer.parseInt(stringBuilder.toString(), 2);
            doSendCaller(7, command2);
        }
        for (int i = 8; i < 13; i++) {
            if(iCallerConn[i-1]) {
                if (callerStationStatus[i+2]) {
                    doSendCaller(i, 2);
                } else {
                    doSendCaller(i, 0);
                }
            }
        }

        if(iCallerConn[12]) {
            stringBuilder = new StringBuilder();
            for (int i = 13; i > 9; i--) {
                stringBuilder.append(callerStatus[i] ? '1' : '0');
            }
            int command3 = Integer.parseInt(stringBuilder.toString(), 2);
            doSendCaller(13, command3);
        }
        if(iCallerConn[13]) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(callerStatus[14] ? '1' : '0');
            stringBuilder.append('0');
            stringBuilder.append('0');
            stringBuilder.append('0');
            int command4 = Integer.parseInt(stringBuilder.toString(), 2);
//        System.out.println("SendCaller Active Threads: " + sendCallerExecutor.getActiveCount());
            doSendCaller(14, command4);
        }
//        System.out.println("SendCaller Pool Size: " + sendCallerExecutor.getPoolSize());

    }


    private final int[] lastCaller = new int[14];
    @Async
    private void doSendCaller(int id, int value){
        if(lastCaller[id-1] != value) {
            System.out.print("Id: " + id);
            System.out.println("  Value: " + value);
            if(id != 6 && id != 7 && id != 13 && id != 14 && value == 2){
                sendCallerExecutor.execute(() ->
                        callNotificationCaller(id, value)
                );
            } else {
                sendCallerExecutor.execute(() ->
                        callStationCaller(id, value)
                );
            }
            lastCaller[id-1] = value;
        }
    }

    public boolean[] parseAGVStatus(int statusValue) {
        if(statusValue < 0) return null;
        boolean[] statusArray = new boolean[8];
        // 從右到左解析各個位元狀態
        for (int i = 0; i < statusArray.length ; i++) {
            // 檢查第i位是否為1，若是則代表狀態為真
            statusArray[i] = (statusValue & 1) == 1;
            // 右移一位，繼續解析下一位元
            statusValue >>= 1;
        }
        return statusArray;
    }

    public boolean[] parseCallerStatus(long statusValue) {
        if(statusValue < 0) return null;
        boolean[] statusArray = new boolean[32];
        // 從右到左解析各個位元狀態
        for (int i = 0; i < statusArray.length ; i++) {
            // 檢查第i位是否為1，若是則代表狀態為真
            statusArray[i] = (statusValue & 1) == 1;
            // 右移一位，繼續解析下一位元
            statusValue >>= 1;
        }
        return statusArray;
    }

    public static Map<Integer, Integer> getCallerStationStatusMap(){
        return new HashMap<>(callerStationStatusMap);
    }


}
