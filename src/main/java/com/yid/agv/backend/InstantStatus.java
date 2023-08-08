package com.yid.agv.backend;

import com.yid.agv.backend.datastorage.AGVManager;
import com.yid.agv.backend.datastorage.StationManager;
import com.yid.agv.backend.datastorage.TaskQueue;
import com.yid.agv.model.AgvStatus;
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
import java.util.*;

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

    private int reDispatch = 0;
    public static boolean iTask = false;
    public static boolean iStandbyTask = false;
    private String[] lastAgvStatusData;
    @Scheduled(fixedRate = 1000) // 每秒執行
    public void updateAgvStatuses() {
        lastAgvStatusData = Objects.requireNonNullElseGet(lastAgvStatusData, () -> new String[agvIdDao.queryAGVList().size()]);
        // 抓取AGV狀態，並更新到agvStatuses
        String[] agvStatusData = testFakeData.crawlAGVStatus().orElse(new String[0]); // TODO: Fake data
//        String[] agvStatusData = crawlAGVStatus().orElse(new String[0]);
        if (agvStatusData.length != 0) {
            for (int i = 0; i < agvManager.getAgvLength(); i++) {
                AgvStatus agvStatus = agvManager.getAgvStatus(i+1);
                String[] data = agvStatusData[i].split(",");
                int j = i + 2;
                // data[0] 車號
                if (!Objects.equals(data[0], "-1")) {
                    CountUtilizationRate.isPoweredOn[i] = true;
                    // data[1] 位置
                    agvStatus.setPlace(data[1]);
                    // data[2] 訊號
                    agvStatus.setSignal(Integer.parseInt(data[2]));
                    // data[3] 電量
                    agvStatus.setBattery(Integer.parseInt(data[3]));
                    // data[4] 任務狀態
                    boolean[] taskStatus = parseAGVStatus(Integer.parseInt(data[4]));

                    if (taskStatus[7]) {
                        agvStatus.setTask("任務執行失敗");
                        notificationDao.insertMessage(1, 16);
                        if (!iStandbyTask){
                            if(reDispatch < 3) {
                                agvStatus.setTask("重新執行任務");
                                ProcessTasks.dispatchTaskToAGV(notificationDao, taskQueue.getTaskByTaskNumber(taskQueue.getNowTaskNumber()), agvStatus.getPlace());
                                reDispatch++;
                            } else if (reDispatch == 3) {
                                ProcessTasks.failedTask(taskQueue, notificationDao, taskDao);
                                iTask = false;
                            }
                        } else {
                            ProcessTasks.failedGoStandbyTask(taskDao);
                            iStandbyTask = false;
                            iTask = false;
                        }
                    } else {
                        if (taskStatus[0]) {
                            if(!iStandbyTask) {
                                agvStatus.setTask(Optional.ofNullable(taskQueue.getNowTaskNumber()).orElse(""));
                                CountUtilizationRate.isWorking[i] = true;
                            }
                            iTask = true;
                        } else {
                            agvStatus.setTask("");
                            CountUtilizationRate.isWorking[i] = false;
                            if(iTask){
                                // 任務完成(task完成)
                                if(!iStandbyTask) {
                                    ProcessTasks.completedTask(taskQueue, analysisDao, taskDao);
                                    // TODO: 這邊可以判斷是否還有任務，直接派回待命點，執行效率會更好，但寫在ProcessTasks邏輯較正確，目前實作在ProcessTasks。
                                    iTask = false;
                                } else {
                                    iTask = false;
                                    iStandbyTask = false;
                                    ProcessTasks.completedGoStandbyTask(taskDao);
                                }
                                reDispatch = 0;
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
                                        agvStatus.setStatus(3);
                                        notificationDao.insertMessage(j, 4);
                                    }
                                    case 1 -> {
                                        // AGV 手動模式
                                        agvStatus.setStatus(2);
                                        notificationDao.insertMessage(j, 3);
                                    }
                                    case 2 -> {
                                        // AGV 連線中(自動上位模式)
                                        agvStatus.setStatus(1);
                                        notificationDao.insertMessage(j, 2);
                                    }
                                    default -> {
                                        // 系統異常資料
                                        agvStatus.setStatus(0);
                                        System.out.println("異常agv狀態資料");
                                        notificationDao.insertMessage(1, 15);
                                    }
                                }
                            }
                            case 1 -> {
                                // AGV 緊急停止
                                agvStatus.setStatus(4);
                                notificationDao.insertMessage(j, 5);
                            }
                            case 2 -> {
                                // AGV 出軌
                                agvStatus.setStatus(5);
                                notificationDao.insertMessage(j, 6);
                            }
                            case 3 -> {
                                // AGV 發生碰撞
                                agvStatus.setStatus(6);
                                notificationDao.insertMessage(j, 7);
                            }
                            case 4 -> {
                                // AGV 前有障礙
                                agvStatus.setStatus(7);
                                notificationDao.insertMessage(j, 8);
                            }
                            case 5 -> {
                                // AGV 轉向角度過大
                                agvStatus.setStatus(8);
                                notificationDao.insertMessage(j, 9);
                            }
                            case 6 -> {
                                // AGV 卡號錯誤
                                agvStatus.setStatus(9);
                                notificationDao.insertMessage(j, 10);
                            }
                            case 7 -> {
                                // AGV 未知卡號
                                agvStatus.setStatus(10);
                                notificationDao.insertMessage(j, 11);
                            }
                            case 8 -> {
                                // AGV 異常排除
                                agvStatus.setStatus(11);
                                notificationDao.insertMessage(j, 12);
                            }
                            case 9 -> {
                                // AGV 感知器偵測異常
                                agvStatus.setStatus(12);
                                notificationDao.insertMessage(j, 13);
                            }
                            case 10 -> {
                                // AGV 充電異常
                                agvStatus.setStatus(13);
                                notificationDao.insertMessage(j, 14);
                            }
                            default -> {
                                // 系統異常資料
                                agvStatus.setStatus(0);
                                System.out.println("異常agv狀態資料");
                                notificationDao.insertMessage(1, 15);
                            }
                        }
                        lastAgvStatusData[i] = data[5];
                    }
                } else {
                    agvStatus.setStatus(0);
                    notificationDao.insertMessage(j, 1);
                    CountUtilizationRate.isPoweredOn[i] = false;
                    CountUtilizationRate.isWorking[i] = false;
                }
            }
        }
    }

    @Scheduled(fixedRate = 1000) // 每秒執行
    public void updateStationStatusesData() {
        // 抓取Station狀態與Booking，並更新到stationStatuses
        StationStatus[] notBookedStationStatuses = new StationStatus[15];

        String[] stationValue= testFakeData.crawlStationStatus().orElse(new String[0]);  // TODO: Fake data
//        String[] stationValue= crawlStationStatus().orElse(new String[0]);

        for(int i=0;i<15 && stationValue.length >= 10;i++) {
            notBookedStationStatuses[i] = new StationStatus();
            if(i<5) {
                notBookedStationStatuses[i].setStatus(6);
            } else {
                // TODO: 這邊假設有無箱是在boolean[0]
                boolean[] dataValue = parseStationStatus(Long.parseLong(stationValue[i-5]));
                if (dataValue[0]) {
                    notBookedStationStatuses[i].setStatus(1);
                } else {
                    notBookedStationStatuses[i].setStatus(0);
                }

            }

            int processBookedStation = taskQueue.getBookedStationStatusByStation(i+1);
            int notBookedStatus = notBookedStationStatuses[i].getStatus();
            StationStatus status = stationManager.getStationStatus(i+1);
            if(processBookedStation == 1 && notBookedStatus == 0){
                // 錯誤，任務起始站棧板離開。
                System.out.println("錯誤，任務起始站棧板離開。");
                status.setStatus(3);
                // TODO: callStationAlarm
            }else if(processBookedStation == 1 && notBookedStatus == 1){
                status.setStatus(2);
            }else if(processBookedStation == 2 && notBookedStatus == 1){
                // 錯誤，任務終點站上有其他棧板。
                System.out.println("錯誤，任務終點站上有其他棧板。");
                status.setStatus(3);
                // TODO: callStationAlarm
            }else if(processBookedStation == 2 && notBookedStatus == 0){
                status.setStatus(2);
            }else if(processBookedStation == 4 && notBookedStatus == 0){
                taskQueue.setBookedStation(i+1, 0);
            }else if(processBookedStation == 4 && notBookedStatus == 1){
                status.setStatus(4);
            }else {
                status.setStatus(notBookedStatus);
            }
        }
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




}
