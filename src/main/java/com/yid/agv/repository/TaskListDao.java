package com.yid.agv.repository;

import com.yid.agv.model.TaskList;

import java.util.List;

public interface TaskListDao {

    List<TaskList> queryTodayTaskLists();
    List<TaskList> queryTaskListsByDate(String date);
    List<TaskList> queryTaskLists();
    List<TaskList> queryAllTaskLists();

    String selectLastTaskListNumber();

    boolean insertTaskList(String taskNumber, String createTime, int step);

    boolean updateTaskListProgress(String taskNumber, int progress);

    boolean updateTaskListPhase(String taskNumber, Phase phase);

    boolean updateTaskListStatus(String taskNumber, int status);

    boolean cancelTaskList(String taskNumber);
}
