package com.yid.agv.service;

import com.yid.agv.model.*;
import com.yid.agv.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
