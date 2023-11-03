package com.yid.agv.model;

import lombok.Data;

@Data
public class NowTaskListResponse {
    private Long id;
    private String taskNumber;
    private int steps;
    private int progress;
    private String phase;

}
