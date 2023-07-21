package com.yid.agv.service;

import com.yid.agv.model.Analysis;

import java.util.List;
import java.util.Map;

import com.yid.agv.repository.AnalysisDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalysisService {

    @Autowired
    private AnalysisDao analysisDao;

    public List<Analysis> queryAnalysisesByAGV(Integer agvId){
        return analysisDao.queryAnalysisesByAGV(agvId);
    }

    public List<Analysis> queryAnalysisesRecentlyByAGV(Integer agvId){
        return analysisDao.queryAnalysisesRecentlyByAGV(agvId);
    }

    public List<Analysis> queryAnalysisesByAGVAndYearAndMonth(Integer agvId, Integer year, Integer month){
        // 參數檢查
        if(year<2022 || month < 1 || month > 12) return null;
        return analysisDao.queryAnalysisesByAGVAndYearAndMonth(agvId, year, month);
    }

    public List<Map<String, Object>> getAnalysisesYearsAndMonths(){
        return analysisDao.getAnalysisesYearsAndMonths();
    }

}
