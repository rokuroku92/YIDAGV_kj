package com.yid.agv.backend;

import com.yid.agv.model.QTask;
import com.yid.agv.model.Station;
import com.yid.agv.repository.AnalysisDao;
import com.yid.agv.repository.NotificationDao;
import com.yid.agv.repository.StationDao;
import com.yid.agv.repository.TaskDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

@Component
public class ProcessTasks {
    @Value("${agvControl.url}")
    private static String agvUrl;
    @Autowired
    private TaskDao taskDao;
    @Autowired
    private StationDao stationDao;
    @Autowired
    private AnalysisDao analysisDao;
    @Autowired
    private NotificationDao notificationDao;

    private static Map<Integer, Integer> stationIdTagMap;
    private static final Queue<QTask> taskQueue = new ArrayDeque<>();
    public static int[] bookedStation = new int[15];
    private boolean iAGVIdle_test = true;
    private static String nowTaskNumber;
    public static int redispatch = 0;



    @Scheduled(fixedRate = 5000)
    public void dispatchTasks() {
        if (stationIdTagMap == null){
            stationIdTagMap = stationDao.queryStations().stream().
                    collect(Collectors.toMap(Station::getId, Station::getTag));
        }
        if (!InstantStatus.iTask && !taskQueue.isEmpty()) {
            InstantStatus.iStandby = false;
            QTask goTask = peekTaskWithPlace() != null ? peekTaskWithPlace():taskQueue.peek();
            if(redispatch<3){
                if(dispatchTaskToAGV(goTask)){
                    nowTaskNumber = goTask.getTaskNumber();
                    goTask.setStatus(1);
                    taskDao.updateTaskStatus(nowTaskNumber, 1);
                    redispatch = 4;
                }else {
                    System.out.println("發送任務失敗5秒後重新發送");
                    notificationDao.insertMessage(1, 18);
                    redispatch++;
                }
            } else if (redispatch == 3) {
                System.out.println("發送任務三次皆失敗，已取消任務");
                notificationDao.insertMessage(1, 19);
                taskDao.cancelTask(nowTaskNumber);
                removeTaskByTaskNumber(nowTaskNumber);
                nowTaskNumber=null;
                redispatch = 0;
            }

        }
//        if (iAGVIdle_test && !taskQueue.isEmpty()) {
// //        if (iAGVIdle() && !taskQueue.isEmpty()) {
// //           QTask nextTask = taskQueue.poll();
//            QTask nextTask = taskQueue.peek();
//            dispatchTaskToAGV(nextTask);
//            nowTaskNumber = nextTask.getTaskNumber();
//            iAGVIdle_test = false;
//            // TODO: 如果Freddie沒有提供目前AGV正在執行哪個任務，這邊需要一個公用變數給InstantStatus來每秒更新狀態的目前任務、任務進度(目前已實作在nowTaskNumber)
//        }
    }

//    @Scheduled(fixedRate = 20000)
//    public void runTasksTest() {
////        if (iAGVIdle() && !taskQueue.isEmpty()) {
//        if (InstantStatus.iTask && !taskQueue.isEmpty()) {
//            QTask task = taskQueue.poll();
//            bookedStation[task.getStartStationId()-1] = 0;
//            bookedStation[task.getTerminalStationId()-1] = 4;
//            System.out.println("Completed task number "+task.getTaskNumber()+".");
//            taskDao.updateTaskStatus(task.getTaskNumber(), 100);
//            int analysisId = analysisDao.getTodayAnalysisId().get(task.getAgvId()-1).getAnalysisId();
//            analysisDao.updateTask(analysisDao.queryAnalysisesByAnalysisId(analysisId).getTask()+1, analysisId);
//            nowTaskNumber = null;
//            iAGVIdle_test = true;
//        }
//        // TODO: 不需要假資料後將此業務邏輯修改並放在InstantStatus的任務執行完成內。
//    }
//    private boolean iAGVIdle(){
//        // TODO: API查看是否閒置
//        return true;
//    }

    public static synchronized boolean dispatchTaskToAGV(QTask task) {
        if (task != null) {
            // TODO: Dispatch the task to the AGV control system
            String url;
            url = agvUrl + "/task0=" + task.getAgvId() + "&" + task.getModeId() + "&" + InstantStatus.agvStatuses[0].getPlace() +
                    "&" + stationIdTagMap.get(task.getStartStationId()) + "&" + stationIdTagMap.get(task.getTerminalStationId());
            // TODO: 看有沒有需要分成不一樣站點數的網址
//            switch (task.getModeId()) {
//                case 1 -> url = agvUrl + "/task0=" + task.getAgvId() + "&" + task.getModeId() + "&" + InstantStatus.agvStatuses[0].getPlace() +
//                                    "&" + stationIdTagMap.get(task.getStartStationId()) + "&" + stationIdTagMap.get(task.getTerminalStationId());
//                case 2 -> url = agvUrl + "/task0=" + task.getAgvId() + "&" + task.getModeId() + "&" + InstantStatus.agvStatuses[0].getPlace() +
//                                    "&" + stationIdTagMap.get(task.getStartStationId()) + "&" + stationIdTagMap.get(task.getTerminalStationId());
//            }
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String webpageContent = response.body().trim();
                System.out.println("Task number "+task.getTaskNumber()+" has been dispatched.");
                return webpageContent.equals("OK");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
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

    public static synchronized QTask peekTaskWithPlace() {
        int place = Integer.parseInt(InstantStatus.agvStatuses[0].getPlace()); // 只有一台車所以是0
        int start, end;
        if (place >= 1001 && place <= 1050){
            start = 1;end = 5;
        } else if (place > 1050 && place <= 1100) {
            start = 6;end = 10;
        } else if (place > 1100 && place <= 1150) {
            start = 11;end = 15;
        }else return null;

        for (QTask task : taskQueue) {
            Integer startStation = task.getStartStationId();
            if(startStation >= start && startStation <= end)return task;
        }
        return null;
    }


    public static synchronized boolean removeTaskByTaskNumber(String taskNumber) {
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

    public static synchronized void pollTaskByTaskNumber(String taskNumber) {
        taskQueue.removeIf(task -> task.getTaskNumber().equals(taskNumber));
    }

    public static synchronized QTask getTaskByTaskNumber(String taskNumber) {
        return taskQueue.stream()
                .filter(task -> task.getTaskNumber().equals(taskNumber))
                .findFirst()
                .orElse(null);
    }

    public static String getNowTaskNumber() {
        return nowTaskNumber;
    }

    public static void setNowTaskNumber(String taskNumber) {
        nowTaskNumber = taskNumber;
    }
}