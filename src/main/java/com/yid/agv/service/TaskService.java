
package com.yid.agv.service;

import com.yid.agv.backend.agvtask.AGVQTask;
import com.yid.agv.backend.station.Grid;
import com.yid.agv.backend.station.GridManager;
import com.yid.agv.backend.agvtask.AGVTaskManager;
import com.yid.agv.dto.TaskRequest;
import com.yid.agv.model.TaskList;
import com.yid.agv.repository.GridListDao;
import com.yid.agv.repository.TaskListDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskListDao taskListDao;
    @Autowired
    private GridListDao gridListDao;
    @Autowired
    private GridManager gridManager;
    @Autowired
    private AGVTaskManager taskQueue;
    
    private String lastDate;


    public Collection<AGVQTask> getTaskQueue(){
        return taskQueue.getTaskQueueCopy();
    }

    public List<TaskList> queryUnCompletedTasks(){
        return taskListDao.queryUncompletedTaskLists();
    }

    public List<TaskList> queryTaskLists(){
        return taskListDao.queryTaskLists();
    }
    public List<TaskList> queryAllTaskLists(){
        return taskListDao.queryAllTaskLists();
    }

    public boolean cancelTask(String taskNumber){

        return taskQueue.removeTaskByTaskNumber(taskNumber);
    }

    public String addTask(TaskRequest taskRequest){
        if(taskRequest.getStartGrid() == null){
            return "未輸入起始格位";
        } else if(taskRequest.getTerminalGrid() == null){
            return "未輸入終點格位";
        }
        Integer startStationId = gridManager.getGirdStationId(taskRequest.getStartGrid());
        Integer terminalStationId = gridManager.getGirdStationId(taskRequest.getTerminalGrid());
        if(startStationId == null){
            return "起始格位輸入錯誤";
        } else if (terminalStationId == null) {
            return "終點格位輸入錯誤";
        }

        if(gridManager.getGridStatus(taskRequest.getStartGrid()) == Grid.Status.FREE){
            return "起始格位無車輛";
        } else if(gridManager.getGridStatus(taskRequest.getStartGrid()) == Grid.Status.BOOKED){
            return "起始格位已被排程";
        }

        if(gridManager.getGridStatus(taskRequest.getTerminalGrid()) == Grid.Status.OCCUPIED){
            return "終點格位上有車輛";
        } else if(gridManager.getGridStatus(taskRequest.getTerminalGrid()) == Grid.Status.BOOKED){
            return "終點格位已被排程";
        }
        if(taskListDao.queryUncompletedTaskLists().size()>30){
            return "任務已達上限！！";
        }

        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formattedDateTime = currentDateTime.format(formatter);

        String taskNumber = "#NE" + getPureTaskNumber();
        taskListDao.insertTaskList(taskNumber, formattedDateTime, 1, startStationId, terminalStationId, TaskListDao.Mode.DEFAULT);
        return "成功發送！ 任務號碼： ".concat(taskNumber);
    }

//    public String addTaskList(TaskListRequest taskListRequest);

    private String getPureTaskNumber(){
        String lastTaskNumber = taskListDao.selectLastTaskListNumber();
        if (lastDate == null) {
            // 伺服器重啟
            lastDate = lastTaskNumber.substring(3, 11);
        }
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = currentDate.format(formatter);
        int serialNumber;
        if (!lastDate.equals(formattedDate)){
            serialNumber = 1;
            lastDate = formattedDate;
        } else {
            // 日期未變更，流水號遞增
            serialNumber = Integer.parseInt(lastTaskNumber.substring(11));
            serialNumber++;
        }
        return lastDate + String.format("%04d", serialNumber);
    }

}
