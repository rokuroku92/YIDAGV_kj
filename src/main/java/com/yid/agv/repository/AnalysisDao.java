package com.yid.agv.repository;


import com.yid.agv.model.Analysis;
import com.yid.agv.model.AnalysisId;
import com.yid.agv.model.YearMonthDay;

import java.util.List;
import java.util.Map;

public interface AnalysisDao {
    
    List<Analysis> queryAnalysisesByAGV(Integer agvId);
    
    List<Analysis> queryAnalysisesRecentlyByAGV(Integer agvId);
    
    List<Analysis> queryAnalysisesByAGVAndYearAndMonth(Integer agvId, Integer year, Integer month);
    
    List<Map<String, Object>> getAnalysisesYearsAndMonths();

    void insertNewDayAnalysis(String agvId, String year, String month, String day, String week);

    YearMonthDay getLastAnalysisYMD();

    List<AnalysisId> getTodayAnalysisId();

    Analysis queryAnalysisesByAnalysisId(Integer analysisId);
    void updateOpenMinute(Integer openMinute, Integer analysisId);
    void updateWorkingMinute(Integer workingMinute, Integer analysisId);
    void updateTask(Integer task, Integer analysisId);
}
