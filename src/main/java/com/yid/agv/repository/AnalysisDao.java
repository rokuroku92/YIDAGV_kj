package com.yid.agv.repository;


import com.yid.agv.model.Analysis;
import com.yid.agv.model.AnalysisId;
import com.yid.agv.model.YearMonthDay;

import java.util.List;
import java.util.Map;

public interface AnalysisDao {
    
    List<Analysis> queryAnalysisByAGV(Integer agvId);
    
    List<Analysis> queryAnalysisRecentlyByAGV(Integer agvId);
    
    List<Analysis> queryAnalysisByAGVAndYearAndMonth(Integer agvId, Integer year, Integer month);
    
    List<Map<String, Object>> getAnalysisYearsAndMonths();

    void insertNewDayAnalysis(String agvId, String year, String month, String day, String week);

    YearMonthDay getLastAnalysisYMD();

    List<AnalysisId> getTodayAnalysisId();

    Analysis queryAnalysisByAnalysisId(Integer analysisId);
    void updateOpenMinute(Integer openMinute, Integer analysisId);
    void updateWorkingMinute(Integer workingMinute, Integer analysisId);
    void updateTask(Integer task, Integer analysisId);
}
