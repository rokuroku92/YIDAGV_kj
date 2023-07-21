package com.yid.agv.backend;

import com.yid.agv.model.AgvStatus;
import com.yid.agv.model.Station;
import com.yid.agv.model.StationStatus;
import com.yid.agv.repository.AGVIdDao;
import com.yid.agv.repository.StationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class InstantStatus {

    @Autowired
    private AGVIdDao agvIdDao;

    @Autowired
    private StationDao stationDao;
    public static AgvStatus[] agvStatuses;
    public static StationStatus[] stationStatuses; // 兩大站、五車位


    private int tempId=0;
    @Scheduled(fixedRate = 1000) // 每秒執行
    public void updateAgvStatuses() {
        // 抓取AGV狀態，並更新到agvStatuses
//        for(int i=0;i<agvIdDao.queryAGVList().size();i++){
//            agvStatuses[i].setStatus(0);
//            agvStatuses[i].setTask("#202307180001"); // 要改在ProcessTasks更新
//            agvStatuses[i].setPlace("1011");
//            agvStatuses[i].setBattery(96);
//            agvStatuses[i].setSignal(96);
//        }
        if(agvStatuses == null){
            agvStatuses = new AgvStatus[agvIdDao.queryAGVList().size()];
            for(int i=0;i<agvStatuses.length;i++){
                agvStatuses[i] = new AgvStatus();
            }
        }
        for(int i=0;i<agvStatuses.length;i++){
//            agvs[i].setStatus((int) (Math.random() * 10) % 3);
            agvStatuses[i].setStatus(2);
            agvStatuses[i].setPlace("1011");
            agvStatuses[i].setTask(ProcessTasks.getNowTaskNumber() == null ? "":ProcessTasks.getNowTaskNumber());
            agvStatuses[i].setBattery(100-tempId%100);
            agvStatuses[i].setSignal(100-tempId%100);
            tempId+=2;
        }
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
        for(int i=0;i<15;i++) {
            notBookedStationStatuses[i] = new StationStatus();
            if(i<5)
                notBookedStationStatuses[i].setStatus(6);
            else
                notBookedStationStatuses[i].setStatus(new Random().nextInt(2));
            if(ProcessTasks.bookedStation[i] == 1 && notBookedStationStatuses[i].getStatus() == 0){
                // 錯誤，任務起始站棧板離開。
                System.out.println("錯誤，任務起始站棧板離開。");
                stationStatuses[i].setStatus(3);
                // callStationAlarm
            }else if(ProcessTasks.bookedStation[i] == 1 && notBookedStationStatuses[i].getStatus() == 1){
                stationStatuses[i].setStatus(2);
            }else if(ProcessTasks.bookedStation[i] == 2 && notBookedStationStatuses[i].getStatus() == 1){
                // 錯誤，任務終點站上有其他棧板。
                System.out.println("錯誤，任務終點站上有其他棧板。");
                stationStatuses[i].setStatus(3);
                // callStationAlarm
            }else if(ProcessTasks.bookedStation[i] == 2 && notBookedStationStatuses[i].getStatus() == 0){
                stationStatuses[i].setStatus(2);
            }else if(ProcessTasks.bookedStation[i] == 4 && notBookedStationStatuses[i].getStatus() == 0){
                ProcessTasks.bookedStation[i] = 0;
            }else if(ProcessTasks.bookedStation[i] == 4 && notBookedStationStatuses[i].getStatus() == 1){
                stationStatuses[i].setStatus(4);
            }else { // 完成狀態可以實作在這邊
                stationStatuses[i].setStatus(notBookedStationStatuses[i].getStatus());
            }
        }
//        ProcessTasks.getTaskQueue().forEach(task -> {
//            int startStation = task.getStartStationId();
//            int bookedStation = task.getTerminalStationId();
//            if (notBookedStationStatuses[startStation-1].getStatus() == 0){
//                // 錯誤，任務起始站棧板離開。
//                System.out.println("錯誤，任務起始站棧板離開。");
//                notBookedStationStatuses[startStation-1].setStatus(3);
//                // callStationAlarm
//            }else{
//                notBookedStationStatuses[startStation-1].setStatus(2);
//            }
//            if (notBookedStationStatuses[bookedStation-1].getStatus() == 1){
//                // 錯誤，任務終點站上有其他棧板。
//                System.out.println("錯誤，任務終點站上有其他棧板。");
//                notBookedStationStatuses[startStation-1].setStatus(3);
//                // callStationAlarm
//            }else{
//                notBookedStationStatuses[bookedStation-1].setStatus(2);
//            }
//        });

        // 完成狀態未實作
//        stationStatuses = Arrays.copyOf(notBookedStationStatuses, notBookedStationStatuses.length);
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

    public String crawlStatus(){
        // 抓取AGV狀態網頁，回傳
        return "";
    }
}
