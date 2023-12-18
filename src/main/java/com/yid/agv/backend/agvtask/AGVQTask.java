package com.yid.agv.backend.agvtask;

import lombok.Data;

@Data
public class AGVQTask {
    private String taskNumber;
    private int agvId;
    private String startStation;
    private Integer startStationId;
    private String terminalStation;
    private Integer terminalStationId;
    private int modeId;
    private int status;

}
