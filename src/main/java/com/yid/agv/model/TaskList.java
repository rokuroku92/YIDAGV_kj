package com.yid.agv.model;

import lombok.Data;

@Data
public class TaskList {
    private Long id;
    private String taskNumber;
    private String createTaskTime;
    private String agv;
    private int agvId;
    private String start;
    private Integer startId;
    private String terminal;
    private Integer terminalId;
    private int modeId;
    private String modeMemo;
    private int status;

}
