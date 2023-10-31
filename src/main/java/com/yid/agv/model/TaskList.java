package com.yid.agv.model;

// for Dao
public class TaskList {
    private Long id;
    private String taskNumber;
    private String createTaskTime;
    private int steps;
    private int progress;
    private int phaseId;
    private int status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskNumber() {
        return taskNumber;
    }

    public void setTaskNumber(String taskNumber) {
        this.taskNumber = taskNumber;
    }

    public String getCreateTaskTime() {
        return createTaskTime;
    }

    public void setCreateTaskTime(String createTaskTime) {
        this.createTaskTime = createTaskTime;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getPhaseId() {
        return phaseId;
    }

    public void setPhaseId(int phaseId) {
        this.phaseId = phaseId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
