package com.yid.agv.backend;

import com.yid.agv.model.AgvStatus;
import com.yid.agv.model.QTask;
import com.yid.agv.model.StationStatus;
import com.yid.agv.repository.*;
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

@Component
public class InstantStatus {

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
    public static AgvStatus[] agvStatuses;
    public static StationStatus[] stationStatuses; // 兩大站、五車位


//    private int tempId=0;

    private static String[] agvStatusData = new String[0];
    private String[] lastAgvStatusData;
    private int redispatch = 0;
    public static boolean iTask = false;
    public static boolean iStandby = false;
    private QTask toStandbyTask;
    @Scheduled(fixedRate = 1000) // 每秒執行
    public void updateAgvStatuses() {
        //若為 null 則創建陣列與實例化
        agvStatuses = Objects.requireNonNullElseGet(agvStatuses, () -> agvIdDao.queryAGVList().stream()
                .map(janet -> new AgvStatus())
                .toArray(AgvStatus[]::new));
        lastAgvStatusData = Objects.requireNonNullElseGet(lastAgvStatusData, () -> new String[agvIdDao.queryAGVList().size()]);
        // 抓取AGV狀態，並更新到agvStatuses
        agvStatusData = crawlAGVStatus().orElse(new String[0]);
        if (agvStatusData.length != 0) {
            for (int i = 0; i < agvStatuses.length; i++) {
                String[] data = agvStatusData[i].split(",");
                int j = i + 2;
                // data[0] 車號
                if (!Objects.equals(data[0], "-1")) {
                    CountUtilizationRate.isPoweredOn[i] = true;
                    // data[1] 位置
                    agvStatuses[i].setPlace(data[1]);
                    // data[2] 訊號
                    agvStatuses[i].setSignal(Integer.parseInt(data[2]));
                    // data[3] 電量
                    agvStatuses[i].setBattery(Integer.parseInt(data[3]));
                    // data[4] 任務狀態？？？
                    boolean[] taskStatus = parseAGVStatus(Integer.parseInt(data[4]));

                    if (taskStatus[7]) {
                        agvStatuses[i].setTask("任務執行失敗");
                        notificationDao.insertMessage(1, 16);
                        if (!iStandby){
                            if(redispatch < 3) {
                                agvStatuses[i].setTask("重新執行任務");
                                ProcessTasks.dispatchTaskToAGV(ProcessTasks.getTaskByTaskNumber(ProcessTasks.getNowTaskNumber()));
                                redispatch++;
                            } else if (redispatch == 3) {
                                System.out.println("任務執行三次皆失敗，已取消任務");
                                notificationDao.insertMessage(1, 17);
                                taskDao.cancelTask(ProcessTasks.getNowTaskNumber());
                                ProcessTasks.removeTaskByTaskNumber(ProcessTasks.getNowTaskNumber());
                                ProcessTasks.setNowTaskNumber(null);
                            }
                        } else {
                            if(redispatch < 3) {
                                agvStatuses[i].setTask("重新執行任務");
                                ProcessTasks.dispatchTaskToAGV(toStandbyTask);
                                redispatch++;
                            } else if (redispatch == 3) {
                                System.out.println("任務執行三次皆失敗，已取消任務");
                                notificationDao.insertMessage(1, 17);
                                taskDao.cancelTask(toStandbyTask.getTaskNumber());
                                toStandbyTask = null;
                            }
                        }
                    } else {
                        if (taskStatus[0]) {
                            agvStatuses[i].setTask(Optional.ofNullable(ProcessTasks.getNowTaskNumber()).orElse(""));
                            CountUtilizationRate.isWorking[i] = true;
                            iTask = true;
                        } else {
                            agvStatuses[i].setTask("");
                            CountUtilizationRate.isWorking[i] = false;
                            if(iTask){
                                // 任務完成(task完成)
                                if(!iStandby) {
                                    QTask cTask = ProcessTasks.getTaskByTaskNumber(ProcessTasks.getNowTaskNumber());
                                    ProcessTasks.bookedStation[Objects.requireNonNull(cTask, "起始站為空").getStartStationId() - 1] = 0;
                                    ProcessTasks.bookedStation[cTask.getTerminalStationId() - 1] = 4;
                                    int analysisId = analysisDao.getTodayAnalysisId().get(cTask.getAgvId() - 1).getAnalysisId();
                                    analysisDao.updateTask(analysisDao.queryAnalysisesByAnalysisId(analysisId).getTask() + 1, analysisId);
                                    System.out.println("Completed task number "+cTask.getTaskNumber()+".");
                                    taskDao.updateTaskStatus(cTask.getTaskNumber(), 100);
                                    ProcessTasks.pollTaskByTaskNumber(ProcessTasks.getNowTaskNumber());
                                    ProcessTasks.setNowTaskNumber(null);

                                    // TODO: 判斷是否佇列還有任務，若無則回到待命點。
                                    if(ProcessTasks.getTaskQueue().isEmpty()){
                                        iStandby = true;
                                        int place = Integer.parseInt(agvStatuses[i].getPlace());
                                        int standbytag;
                                        if (place >= 1001 && place <= 1050){
                                            standbytag = 1055;
                                        } else if (place > 1050 && place <= 1100) {
                                            standbytag = 1065;
                                        } else {
                                            standbytag = 1075;
                                        }
                                        LocalDateTime now = LocalDateTime.now();
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                                        String formattedDateTime = now.format(formatter);

                                        toStandbyTask = new QTask();
                                        toStandbyTask.setAgvId(i+1);
                                        toStandbyTask.setModeId(1);
                                        toStandbyTask.setStatus(0);
                                        toStandbyTask.setTaskNumber("#Standby"+formattedDateTime);
                                        toStandbyTask.setStartStationId(place);
                                        toStandbyTask.setTerminalStationId(standbytag);
                                        toStandbyTask.setNotificationStationId(0);
                                        ProcessTasks.dispatchTaskToAGV(toStandbyTask);
                                    }else {
                                        iTask = false;
                                    }
                                }else {
                                    int analysisId = analysisDao.getTodayAnalysisId().get(toStandbyTask.getAgvId() - 1).getAnalysisId();
                                    analysisDao.updateTask(analysisDao.queryAnalysisesByAnalysisId(analysisId).getTask() + 1, analysisId);
                                    System.out.println("Completed task number "+toStandbyTask.getTaskNumber()+".");
                                    taskDao.updateTaskStatus(toStandbyTask.getTaskNumber(), 100);
                                    toStandbyTask = null;
                                    iTask = false;
                                }
                                redispatch = 0;
                                ProcessTasks.redispatch = 0;
                            }
                        }
                    }
                    // data[5] agv狀態
                    if (!Objects.equals(lastAgvStatusData[i], data[5])) {
                        switch (Integer.parseInt(data[5]) / 10) {
                            case 0 -> {
                                switch (Integer.parseInt(data[5]) % 10) {
                                    case 0 -> {
                                        // AGV 重新啟動
                                        agvStatuses[i].setStatus(3);
                                        notificationDao.insertMessage(j, 4);
                                    }
                                    case 1 -> {
                                        // AGV 手動模式
                                        agvStatuses[i].setStatus(2);
                                        notificationDao.insertMessage(j, 3);
                                    }
                                    case 2 -> {
                                        // AGV 連線中(自動上位模式)
                                        agvStatuses[i].setStatus(1);
                                        notificationDao.insertMessage(j, 2);
                                    }
                                    default -> {
                                        // 系統異常資料
                                        agvStatuses[i].setStatus(0);
                                        System.out.println("異常agv狀態資料");
                                        notificationDao.insertMessage(1, 15);
                                    }
                                }
                            }
                            case 1 -> {
                                // AGV 緊急停止
                                agvStatuses[i].setStatus(4);
                                notificationDao.insertMessage(j, 5);
                            }
                            case 2 -> {
                                // AGV 出軌
                                agvStatuses[i].setStatus(5);
                                notificationDao.insertMessage(j, 6);
                            }
                            case 3 -> {
                                // AGV 發生碰撞
                                agvStatuses[i].setStatus(6);
                                notificationDao.insertMessage(j, 7);
                            }
                            case 4 -> {
                                // AGV 前有障礙
                                agvStatuses[i].setStatus(7);
                                notificationDao.insertMessage(j, 8);
                            }
                            case 5 -> {
                                // AGV 轉向角度過大
                                agvStatuses[i].setStatus(8);
                                notificationDao.insertMessage(j, 9);
                            }
                            case 6 -> {
                                // AGV 卡號錯誤
                                agvStatuses[i].setStatus(9);
                                notificationDao.insertMessage(j, 10);
                            }
                            case 7 -> {
                                // AGV 未知卡號
                                agvStatuses[i].setStatus(10);
                                notificationDao.insertMessage(j, 11);
                            }
                            case 8 -> {
                                // AGV 異常排除
                                agvStatuses[i].setStatus(11);
                                notificationDao.insertMessage(j, 12);
                            }
                            case 9 -> {
                                // AGV 感知器偵測異常
                                agvStatuses[i].setStatus(12);
                                notificationDao.insertMessage(j, 13);
                            }
                            case 10 -> {
                                // AGV 充電異常
                                agvStatuses[i].setStatus(13);
                                notificationDao.insertMessage(j, 14);
                            }
                            default -> {
                                // 系統異常資料
                                agvStatuses[i].setStatus(0);
                                System.out.println("異常agv狀態資料");
                                notificationDao.insertMessage(1, 15);
                            }
                        }
                        lastAgvStatusData[i] = data[5];
                    }
                } else {
                    agvStatuses[i].setStatus(0);
                    notificationDao.insertMessage(j, 1);
                    CountUtilizationRate.isPoweredOn[i] = false;
                    CountUtilizationRate.isWorking[i] = false;
                }
            }
        }else{
            System.out.println("AGV 控制系統未連線");
            CountUtilizationRate.isPoweredOn = new boolean[agvIdDao.queryAGVList().size()];
            CountUtilizationRate.isWorking = new boolean[agvIdDao.queryAGVList().size()];
        }

        // 假資料
//        for(int i=0;i<agvStatuses.length;i++){
//            agvStatuses[i].setStatus(2);
//            agvStatuses[i].setPlace("1011");
//            agvStatuses[i].setTask(Optional.ofNullable(ProcessTasks.getNowTaskNumber()).orElse(""));
//            agvStatuses[i].setBattery(100-tempId%100);
//            agvStatuses[i].setSignal(100-tempId%100);
//            tempId+=2;
//        }

    }

