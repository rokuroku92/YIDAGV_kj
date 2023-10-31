package com.yid.agv.model;

import lombok.Data;

@Data
public class TaskDetail {
    private Long id;
    private String taskNumber;
    private String title;
    private int sequence;
    private String start;
    private Integer startTag;
    private String terminal;
    private Integer terminalTag;
    private int mode;
    private String modeMemo;
    private int status;

}
