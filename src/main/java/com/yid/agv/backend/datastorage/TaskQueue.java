package com.yid.agv.backend.datastorage;


import com.yid.agv.backend.InstantStatus;
import com.yid.agv.model.QTask;
import com.yid.agv.repository.StationDao;
import com.yid.agv.repository.TaskDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class TaskQueue {
    @Autowired
    private StationDao stationDao;
    @Autowired
    private TaskDao taskDao;
    @Autowired
    private AGVManager agvManager;
    @Autowired
    private StationManager stationManager;
    private final Queue<QTask> taskQueue;
    private final int[] bookedStation;
    private String nowTaskNumber;

    private TaskQueue() {
        taskQueue = new ConcurrentLinkedDeque<>();
        bookedStation = new int[15];
        nowTaskNumber = "";
    }

    @SuppressWarnings("unused")
    private static class Holder {
        private static final TaskQueue INSTANCE = new TaskQueue();
    }



    public boolean iDispatch(){
        return !InstantStatus.iStandbyTask && !taskQueue.isEmpty() && !InstantStatus.getAgvLowBattery()[0] && nowTaskNumber.equals("");
    }

    public boolean isEmpty(){
        return taskQueue.isEmpty();
    }

    public boolean iEqualsStandbyStation(){
        int place = Integer.parseInt(agvManager.getAgvStatus(1).getPlace() == null ? "-1" : agvManager.getAgvStatus(1).getPlace());
        if (place == -1) return false;

        List<String> standbyTags = stationDao.queryStandbyTags();
        boolean iEquals = false;

        for (String standbyTag : standbyTags) {
            if (Integer.parseInt(standbyTag) == place
                    || Integer.parseInt(standbyTag)-500 == place)
                iEquals = true;
        }
        return iEquals;
    }

    public boolean addTaskToQueue(QTask task) {
        if(taskQueue.size() >= 5)
            return false;
        taskQueue.offer(task);
        bookedStation[task.getStartStationId()-1] = 1;
        bookedStation[task.getTerminalStationId()-1] = 2;
        return true;
    }


    public Integer getTerminalByNotification(String notificationId){
        if(taskQueue.size() >= 5) return null;
        int s,x;
        int notificationIdInt = Integer.parseInt(notificationId);
        if (notificationIdInt > 0 && notificationIdInt <= 5){
            s=1;x=5;
        }else if(notificationIdInt > 5 && notificationIdInt <= 10){
            s=6;x=10;
        }else if(notificationIdInt > 10 && notificationIdInt <= 15){
            s=11;x=15;
        }else {
            s = 0;x = 1;
        }
        for(int i=s;i<=x;i++){
            int stationStatus = stationManager.getStationStatus(i).getStatus();
            if(stationStatus == 0 || stationStatus == 1)
                return i;
        }
        return null;
    }


    private final int[] stationTag1 = new int[]{1001, 1252, 1254, 1256, 1258, 1260};
    private final int[] stationTag2 = new int[]{1024, 1513, 1515, 1517, 1771, 1773};
    public QTask peekTaskWithPlace() {
        if(taskQueue.isEmpty())return null;
        int place = Integer.parseInt(agvManager.getAgvStatus(1).getPlace()); // 只有一台車id=1
        int start = -1, end = -1;

        for (int tag: stationTag1) {
            if (place == tag) {
                start = 6;
                end = 10;
                break;
            }
        }
        if(start == -1){
            for (int tag: stationTag2) {
                if (place == tag) {
                    start = 11;
                    end = 15;
                    break;
                }
            }
        }
        if(start == -1){
            QTask task = taskQueue.peek();
            nowTaskNumber = Objects.requireNonNull(task).getTaskNumber();
            InstantStatus.startStation = task.getStartStationId();
            InstantStatus.terminalStation = task.getTerminalStationId();
            InstantStatus.taskProgress = InstantStatus.TaskProgress.PRE_START_STATION;
            return task;
        }

        for (QTask task : taskQueue) {
            Integer startStation = task.getStartStationId();
            if(startStation >= start && startStation <= end){
                nowTaskNumber = task.getTaskNumber();
                InstantStatus.startStation = task.getStartStationId();
                InstantStatus.terminalStation = task.getTerminalStationId();
                InstantStatus.taskProgress = InstantStatus.TaskProgress.PRE_START_STATION;
                return task;
            }
        }

        QTask task = taskQueue.peek();
        nowTaskNumber = Objects.requireNonNull(task).getTaskNumber();
        InstantStatus.startStation = task.getStartStationId();
        InstantStatus.terminalStation = task.getTerminalStationId();
        InstantStatus.taskProgress = InstantStatus.TaskProgress.PRE_START_STATION;
        return task;
    }

    public boolean removeTaskByTaskNumber(String taskNumber) {
        Iterator<QTask> taskIterator = taskQueue.iterator();
        while (taskIterator.hasNext()) {
            QTask task = taskIterator.next();
            if (task.getTaskNumber().equals(taskNumber) && task.getStatus()==0) {
                taskIterator.remove();
                bookedStation[task.getStartStationId()-1] = 0;
                bookedStation[task.getTerminalStationId()-1] = 0;
                return true;
            }
        }
        return false;
    }

    public void failedTask() {
        Iterator<QTask> taskIterator = taskQueue.iterator();
        while (taskIterator.hasNext()) {
            QTask task = taskIterator.next();
            if (task.getTaskNumber().equals(nowTaskNumber)) {
                taskIterator.remove();
                bookedStation[task.getStartStationId()-1] = 0;
                bookedStation[task.getTerminalStationId()-1] = 0;
                nowTaskNumber = "";
            }
        }
        taskDao.cancelTask(nowTaskNumber);
    }

    public void completedTask() {
        taskQueue.removeIf(task -> task.getTaskNumber().equals(nowTaskNumber));
        taskDao.updateTaskStatus(nowTaskNumber, 100);
    }

    public QTask getTaskByTaskNumber(String taskNumber) {
        return taskQueue.stream()
                .filter(task -> task.getTaskNumber().equals(taskNumber))
                .findFirst()
                .orElse(null);
    }

    public void updateTaskStatus(String taskNumber, int status){
        for (QTask task : taskQueue) {
            if (task.getTaskNumber().equals(taskNumber)) {
                task.setStatus(status);
            }
        }
        taskDao.updateTaskStatus(nowTaskNumber, status);
    }

    public String getNowTaskNumber() {
        return nowTaskNumber;
    }

    public void setNowTaskNumber(String taskNumber) {
        nowTaskNumber = taskNumber;
    }

    public int getBookedStationStatusByStation(int station){
        return bookedStation[station-1];
    }

    public void setBookedStation(int station, int status){
        bookedStation[station-1] = status;
    }

    public Collection<QTask> getTaskQueueCopy(){
        return Collections.unmodifiableCollection(taskQueue);
    }

}
