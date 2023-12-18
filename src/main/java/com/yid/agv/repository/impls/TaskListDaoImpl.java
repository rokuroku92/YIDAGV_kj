package com.yid.agv.repository.impls;

import com.yid.agv.model.TaskList;
import com.yid.agv.repository.TaskListDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TaskListDaoImpl implements TaskListDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Override
    public List<TaskList> queryUncompletedTaskLists(){
        String sql = "SELECT tl.id, tl.task_number, tl.create_task_time, ad.name AS agv, tl.agv_id, tl.mode_id, sd.id AS start_id, " +
                "sd.name AS start, sdd.id AS terminal_id, sd.name AS terminal, tl.status FROM task_list tl " +
                "LEFT JOIN agv_data ad ON tl.agv_id = ad.id " +
                "LEFT JOIN station_data sd ON tl.start_id = sd.id " +
                "LEFT JOIN station_data sdd ON tl.terminal_id = sdd.id WHERE tl.status = 0 ORDER BY id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(TaskList.class));
    }
    @Override
    public List<TaskList> queryTaskListsByDate(String date){
        String sql = "SELECT tl.id, tl.task_number, tl.create_task_time, ad.name As agv, tl.agv_id, tl.mode_id, sd.id AS start_id, " +
                "sd.name AS start, sdd.id AS terminal_id, sd.name AS terminal, tl.status FROM task_list tl " +
                "LEFT JOIN agv_data ad ON tl.agv_id = ad.id " +
                "LEFT JOIN station_data sd ON tl.start_id = sd.id " +
                "LEFT JOIN station_data sdd ON tl.terminal_id = sdd.id " +
                "WHERE DATE_FORMAT(STR_TO_DATE(create_task_time, '%Y%m%d%H%i%s'), '%Y-%m-%d') = ? ORDER BY id DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(TaskList.class), date);
    }
    @Override
    public List<TaskList> queryTaskLists(){
        String sql = "SELECT tl.id, tl.task_number, tl.create_task_time, ad.name AS agv, tl.agv_id, tl.mode_id, sd.id AS start_id, " +
                "sd.name AS start, sdd.id AS terminal_id, sd.name AS terminal, tl.status FROM task_list tl " +
                "LEFT JOIN agv_data ad ON tl.agv_id = ad.id " +
                "LEFT JOIN station_data sd ON tl.start_id = sd.id " +
                "LEFT JOIN station_data sdd ON tl.terminal_id = sdd.id ORDER BY id DESC LIMIT 100";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(TaskList.class));
    }
    @Override
    public List<TaskList> queryAllTaskLists(){
        String sql = "SELECT tl.id, tl.task_number, tl.create_task_time, ad.name AS agv, tl.agv_id, tl.mode_id, sd.id AS start_id, " +
                "sd.name AS start, sdd.id AS terminal_id, sd.name AS terminal, tl.status FROM task_list tl " +
                "LEFT JOIN agv_data ad ON tl.agv_id = ad.id " +
                "LEFT JOIN station_data sd ON tl.start_id = sd.id " +
                "LEFT JOIN station_data sdd ON tl.terminal_id = sdd.id ORDER BY id DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(TaskList.class));
    }
    @Override
    public String selectLastTaskListNumber(){
        String sql = "SELECT task_number FROM task_list WHERE `task_number` LIKE '#%E%' ORDER BY task_number DESC LIMIT 1";
        return jdbcTemplate.queryForObject(sql, String.class);
    }
    @Override
    public boolean insertTaskList(String taskNumber, String createTime, int agvId, Integer startId, Integer terminalId, Mode mode){
        String sql = "INSERT INTO `task_list`(`task_number`, `create_task_time`, `agv_id`, `start_id`, `terminal_id`, `mode_id`) VALUES(?, ?, ?, ?, ?, ?)";

        // 使用 JdbcTemplate 的 update 方法執行 SQL 語句
        int rowsAffected = jdbcTemplate.update(sql, taskNumber, createTime, agvId, startId, terminalId, mode.getValue());
        return (rowsAffected > 0);
    }

    @Override
    public boolean updateTaskListStatus(String taskNumber, int status){
        String sql = "UPDATE `task_list` SET `status` = ? WHERE `task_number` = ?";
        int rowsAffected = jdbcTemplate.update(sql, status, taskNumber);
        return (rowsAffected > 0);
    }
    @Override
    public boolean cancelTaskList(String taskNumber){
        String sql = "UPDATE `task_list` SET `status` = -1 WHERE `task_number` = ?";
        // 使用 JdbcTemplate 的 update 方法執行 SQL 語句
        int rowsAffected = jdbcTemplate.update(sql, taskNumber);
        return (rowsAffected > 0);
    }


}
