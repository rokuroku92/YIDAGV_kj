package com.yid.agv.repository;


import com.yid.agv.model.Notification;

import java.util.List;

public interface NotificationDao {

    enum Title{
        AGV_SYSTEM(1), AMR_1(2), AMR_2(3), AMR_3(4),
        CALLER_1(5), CALLER_2(6);
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
        FAILED_SEND_TASK(18), FAILED_SEND_TASK_THREE_TIMES(19), BATTERY_TOO_LOW(20),
        CALLER_LONG_OFFLINE(21);
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
