package com.yid.agv.repository.impls;

import com.yid.agv.model.Task;
import com.yid.agv.repository.TaskDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TaskDaoImpl implements TaskDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Override
    public List<Task> queryTodayTasks(){
        String sql = "SELECT * FROM task_history WHERE DATE_FORMAT(STR_TO_DATE(create_task_time, '%Y%m%d%H%i%s'), '%Y-%m-%d') = CURDATE() AND status != -1 ORDER BY id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Task.class));
    }
    @Override
    public List<Task> queryTasksByDate(String date){
        String sql = "SELECT * FROM task_history WHERE DATE_FORMAT(STR_TO_DATE(create_task_time, '%Y%m%d%H%i%s'), '%Y-%m-%d') = ? ORDER BY id DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Task.class), date);
    }
    @Override
    public List<Task> queryAllTasks(){
        String sql = "SELECT * FROM task_history ORDER BY id DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Task.class));
    }
    @Override
    public String selectLastTaskNumber(){
        String sql = "SELECT task_number FROM task_history ORDER BY task_number DESC LIMIT 1";
        return jdbcTemplate.queryForObject(sql, String.class);
    }
    @Override
    public boolean insertTask(String taskNumber, String createTime, String agv, String start, String terminal, String notification, String mode){
        String sql = "INSERT INTO `task_history`(`task_number`, `create_task_time`, `agv_id`, `start_id`, `terminal_id`, `notification_id`, `mode_id`)" +
                       "VALUES(?, ?, ?, ?, ?, ?, ?)";

        // 使用 JdbcTemplate 的 update 方法執行 SQL 語句
        int rowsAffected = jdbcTemplate.update(sql, taskNumber, createTime, agv, start, terminal, notification, mode);
        return (rowsAffected > 0);
    }
    @Override
    public boolean insertTaskNoStart(String taskNumber, String createTime, String agv, String terminal, String notification,String mode){
        String sql = "INSERT INTO `task_history`(`task_number`, `create_task_time`, `agv_id`, `terminal_id`, `notification_id`, `mode_id`)" +
                       "VALUES(?, ?, ?, ?, ?, ?)";

        // 使用 JdbcTemplate 的 update 方法執行 SQL 語句
        int rowsAffected = jdbcTemplate.update(sql, taskNumber, createTime, agv, terminal, notification, mode);
        return (rowsAffected > 0);
    }
    @Override
    public boolean updateTaskStatus(String taskNumber, int status){
        String sql = "UPDATE `task_history` SET `status` = ? WHERE (`task_number` = ?)";
        int rowsAffected = jdbcTemplate.update(sql, status, taskNumber);
        return (rowsAffected > 0);
    }
    @Override
    public boolean cancelTask(String taskNumber){
        String sql = "UPDATE `task_history` SET `status` = '-1' WHERE (`task_number` = ?)";
        // 使用 JdbcTemplate 的 update 方法執行 SQL 語句
        int rowsAffected = jdbcTemplate.update(sql, taskNumber);
        return (rowsAffected > 0);
    }
}
