package com.yid.agv.repository;


import com.yid.agv.model.Task;

import java.util.List;

public interface TaskDao {
        
    List<Task> queryTodayTasks();
    
    List<Task> queryTasksByDate(String date);
    
    List<Task> queryAllTasks();
    
    String selectLastTaskNumber();
    
    boolean insertTask(String taskNumber, String createTime, String agv, String start, String terminal, String notification, String mode);
    
    boolean insertTaskNoStart(String taskNumber, String createTime, String agv, String terminal, String notification,String mode);
    
    boolean updateTaskStatus(String taskNumber, int status);
    
    boolean cancelTask(String taskNumber);
}
