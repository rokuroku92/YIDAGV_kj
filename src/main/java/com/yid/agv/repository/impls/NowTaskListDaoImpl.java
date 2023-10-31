package com.yid.agv.repository.impls;

import com.yid.agv.model.NowTaskList;
import com.yid.agv.repository.NowTaskListDao;
import com.yid.agv.repository.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NowTaskListDaoImpl implements NowTaskListDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<NowTaskList> queryNowTaskLists(){
        String sql = "SELECT ntl.id, ntl.task_number, ntl.steps, ntl.progress, tp.name AS phase " +
                "FROM now_task_list ntl INNER JOIN task_phase tp ON ntl.phase_id = tp.id ORDER BY id DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(NowTaskList.class));
    }

    @Override
    public boolean updateNowTaskListPhase(String taskNumber, Phase phase){
        String sql = "UPDATE `now_task_list` SET `phase_id` = ? WHERE `task_number` = ?";
        int rowsAffected = jdbcTemplate.update(sql, phase.getValue(), taskNumber);
        return (rowsAffected > 0);
    }

    @Override
    public boolean updateNowTaskListProgress(String taskNumber, int progress){
        String sql = "UPDATE `now_task_list` SET `progress` = ? WHERE `task_number` = ?";
        int rowsAffected = jdbcTemplate.update(sql, progress, taskNumber);
        return (rowsAffected > 0);
    }

    @Override
    public boolean deleteNowTaskList(String taskNumber){
        String sql = "DELETE FROM `now_task_list` WHERE task_number = ?";
        int rowsAffected = jdbcTemplate.update(sql, taskNumber);
        return (rowsAffected > 0);
    }
}
