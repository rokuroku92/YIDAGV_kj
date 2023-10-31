package com.yid.agv.model;

import lombok.Data;

@Data
public class NowTaskList {
    private Long id;
    private String taskNumber;
    private int steps;
    private int progress;
    private String phase;

}