    @Scheduled(fixedRate = 1000) // 每秒執行
    public void updateStationStatusesData() {
        // 抓取Station狀態與Booking，並更新到stationStatuses
        StationStatus[] notBookedStationStatuses = new StationStatus[15]; // 兩大站、五車位
        if(stationStatuses == null){
            stationStatuses = new StationStatus[15];
            for(int i=0;i<stationStatuses.length;i++){
                stationStatuses[i] = new StationStatus();
            }
        }
        // TODO: getStationStatus && setStatus() DONE
//        String[] stationValue= crawlStationStatus().orElse(new String[0]);
        for(int i=0;i<15;i++) {
            notBookedStationStatuses[i] = new StationStatus();
//            boolean[] dataValue = parseStationStatus(Long.parseLong(stationValue[i]));
//            if(i<5) {
//                notBookedStationStatuses[i].setStatus(6);
//            } else {
//                // TODO: 這邊假設有無箱是在boolean[0]
//                if (dataValue[0]) {
//                    notBookedStationStatuses[i].setStatus(1);
//                } else {
//                    notBookedStationStatuses[i].setStatus(0);
//                }
//
//            }

            // 假資料
            if(i<5) {
                notBookedStationStatuses[i].setStatus(6);
            } else {
                notBookedStationStatuses[i].setStatus(new Random().nextInt(2));
            }


            int processBookedStation = ProcessTasks.bookedStation[i];
            int notBookedStatus = notBookedStationStatuses[i].getStatus();
            if(processBookedStation == 1 && notBookedStatus == 0){
                // 錯誤，任務起始站棧板離開。
                System.out.println("錯誤，任務起始站棧板離開。");
                stationStatuses[i].setStatus(3);
                // TODO: callStationAlarm
            }else if(processBookedStation == 1 && notBookedStatus == 1){
                stationStatuses[i].setStatus(2);
            }else if(processBookedStation == 2 && notBookedStatus == 1){
                // 錯誤，任務終點站上有其他棧板。
                System.out.println("錯誤，任務終點站上有其他棧板。");
                stationStatuses[i].setStatus(3);
                // TODO: callStationAlarm
            }else if(processBookedStation == 2 && notBookedStatus == 0){
                stationStatuses[i].setStatus(2);
            }else if(processBookedStation == 4 && notBookedStatus == 0){
                ProcessTasks.bookedStation[i] = 0;
            }else if(processBookedStation == 4 && notBookedStatus == 1){
                stationStatuses[i].setStatus(4);

            }else { // 完成狀態可以實作在這邊
                stationStatuses[i].setStatus(notBookedStatus);
            }
        }
    }

