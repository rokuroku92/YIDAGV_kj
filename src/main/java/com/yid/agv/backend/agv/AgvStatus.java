package com.yid.agv.backend.agv;

public class AgvStatus {

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

    private int status;
    private String task;
    private String place;
    private int battery;
    private int signal;

    public int getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status.getValue();
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getSignal() {
        return signal;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }
}
