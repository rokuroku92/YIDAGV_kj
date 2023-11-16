package com.yid.agv.backend.agvtask;


import com.yid.agv.backend.station.GridManager;
import com.yid.agv.model.TaskList;
import com.yid.agv.repository.AGVIdDao;
import com.yid.agv.repository.TaskListDao;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class AGVTaskManager {
    @Autowired
    private AGVIdDao agvIdDao;
    @Autowired
    private TaskListDao taskListDao;

    @Autowired
    private GridManager gridManager;
    private final Map<Integer, Queue<AGVQTask>> taskQueueMap;

    public AGVTaskManager() {
        taskQueueMap = new HashMap<>();
    }
    @PostConstruct
    public void initialize() {
        agvIdDao.queryAGVList().forEach(agvId -> taskQueueMap.put(agvId.getId(), new ConcurrentLinkedDeque<>()));
    }

    @Scheduled(fixedRate = 5000)
    public synchronized void refreshTaskList() {
        // 取得未完成任務
        List<TaskList> uncompletedTasks = taskListDao.queryUncompletedTaskLists();

        // 遍歷各個 AGV 的任務佇列
        taskQueueMap.forEach((agvId, agvQueue) -> {
            // 遍歷未完成任務列表
            uncompletedTasks.forEach(taskList -> {
                // 如果AGV任務佇列中的任務號碼不包含這筆未完成任務列表元素中的任務號碼
                if (agvQueue.stream().noneMatch(task -> task.getTaskNumber().equals(taskList.getTaskNumber()))) {
                    AGVQTask newTask = new AGVQTask();
                    newTask.setTaskNumber(taskList.getTaskNumber());
                    newTask.setAgvId(agvId);
                    newTask.setStartStationId(taskList.getStartId());
                    newTask.setTerminalStationId(taskList.getTerminalId());
                    newTask.setModeId(taskList.getModeId());
                    newTask.setStatus(0);
                    agvQueue.offer(newTask);
                }
            });
        });
    }

    public Queue<AGVQTask> getTaskQueue(int agvId){
        return taskQueueMap.get(agvId);
    }

    public void forceClearTaskQueueByAGVId(int agvId){
        taskQueueMap.put(agvId, new ConcurrentLinkedDeque<>());
    }

    public AGVQTask getNewTaskByAGVId(int agvId){
        return taskQueueMap.get(agvId).poll();
    }

    public boolean isEmpty(int agvId){
        return taskQueueMap.get(agvId).isEmpty();
    }

//    public boolean addTaskToQueue(AGVQTask task);  // 這個專案AGV任務佇列的取得方式靠資料庫
//    public Integer getTerminalByNotification(String notificationId);  // 這個專案不用自動選擇終點站
//    public QTask peekTaskWithPlace();  // 這個專案不用優先派遣演算法

    public boolean removeTaskByTaskNumber(String taskNumber) {
        for (Queue<AGVQTask> taskQueue : taskQueueMap.values()) {
            Iterator<AGVQTask> taskIterator = taskQueue.iterator();
            while (taskIterator.hasNext()) {
                AGVQTask task = taskIterator.next();
                if (task.getTaskNumber().equals(taskNumber) && task.getStatus() == 0) {
                    taskIterator.remove();
                    taskListDao.cancelTaskList(taskNumber);
                    return true;
                }
            }
        }
        return false;
    }

    public Collection<AGVQTask> getTaskQueueCopy() {
        List<AGVQTask> allTasks = new ArrayList<>();

        taskQueueMap.values().forEach(taskQueue -> {
            synchronized (taskQueue) {
                allTasks.addAll(taskQueue);
            }
        });

        allTasks.sort(Comparator.comparing(  // #NE202311050001
                AGVQTask::getTaskNumber,
                Comparator.<String, String>comparing(
                                taskNumber -> taskNumber.substring(3, 11),  // 照日期排序
                                Comparator.naturalOrder()
                        )
                        .thenComparing(
                                taskNumber -> taskNumber.substring(11),  // 照流水號排序
                                Comparator.naturalOrder()
                        )
        ));

//        allTasks.sort(Comparator.comparing(AGVQTask::getTaskNumber,
//                Comparator.comparing(taskNumber -> taskNumber.substring(3, 11))
//                        .thenComparing(taskNumber -> taskNumber.substring(11))));

        return Collections.unmodifiableCollection(allTasks);
    }

}