    public static synchronized Integer getTerminalByNotification(String notification){
        if(ProcessTasks.getTaskQueue().size() >= 5) return null;
        int s,x;
        int noti = Integer.parseInt(notification);
        if (noti > 0 && noti <= 5){
            s=0;x=5;
        }else if(noti > 5 && noti <= 10){
            s=5;x=10;
        }else if(noti > 10 && noti <= 15){
            s=10;x=15;
        }else {
            s = 0;x = 1;
        }
        for(int i=s;i<x;i++){
            if(stationStatuses[i].getStatus() == 0 || stationStatuses[i].getStatus() == 1)
                return i+1;
        }
        return null;
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
                notificationDao.insertMessage(1, 1);
                iCon=false;
            }
        }

        return Optional.empty();
    }

    public Optional<String[]> crawlStationStatus() {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
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

    public boolean callStationCaller(int id, int mode) {
        String modeStr;
        switch (mode){
            case 1 -> modeStr = "set";
            case 2 -> modeStr = "clr";
            default -> {
                return false;
            }
        }
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(agvUrl + "/caller=" + id + "&7&" + modeStr)) // TODO: 假設&7&中間7為{mask}之值為二進位0111代表功能號碼
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String webpageContent = response.body().trim();
            return webpageContent.equals("OK");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean[] parseAGVStatus(int statusValue) {
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

    public boolean[] parseStationStatus(long statusValue) {
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

    public static String[] getAgvStatusData(){
        return agvStatusData;
    }

}
