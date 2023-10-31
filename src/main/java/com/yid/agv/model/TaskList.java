package com.yid.agv.model;

import lombok.Data;

@Data
public class TaskList {
    private Long id;
    private String taskNumber;
    private String createTaskTime;
    private int steps; // 任務共有幾個動作
    private int progress; // 任務進度百分比
    private String phase; // 任務階段
    private int status; // 任務狀態

}
