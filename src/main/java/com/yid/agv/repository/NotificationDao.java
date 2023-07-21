package com.yid.agv.repository;


import com.yid.agv.model.Notification;

import java.util.List;

public interface NotificationDao {
        
    List<Notification> queryTodayNotifications();
    
    List<Notification> queryNotificationsByDate(String date);
    
    List<Notification> queryAllNotifications();
    
}
