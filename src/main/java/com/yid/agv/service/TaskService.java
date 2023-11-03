
package com.yid.agv.service;

import com.yid.agv.backend.station.GridManager;
import com.yid.agv.backend.agvtask.AGVTaskManager;
import com.yid.agv.backend.agvtask.AGVQTask;
import com.yid.agv.model.NowTaskListResponse;
import com.yid.agv.model.TaskList;
import com.yid.agv.repository.NowTaskListDao;
import com.yid.agv.repository.TaskDetailDao;
import com.yid.agv.repository.TaskListDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class TaskService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TaskListDao taskListDao;
    @Autowired
    private NowTaskListDao nowTaskListDao;
    @Autowired
    private TaskDetailDao taskDetailDao;

    @Autowired
    private GridManager stationManager;
    @Autowired
    private AGVTaskManager taskQueue;
    
    private String lastDate;

//    public Map<Integer, Integer> getCompletedTasksMap() {
//        return AGVInstantStatus.getCallerStationStatusMap();
//    }

    public Collection<AGVQTask> getTaskQueue(){
        return taskQueue.getTaskQueueCopy();
    }

    public List<NowTaskListResponse> queryNowTaskLists(){
        return nowTaskListDao.queryNowTaskListsResult();
    }

    public List<TaskList> queryTaskLists(){
        return taskListDao.queryTaskLists();
    }
    public List<TaskList> queryAllTaskLists(){
        return taskListDao.queryAllTaskLists();
    }

    public boolean cancelTask(String taskNumber){
        return taskQueue.removeTaskByTaskNumber(taskNumber) && taskListDao.cancelTaskList(taskNumber);
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

        String lastTaskNumber = taskListDao.selectLastTaskListNumber();
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

        AGVQTask newTask = new AGVQTask();
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
