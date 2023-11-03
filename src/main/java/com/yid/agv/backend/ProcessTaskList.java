package com.yid.agv.backend;

import com.yid.agv.backend.agvtask.AGVQTask;
import com.yid.agv.backend.agvtask.AGVTaskManager;
import com.yid.agv.backend.tasklist.TaskListManager;
import com.yid.agv.model.NowTaskList;
import com.yid.agv.model.TaskDetail;
import com.yid.agv.repository.NowTaskListDao;
import com.yid.agv.repository.Phase;
import com.yid.agv.repository.TaskDetailDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProcessTaskList {
    @Autowired
    private NowTaskListDao nowTaskListDao;
    @Autowired
    private TaskDetailDao taskDetailDao;
    @Autowired
    private AGVTaskManager AGVTaskManager;
    @Autowired
    private TaskListManager taskListManager;


    @Scheduled(fixedRate = 4000)
    public void checkTaskList() {
        for(int i = 1; i <= taskListManager.getTaskListMapSize(); i++){
            NowTaskList nowTaskList = taskListManager.getNowTaskListByTaskProcessId(i);
            if (nowTaskList == null) continue;
            List<TaskDetail> taskDetails = taskListManager.getTaskDetailByTaskNumber(nowTaskList.getTaskNumber());

            if (nowTaskList.getTaskNumber().startsWith("#YE")){
                handleYETask(nowTaskList, taskDetails);
            } else if (nowTaskList.getTaskNumber().startsWith("#RE")){
                handleRETask(nowTaskList, taskDetails);
            } else if (nowTaskList.getTaskNumber().startsWith("#NE")){
                handleNETask(nowTaskList, taskDetails);
            }
        }

    }

    private void handleYETask(NowTaskList nowTaskList, List<TaskDetail> taskDetails){
        switch (nowTaskList.getPhase()) {
            case PRE_START -> {
                if(iElevatorIdle){  // TODO: check elevator permission
                    // TODO: get elevator permission
                    if(iGetElevatorPermission){
                        taskListManager.setTaskListPhase(nowTaskList, Phase.CALL_ELEVATOR);
                    }
                }
            }
            case CALL_ELEVATOR -> {
                // TODO: call elevator
                if(iElevatorOpenDoor){
                    taskDetails.forEach(taskDetail -> {
                        if (taskDetail.getTitle().equals("AMR#1")) {
                            AGVQTask task = new AGVQTask();
                            task.setAgvId(1);
                            task.setTaskNumber(taskDetail.getTaskNumber());
                            task.setSequence(taskDetail.getSequence());
                            task.setModeId(taskDetail.getMode());
                            task.setStartStationId(taskDetail.getStartId());
                            task.setTerminalStationId(taskDetail.getTerminalId());
                            task.setStatus(0);
                            AGVTaskManager.addTaskToQueueByAGVId(1, task);
                        }
                    });
                    taskListManager.setTaskListPhase(nowTaskList, Phase.FIRST_STAGE_1F);
                }
            }
            case FIRST_STAGE_1F -> {

            }
            case ELEVATOR_TRANSFER -> {

            }
            case SECOND_STAGE_3F -> {

            }
            case THIRD_STAGE_3F -> {

            }
            case COMPLETED -> {

            }
        }
    }

    private void handleRETask(NowTaskList nowTaskList, List<TaskDetail> taskDetails){
        switch (nowTaskList.getPhase()) {
            case PRE_START -> {

            }
            case CALL_ELEVATOR -> {

            }
            case FIRST_STAGE_3F -> {

            }
            case ELEVATOR_TRANSFER -> {

            }
            case SECOND_STAGE_1F -> {

            }
            case COMPLETED -> {

            }
        }
    }

    private void handleNETask(NowTaskList nowTaskList, List<TaskDetail> taskDetails){
        switch (nowTaskList.getPhase()) {
            case PRE_START -> {

            }
            case TRANSFER -> {

            }
            case COMPLETED -> {

            }
        }
    }


}
