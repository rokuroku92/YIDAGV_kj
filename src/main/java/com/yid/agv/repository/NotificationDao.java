package com.yid.agv.repository;


import com.yid.agv.model.Notification;

import java.util.List;

public interface NotificationDao {

    enum Title{
        AGV_SYSTEM(1), AGV_1(2), AGV_2(3);
        private final int value;
        Title(int value) {
            this.value = value;
        }
        public int getValue(){
            return value;
        }
    }
    enum Status{
        OFFLINE(1), ONLINE(2), MANUAL(3), REBOOT(4),
        STOP(5), DERAIL(6), COLLIDE(7), OBSTACLE(8),
        EXCESSIVE_TURN_ANGLE(9), WRONG_TAG_NUMBER(10), UNKNOWN_TAG_NUMBER(11),
        EXCEPTION_EXCLUSION(12), SENSOR_ERROR(13), CHARGE_ERROR(14), ERROR_AGV_DATA(15),
        FAILED_EXECUTION_TASK(16), FAILED_EXECUTION_TASK_THREE_TIMES(17),
        FAILED_SEND_TASK(18), FAILED_SEND_TASK_THREE_TIMES(19);
        private final int value;
        Status(int value) {
            this.value = value;
        }

        public int getValue(){
            return value;
        }

    }

    List<Notification> queryTodayNotifications();
    
    List<Notification> queryNotificationsByDate(String date);
    
    List<Notification> queryAllNotifications();
    List<Notification> queryNotifications();

    void insertMessage(Title titleId, Status messageId);
    
}
