package com.yid.agv.backend;

import com.yid.agv.model.Analysis;
import com.yid.agv.model.QTask;
import com.yid.agv.repository.AnalysisDao;
import com.yid.agv.repository.TaskDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

@Component
public class ProcessTasks {
    @Autowired
    private TaskDao taskDao;
    @Autowired
    private AnalysisDao analysisDao;

    private static final Queue<QTask> taskQueue = new ArrayDeque<>();
    public static int[] bookedStation = new int[15];
    private boolean iAGVIdle_test = true;
    private static String nowTaskNumber;


    @Scheduled(fixedRate = 5000)
    public void dispatchTasks() {
        if (iAGVIdle_test && !taskQueue.isEmpty()) {
//        if (iAGVIdle() && !taskQueue.isEmpty()) {
//            QTask nextTask = taskQueue.poll();
            QTask nextTask = taskQueue.peek();
            dispatchTaskToAGV(nextTask);
            nowTaskNumber = nextTask.getTaskNumber();
            iAGVIdle_test = false;
            // 如果Freddie沒有提供目前AGV正在執行哪個任務，這邊需要一個公用變數給InstantStatus來每秒更新狀態的目前任務、任務進度
        }
    }

    @Scheduled(fixedRate = 20000)
    public void runTasksTest() {
//        if (iAGVIdle() && !taskQueue.isEmpty()) {
        if (!iAGVIdle_test && !taskQueue.isEmpty()) {
            QTask task = taskQueue.poll();
            bookedStation[task.getStartStationId()-1] = 0;
            bookedStation[task.getTerminalStationId()-1] = 4;
            System.out.println("Completed task number "+task.getTaskNumber()+".");
            taskDao.updateTaskStatus(task.getTaskNumber(), 100);
            int analysisId = analysisDao.getTodayAnalysisId().get(task.getAgvId()-1).getAnalysisId();
            analysisDao.updateTask(analysisDao.queryAnalysisesByAnalysisId(analysisId).getTask()+1, analysisId);
            nowTaskNumber = null;
            iAGVIdle_test = true;
        }
    }
    private boolean iAGVIdle(){
        // API查看是否閒置
        return true;
    }

    private void dispatchTaskToAGV(QTask task) {
        if (task != null) {
            // Dispatch the task to the AGV control system
            // 發送api請求
            System.out.println("Task number "+task.getTaskNumber()+" has been dispatched.");
        }
    }

    public static synchronized boolean addTaskToQueue(QTask task) {
        if(taskQueue.size() >= 5)
            return false;
        taskQueue.offer(task);
        bookedStation[task.getStartStationId()-1] = 1;
        bookedStation[task.getTerminalStationId()-1] = 2;
        return true;
    }

    public static synchronized Queue<QTask> getTaskQueue() {
        return taskQueue;
    }

    public static synchronized boolean removeTaskByTaskNumber(String taskNumber) {
        Iterator<QTask> taskIterator = taskQueue.iterator();
        while (taskIterator.hasNext()) {
            QTask task = taskIterator.next();
            if (task.getTaskNumber().equals(taskNumber)) {
                taskIterator.remove();
                bookedStation[task.getStartStationId()-1] = 0;
                bookedStation[task.getTerminalStationId()-1] = 0;
                return true;
            }
        }
        return false;
    }

    public static String getNowTaskNumber() {
        return nowTaskNumber;
    }
}
