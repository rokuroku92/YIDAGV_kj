package com.yid.agv.repository.impls;

import com.yid.agv.model.Analysis;
import com.yid.agv.model.AnalysisId;
import com.yid.agv.model.YearMonthDay;
import com.yid.agv.repository.AnalysisDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class AnalysisDaoImpl implements AnalysisDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Override
    public List<Analysis> queryAnalysisesByAGV(Integer agvId){
        String sql = "select a.analysis_id, a.agv_id, a.year, a.month, a.day, a.week, a.working_minute, a.open_minute, a.task " +
                     "from analysis a where agv_id=? order by year, month, day";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Analysis.class), agvId);
    }
    @Override
    public List<Analysis> queryAnalysisesRecentlyByAGV(Integer agvId){
        String sql = "select * from (select a.analysis_id, a.agv_id, a.year, a.month, a.day, a.week, a.working_minute, a.open_minute, a.task " +
                     "from analysis a where agv_id=? order by a.analysis_id DESC LIMIT 14)a order by analysis_id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Analysis.class), agvId);
    }
    @Override
    public List<Analysis> queryAnalysisesByAGVAndYearAndMonth(Integer agvId, Integer year, Integer month){
        String sql = "select a.analysis_id, a.agv_id, a.year, a.month, a.day, a.week, a.working_minute, a.open_minute, a.task " +
                     "from analysis a where agv_id=? and year=? and month=? order by day";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Analysis.class), agvId, year, month);
    }
    @Override
    public List<Map<String, Object>> getAnalysisesYearsAndMonths(){
        String sql = "select DISTINCT a.year,a.month from analysis a order by year, month";
        return jdbcTemplate.queryForList(sql);
    }
    @Override
    public void insertNewDayAnalysis(String agvId, String year, String month, String day, String week){
        String sql = "INSERT INTO `analysis`(`agv_id`,`year`,`month`,`day`,`week`,`working_minute`,`open_minute`,`task`) VALUES(?,?,?,?,?,0,0,0)";
        // 使用 JdbcTemplate 的 update 方法執行 SQL 語句
        jdbcTemplate.update(sql, agvId, year, month, day, week);
    }
    @Override
    public YearMonthDay getLastAnalysisYMD(){
        String sql = "SELECT year, month, day FROM analysis WHERE (year, month, day) <= (SELECT MAX(year), MAX(month), MAX(day) FROM analysis) " +
                     "ORDER BY year DESC, month DESC, day DESC LIMIT 1";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            YearMonthDay ymd = new YearMonthDay();
            ymd.setYear(rs.getInt("year"));
            ymd.setMonth(rs.getInt("month"));
            ymd.setDay(rs.getInt("day"));
            return ymd;
        });
    }
    @Override
    public List<AnalysisId> getTodayAnalysisId(){
        String sql = "SELECT agv_id, MAX(analysis_id) as analysis_id FROM analysis WHERE agv_id IN (1, 2, 3) GROUP BY agv_id ORDER BY agv_id";
//        return jdbcTemplate.query(sql, (rs, rowNum) -> {
//            AnalysisId a = new AnalysisId();
//            a.setAgvId(rs.getInt("agv_id"));
//            a.setAnalysisId(rs.getInt("analysis_id"));
//            return a;
//        });
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(AnalysisId.class));
    }
    @Override
    public Analysis queryAnalysisesByAnalysisId(Integer analysisId){
        String sql = "SELECT * FROM analysis WHERE analysis_id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Analysis a = new Analysis();
            a.setAnalysisId(rs.getLong("analysis_id"));
            a.setAgvId(rs.getInt("agv_id"));
            a.setYear(rs.getInt("year"));
            a.setMonth(rs.getInt("month"));
            a.setDay(rs.getInt("day"));
            a.setWeek(rs.getInt("week"));
            a.setOpenMinute(rs.getInt("open_minute"));
            a.setWorkingMinute(rs.getInt("working_minute"));
            a.setTask(rs.getInt("task"));
            return a;
        }, analysisId);
    }
    @Override
    public void updateOpenMinute(Integer openMinute, Integer analysisId) {
        String sql = "UPDATE `analysis` SET `open_minute` = ? WHERE `analysis_id` = ?";
        // 使用 JdbcTemplate 的 update 方法執行 SQL 語句
        jdbcTemplate.update(sql, openMinute, analysisId);
    }
    @Override
    public void updateWorkingMinute(Integer workingMinute, Integer analysisId) {
        String sql = "UPDATE `analysis` SET `working_minute` = ? WHERE `analysis_id` = ?";
        // 使用 JdbcTemplate 的 update 方法執行 SQL 語句
        jdbcTemplate.update(sql, workingMinute, analysisId);
    }

    @Override
    public void updateTask(Integer task, Integer analysisId) {
        String sql = "UPDATE `analysis` SET `task` = ? WHERE `analysis_id` = ?";
        // 使用 JdbcTemplate 的 update 方法執行 SQL 語句
        jdbcTemplate.update(sql, task, analysisId);
    }
}
