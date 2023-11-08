package com.yid.agv.backend;

import com.yid.agv.backend.agv.AGVManager;
import com.yid.agv.backend.agvtask.AGVQTask;
import com.yid.agv.backend.agvtask.AGVTaskManager;
import com.yid.agv.backend.elevator.ElevatorManager;
import com.yid.agv.backend.tasklist.TaskListManager;
import com.yid.agv.model.NowTaskList;
import com.yid.agv.model.TaskDetail;
import com.yid.agv.repository.NowTaskListDao;
import com.yid.agv.repository.Phase;
import com.yid.agv.repository.TaskDetailDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
public class ProcessTaskList {
    @Autowired
    private NowTaskListDao nowTaskListDao;
    @Autowired
    private TaskDetailDao taskDetailDao;
    @Autowired
    private AGVManager agvManager;
    @Autowired
    private AGVTaskManager agvTaskManager;
    @Autowired
    private TaskListManager taskListManager;
    @Autowired
    private ElevatorManager elevatorManager;


    @Scheduled(fixedRate = 4000)
    public void checkTaskList() {
        for(int i = 1; i <= taskListManager.getTaskListMapSize(); i++){
            NowTaskList nowTaskList = taskListManager.getNowTaskListByTaskProcessId(i);
            if (nowTaskList == null) continue;
            List<TaskDetail> taskDetails = taskListManager.getTaskDetailByTaskNumber(nowTaskList.getTaskNumber());

            if (nowTaskList.getTaskNumber().startsWith("#YE")){
                handleYETask(nowTaskList, taskDetails, i);
            } else if (nowTaskList.getTaskNumber().startsWith("#RE")){
                handleRETask(nowTaskList, taskDetails, i);
            } else if (nowTaskList.getTaskNumber().startsWith("#NE")){
                handleNETask(nowTaskList, taskDetails, i);
            }
        }

    }

    private void handleYETask(NowTaskList nowTaskList, List<TaskDetail> taskDetails, int taskProcessId){
        switch (nowTaskList.getPhase()) {
            case PRE_START -> {
                if(elevatorManager.acquireElevatorPermission()){  // check elevator permission
                    elevatorManager.controlElevatorDoor(1, true);
                    taskListManager.setTaskListPhase(nowTaskList, Phase.CALL_ELEVATOR);
                    taskListManager.setTaskListProgress(nowTaskList, 1);
                }
            }
            case CALL_ELEVATOR -> {
                if(elevatorManager.getIOpenDoor()){
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
                            agvTaskManager.addTaskToQueueByAGVId(task);
                        }
                    });
                    taskDetails.forEach(taskDetail -> {
                        if (taskDetail.getMode() == 100) {
                            taskListManager.setTaskListProgress(nowTaskList, taskDetail.getSequence()/99);
                        }
                    });
                    taskListManager.setTaskListPhase(nowTaskList, Phase.FIRST_STAGE_1F);
                }
            }
            case FIRST_STAGE_1F -> {
                if(agvManager.getAgv(1).getTask().getTaskNumber().startsWith("#SB") && !agvManager.iAgvInElevator(1)){
                    elevatorManager.controlElevatorDoor(1, false);
                    elevatorManager.controlElevatorDoor(3, true);
                    taskListManager.setTaskListPhase(nowTaskList, Phase.ELEVATOR_TRANSFER);
                }
            }
            case ELEVATOR_TRANSFER -> {
                if(elevatorManager.getIOpenDoor()){
                    taskDetails.forEach(taskDetail -> {
                        if (taskDetail.getTitle().equals("AMR#3") && taskDetail.getStart().startsWith("E-")) {
                            AGVQTask task = new AGVQTask();
                            task.setAgvId(3);
                            task.setTaskNumber(taskDetail.getTaskNumber());
                            task.setSequence(taskDetail.getSequence());
                            task.setModeId(taskDetail.getMode());
                            task.setStartStationId(taskDetail.getStartId());
                            task.setTerminalStationId(taskDetail.getTerminalId());
                            task.setStatus(0);
                            agvTaskManager.addTaskToQueueByAGVId(task);
                        }
                    });
                    taskDetails.forEach(taskDetail -> {
                        if (taskDetail.getMode() == 101) {
                            taskListManager.setTaskListProgress(nowTaskList, taskDetail.getSequence()/99);
                        }
                    });
                    taskListManager.setTaskListPhase(nowTaskList, Phase.SECOND_STAGE_3F);
                }
            }
            case SECOND_STAGE_3F -> {
                if(agvTaskManager.isEmpty(3) && agvManager.getAgv(3).getTask() == null){
                    elevatorManager.controlElevatorDoor(3, false);
                    elevatorManager.resetElevatorPermission();  // unlock elevator permission
                    taskDetails.forEach(taskDetail -> {
                        if (taskDetail.getTitle().equals("AMR#3") && taskDetail.getStart().startsWith("3-T-")) {
                            AGVQTask task = new AGVQTask();
                            task.setAgvId(3);
                            task.setTaskNumber(taskDetail.getTaskNumber());
                            task.setSequence(taskDetail.getSequence());
                            task.setModeId(taskDetail.getMode());
                            task.setStartStationId(taskDetail.getStartId());
                            task.setTerminalStationId(taskDetail.getTerminalId());
                            task.setStatus(0);
                            agvTaskManager.addTaskToQueueByAGVId(task);
                        }
                    });
                    taskListManager.setTaskListPhase(nowTaskList, Phase.THIRD_STAGE_3F);
                }
            }
            case THIRD_STAGE_3F -> {
                if(agvTaskManager.isEmpty(3) && agvManager.getAgv(3).getTask() == null){
                    taskListManager.setTaskListPhase(nowTaskList, Phase.COMPLETED);
                }
            }
            case COMPLETED -> {
                taskListManager.completedTaskList(taskProcessId);
            }
        }
    }

    private void handleRETask(NowTaskList nowTaskList, List<TaskDetail> taskDetails, int taskProcessId){
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

    private void handleNETask(NowTaskList nowTaskList, List<TaskDetail> taskDetails, int taskProcessId){
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
