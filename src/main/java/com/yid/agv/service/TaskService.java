
package com.yid.agv.service;

import com.yid.agv.backend.InstantStatus;
import com.yid.agv.backend.datastorage.StationManager;
import com.yid.agv.backend.datastorage.TaskQueue;
import com.yid.agv.model.QTask;
import com.yid.agv.model.Task;
import com.yid.agv.repository.TaskDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class TaskService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private TaskDao taskDao;

    @Autowired
    private StationManager stationManager;
    @Autowired
    private TaskQueue taskQueue;
    
    private String lastDate;

    public Map<Integer, Integer> getCompletedTasksMap() {
        return InstantStatus.getCallerStationStatusMap();
    }

    public Collection<QTask> getTaskQueue(){
        return taskQueue.getTaskQueueCopy();
    }

    public List<Task> queryTodayTasks(){
        return taskDao.queryTodayTasks();
    }

    public List<Task> queryTasksByDate(String date){
        return taskDao.queryTasksByDate(date);
    }

    public List<Task> queryAllTasks(){
        return taskDao.queryAllTasks();
    }

    public boolean cancelTask(String taskNumber){
        return taskQueue.removeTaskByTaskNumber(taskNumber) && taskDao.cancelTask(taskNumber);
    }

    public boolean insertTaskAndAddTask(String time, String agv, String start, String notification, String mode) {
        if(time.equals("")
            ||agv.equals("")
            ||start.equals("")
            ||notification.equals("")
            ||mode.equals("")) return false;

        if (stationManager.getStationStatus(Integer.parseInt(start)).getStatus() != 1)
            return false;
        // getTerminal if not, return false
        Integer terminal = taskQueue.getTerminalByNotification(notification);
        if (terminal == null) return false;

        String lastTaskNumber = taskDao.selectLastTaskNumber();
        if (lastDate == null) {
            // 伺服器重啟
            lastDate = lastTaskNumber.substring(1, 9);
        }
        System.out.println("lastDate: "+lastDate);
        System.out.println("time.substring(0, 8): "+time.substring(0, 8));
        int serialNumber;
        if (!lastDate.equals(time.substring(0, 8))){
            serialNumber = 1;
            lastDate = time.substring(0, 8);
        } else {
            // 日期未變更，流水號遞增
            serialNumber = Integer.parseInt(lastTaskNumber.substring(9));
            serialNumber++;
        }
        String taskNumber = "#" + lastDate + String.format("%04d", serialNumber);

        QTask newTask = new QTask();
        newTask.setStatus(0);
        newTask.setTaskNumber(taskNumber);
        newTask.setAgvId(Integer.parseInt(agv));
        newTask.setModeId(Integer.parseInt(mode));
        newTask.setStartStationId(Integer.parseInt(start));
        newTask.setNotificationStationId(Integer.parseInt(notification));
        newTask.setTerminalStationId(terminal);

        return taskQueue.addTaskToQueue(newTask) &&
                taskDao.insertTask(taskNumber, time, agv, start, terminal.toString(), notification, mode);
//        return ProcessTasks.addTaskToQueue(newTask) &&
//                (("".equals(start)) ?
//                taskDao.insertTaskNoStart(taskNumber, time, agv, terminal.toString(), notification, mode) :
//                taskDao.insertTask(taskNumber, time, agv, start, terminal.toString(), notification, mode));
        // 這個專案中目前taskDao.insertTaskNoStart()永遠不會用到。
    }
}
