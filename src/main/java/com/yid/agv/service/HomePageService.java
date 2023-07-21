package com.yid.agv.service;

import com.yid.agv.model.*;
import com.yid.agv.repository.AGVIdDao;
import com.yid.agv.repository.ModeDao;
import com.yid.agv.repository.NotificationDao;
import com.yid.agv.repository.StationDao;
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
    private StationDao StationDao;
    
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

    public List<Station> queryStations(){
        return StationDao.queryStations();
    }


    public List<Mode> queryModes(){
        return modeDao.queryModes();
    }
    
    
}
