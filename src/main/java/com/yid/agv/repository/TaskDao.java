package com.yid.agv.repository;


import com.yid.agv.model.TaskList;

import java.util.List;

public interface TaskDao {

    List<TaskList> queryTodayTaskLists();
    List<TaskList> queryTaskListsByDate(String date);
    
    List<TaskList> queryAllTaskLists();
    
    String selectLastTaskListNumber();
    
    boolean insertTaskList(String taskNumber, String createTime, String step);

    
    boolean updateTaskListStatus(String taskNumber, int status);
    
    boolean cancelTaskList(String taskNumber);
}
