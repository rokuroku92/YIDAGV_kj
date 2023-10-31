package com.yid.agv.backend.agv;

import com.yid.agv.backend.task.QTask;

public class Agv {
    public enum Status{
        OFFLINE(1), ONLINE(2), MANUAL(3), REBOOT(4),
        STOP(5), DERAIL(6), COLLIDE(7), OBSTACLE(8),
        EXCESSIVE_TURN_ANGLE(9), WRONG_TAG_NUMBER(10), UNKNOWN_TAG_NUMBER(11),
        EXCEPTION_EXCLUSION(12), SENSOR_ERROR(13), CHARGE_ERROR(14), ERROR_AGV_DATA(15);
        private final int value;
        Status(int value) {
            this.value = value;
        }

        public int getValue(){
            return value;
        }

    }

    public enum TaskStatus{
        NO_TASK, PRE_START_STATION, PRE_TERMINAL_STATION, COMPLETED
    }

    private int status;
    private String taskNumber;
    private String place;
    private int battery;
    private int signal;

    private QTask task;


}
