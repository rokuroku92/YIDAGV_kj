package com.yid.agv.model;

import lombok.Data;

@Data
public class Analysis {
    private Long analysisId;
    private Integer agvId;
    private Integer year;
    private Integer month;
    private Integer day;
    private Integer week;

    private Integer workingMinute;
    private Integer openMinute;
    private Integer task;

}
