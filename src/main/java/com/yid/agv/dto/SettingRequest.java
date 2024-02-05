package com.yid.agv.dto;

import lombok.Data;

@Data
public class SettingRequest {
    private String agvControlUrl;
    private Integer agvLowBattery;
    private Integer agvLowBatteryDuration;
    private Integer agvObstacleDuration;
    private Integer agvTaskExceptionOption;
    private Integer httpTimeout;
    private Integer httpMaxRetry;
}
