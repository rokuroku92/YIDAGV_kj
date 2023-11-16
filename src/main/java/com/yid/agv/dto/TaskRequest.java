package com.yid.agv.dto;

import lombok.Data;

@Data
public class TaskRequest {
    private String startGrid;
    private String terminalGrid;
}
