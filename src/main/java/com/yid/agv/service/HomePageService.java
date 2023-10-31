package com.yid.agv.service;

import com.yid.agv.backend.agv.AGVManager;
import com.yid.agv.backend.agv.AgvStatus;
import com.yid.agv.backend.station.StationManager;
import com.yid.agv.backend.station.StationStatus;
import com.yid.agv.model.*;
import com.yid.agv.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class HomePageService {
    
    @Autowired
    private AGVIdDao agvIdDao;
    
    @Autowired
    private NotificationDao notificationDao;
    
    @Autowired
    private StationDao stationDao;

    @Autowired
    private MessageDataDao messageDataDao;
    
    @Autowired
    private ModeDao modeDao;

    @Autowired
    private AGVManager agvManager;

    @Autowired
    private StationManager stationManager;

    @PostConstruct
    public void __init() {
        Arrays.fill(equipmentIAlarm, 0);
    }

    private int iAlarm;
    private final int[] equipmentIAlarm = new int[14];

    public int[] getEquipmentIAlarm() {
        return equipmentIAlarm;
    }

    public void setEquipmentIAlarm(int i, int value) {
        this.equipmentIAlarm[i] = value;
    }

    public int getIAlarm() {
        return iAlarm;
    }

    public void setIAlarm(int iAlarm) {
        this.iAlarm = iAlarm;
    }

    public AgvStatus[] getAgvStatus(){
        return agvManager.getAgvStatusCopyArray();
    }

    public StationStatus[] getStationStatus(){
        return stationManager.getStationStatusCopyArray();
    }

    public List<AGVId> queryAGVList(){
        return agvIdDao.queryAGVList();
    }

    public List<Notification> queryTodayNotifications(){
        return notificationDao.queryTodayNotifications();
    }

    public List<Notification> queryNotificationsByDate(String date){
        return notificationDao.queryNotificationsByDate(date);
    }

    public List<Notification> queryAllNotifications(){
        return notificationDao.queryAllNotifications();
    }

    public List<Notification> queryNotifications(){
        return notificationDao.queryNotifications();
    }

    public List<Station> queryStations(){
        return stationDao.queryStations();
    }
    public List<NotificationStation> queryNotificationStations(){
        return stationDao.queryNotificationStations();
    }
    public List<MessageData> queryMessageData(){return messageDataDao.queryMessageData();}

    public List<Mode> queryModes(){
        return modeDao.queryModes();
    }
    
    
}
