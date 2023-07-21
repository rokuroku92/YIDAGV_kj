package com.yid.agv.model;

public class QTask {
    private String taskNumber;
    private int agvId;
    private Integer startStationId;
    private Integer terminalStationId;
    private Integer notificationStationId;
    private int modeId;
    private int status;

    public String getTaskNumber() {
        return taskNumber;
    }

    public void setTaskNumber(String taskNumber) {
        this.taskNumber = taskNumber;
    }

    public int getAgvId() {
        return agvId;
    }

    public void setAgvId(int agvId) {
        this.agvId = agvId;
    }

    public Integer getStartStationId() {
        return startStationId;
    }

    public void setStartStationId(Integer startStationId) {
        this.startStationId = startStationId;
    }

    public Integer getTerminalStationId() {
        return terminalStationId;
    }

    public void setTerminalStationId(Integer terminalStationId) {
        this.terminalStationId = terminalStationId;
    }

    public Integer getNotificationStationId() {
        return notificationStationId;
    }

    public void setNotificationStationId(Integer notificationStationId) {
        this.notificationStationId = notificationStationId;
    }

    public int getModeId() {
        return modeId;
    }

    public void setModeId(int modeId) {
        this.modeId = modeId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
