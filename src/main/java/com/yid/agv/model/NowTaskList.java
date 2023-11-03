package com.yid.agv.model;

import com.yid.agv.repository.Phase;
import lombok.Data;

@Data
public class NowTaskList {
    private Long id;
    private String taskNumber;
    private int steps;
    private int progress;
    private int phaseId;  // 此值僅用於回傳的資料庫數據，以下方 phase 為準。
    private Phase phase;
}
