package com.yid.agv.backend;

import com.yid.agv.model.Analysis;
import com.yid.agv.model.AnalysisId;
import com.yid.agv.model.YearMonthDay;
import com.yid.agv.repository.AGVIdDao;
import com.yid.agv.repository.AnalysisDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Component
public class CountUtilizationRate {
    private final String checkUrl = "http://example.com/checkPower"; // 替換成開機API網址
    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private AnalysisDao analysisDao;
    @Autowired
    private AGVIdDao agvIdDao;
    private String lastDate;
    private List<AnalysisId> analysisIds;

    @Scheduled(fixedRate = 60000) // 每分鐘執行一次
    public void checkAgvStatus() {
        boolean[] isPoweredOn = checkPower(); // 檢查AGV是否開機
        boolean[] isWorking = checkStatus(); // 檢查AGV是否工作

        // 現在時間
        LocalDate currentDate = LocalDate.now();
        String year = String.valueOf(currentDate.getYear());
        String month = String.valueOf(currentDate.getMonthValue());
        String day = String.valueOf(currentDate.getDayOfMonth());
        String week = String.valueOf(currentDate.getDayOfWeek().getValue());
        String nowDate = year.concat(month).concat(day);

        if(lastDate == null){
            YearMonthDay ymd = analysisDao.getLastAnalysisYMD();
            lastDate = String.valueOf(ymd.getYear()).concat(String.valueOf(ymd.getMonth())).concat(String.valueOf(ymd.getDay()));
            analysisIds = analysisDao.getTodayAnalysisId();
        }
        if(!lastDate.equals(nowDate)){
            for(int i=0;i<agvIdDao.queryAGVList().size();i++)
                analysisDao.insertNewDayAnalysis(Integer.toString(agvIdDao.queryAGVList().get(i).getId()), year, month, day, week);
            lastDate = nowDate;
            analysisIds = analysisDao.getTodayAnalysisId();
        }

//        for (AnalysisId analysisId : analysisIds) {
//            System.out.println(analysisId.getAgvId());
//            System.out.println(analysisId.getAnalysisId());
//        }

        for(int i=0;i<agvIdDao.queryAGVList().size();i++){
            // 執行相應的處理邏輯，如更新資料庫、記錄日誌等
            Analysis analysis = analysisDao.queryAnalysisesByAnalysisId(analysisIds.get(i).getAnalysisId());
            if (isPoweredOn[i]) {
                // AGV開機處理邏輯
                analysisDao.updateOpenMinute(analysis.getOpenMinute()+1, analysisIds.get(i).getAnalysisId());
                System.out.println("BootMinute++");
            } // AGV未開機則不處理業務邏輯

            if (isWorking[i]) {
                // AGV工作處理邏輯
                analysisDao.updateWorkingMinute(analysis.getWorkingMinute()+1, analysisIds.get(i).getAnalysisId());
                System.out.println("WorkingMinute++");
            } // AGV停止工作則不處理業務邏輯
        }

    }
    private boolean[] checkPower() {
        // 發送HTTP請求至開機API網址，檢查網址的內容並返回結果
        // 使用RestTemplate或其他HTTP客戶端進行請求，並解析回應內容
        // 返回開機狀態，true表示開機，false表示未開機
//        return restTemplate.getForObject(checkPowerUrl, Boolean.class);
        return new boolean[]{true, true, true};
    }

    private boolean[] checkStatus() {
        // 發送HTTP請求至工作API網址，檢查網址的內容並返回結果
        // 使用RestTemplate或其他HTTP客戶端進行請求，並解析回應內容
        // 返回工作狀態，true表示工作中，false表示停止工作
//        return restTemplate.getForObject(checkStatusUrl, Boolean.class);
        return new boolean[]{new Random().nextBoolean(), new Random().nextBoolean(), new Random().nextBoolean()};
    }

}
