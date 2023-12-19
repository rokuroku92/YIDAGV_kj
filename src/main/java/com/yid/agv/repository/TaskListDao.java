package com.yid.agv.repository;

import com.yid.agv.model.TaskList;

import java.util.List;

public interface TaskListDao {
    enum Mode{
        DEFAULT(1);
        private final int value;
        Mode(int value) {
            this.value = value;
        }
        public int getValue(){
            return value;
        }
    }

    List<TaskList> queryUncompletedTaskLists();
    List<TaskList> queryUnexpectedTaskLists();
    List<TaskList> queryTaskListsByDate(String date);
    List<TaskList> queryTaskLists();
    List<TaskList> queryAllTaskLists();

    String selectLastTaskListNumber();

    boolean insertTaskList(String taskNumber, String createTime, int agvId, Integer startId, Integer terminalId, Mode mode);
    boolean insertTaskList(String taskNumber, String createTime, int agvId, Integer startId, Integer terminalId, Mode mode, int status);

    boolean updateTaskListStatus(String taskNumber, int status);

    boolean cancelTaskList(String taskNumber);
}
