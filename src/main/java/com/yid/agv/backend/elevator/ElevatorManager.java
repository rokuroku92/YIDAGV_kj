package com.yid.agv.backend.elevator;

import com.yid.agv.model.Station;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
public class ElevatorManager {
    @Value("${agvControl.url}")
    private String agvUrl;
    @Value("${elevator.pre_person_open_door_duration}")
    private int prePersonOpenDoorDuration;
    private ElevatorPermission elevatorPermission;

    private int prePersonOpenDoorCount;
    private int elevatorPersonCount;
    private boolean iOpenDoor;
    private boolean iPersonOccupancyButton;
    private boolean iObstacle;
    private boolean iAlarmObstacle;
    private final int[] lastCallerStatus;

    private final Queue<Integer> callQueue;
    public ElevatorManager() {
        callQueue = new ConcurrentLinkedDeque<>();
        lastCallerStatus = new int[6];
    }

    @PostConstruct
    public void initialize() {
        Arrays.fill(lastCallerStatus, -1);
        prePersonOpenDoorCount = 0;
        elevatorPersonCount = 0;
    }

    @Scheduled(fixedRate = 1000)
    public void elevatorProcess() {
        String[] statusData = crawlStatus().orElse(new String[0]);
        int[] callerStatus = new int[6];
        Arrays.fill(callerStatus, 0);

        String[] elevatorData = statusData[0].split(",");
        // 假設開門
        iOpenDoor = elevatorData[1].equals("1");
        // 假設人員按鈕被按下
        iPersonOccupancyButton = elevatorData[2].equals("1");
        // 假設電梯有障礙
        iObstacle = elevatorData[3].equals("1");

        for(int i = 1; i <= statusData.length; i++){
            String[] data = statusData[i].split(",");  // 分隔資料
            if (data[0].equals("-1")){
                callerStatus[i] = -1;
                continue;
            }
            // 假設呼叫按鈕按下
            if (data[1].equals("1")){
                int finalI = i;
                AtomicBoolean had = new AtomicBoolean(false);
                callQueue.forEach(floor -> {
                    if(floor == finalI){
                        had.set(true);
                    }
                });
                if(!had.get()){
                    callQueue.offer(i);
                }
                callerStatus[i] = 2;  // TODO: 假設 2 是閃黃燈
            }
        }

        switch (elevatorPermission){
            case SYSTEM -> {
                return;
            }
            case PRE_PERSON -> {
                if (iOpenDoor){
                    if (!iPersonOccupancyButton){
                        prePersonOpenDoorCount++;
                        if(prePersonOpenDoorCount > prePersonOpenDoorDuration){
                            Integer floor = callQueue.poll();
                            // TODO: clear callerButton
                            controlElevatorDoor(floor, false);
                            elevatorPermission = ElevatorPermission.FREE;
                            prePersonOpenDoorCount = 0;
                        }
                    } else {
                        Integer floor = callQueue.poll();
                        // TODO: clear callerButton
                        controlElevatorDoor(floor, false);
                        prePersonOpenDoorCount = 0;
                        elevatorPermission = ElevatorPermission.PERSON;
                    }
                }
            }
            case PERSON -> {
                prePersonOpenDoorCount = 0;
                elevatorPersonCount++;
                if (!iPersonOccupancyButton){
                    elevatorPersonCount = 0;
                    // TODO: close Door
                    elevatorPermission = ElevatorPermission.FREE;
                }
            }
            case FREE -> {
                elevatorPersonCount = 0;
                if (!callQueue.isEmpty()){
                    Integer floor = callQueue.peek();
                    if (controlElevatorDoor(floor, true)){
                        callerStatus[floor-1] = 3;  // TODO: 假設黃色恆亮是3
                        elevatorPermission = ElevatorPermission.PRE_PERSON;
                    }
                }
            }
        }

        for (int i = 0; i < callerStatus.length; i++) {
            if(callerStatus[i] == -1){
                lastCallerStatus[i] = -1;
                continue;
            }
            switch (elevatorPermission){
                case FREE,PRE_PERSON,PERSON -> {
                    if(callerStatus[i] == 3){
                        sendCaller(i+1, 6);  // TODO: 假設黃色、綠色燈號恆亮是6
                    } else if (callerStatus[i] == 2){
                        sendCaller(i+1, 5);  // TODO: 假設綠色燈號恆亮、黃色閃爍是5
                    } else {
                        sendCaller(i+1, 4);  // TODO: 假設綠色燈號恆亮4
                    }
                }
                case SYSTEM -> {
                    if (callerStatus[i] == 2){
                        sendCaller(i+1, 9);  // TODO: 假設紅色燈號恆亮、黃色閃爍是5
                    } else {
                        sendCaller(i+1, 8);  // TODO: 假設紅燈號恆亮4
                    }
                }

            }
        }

    }

    public ElevatorPermission getElevatorPermission() {
        return elevatorPermission;
    }

    public boolean getIOpenDoor(){
        return iOpenDoor;
    }

    public boolean acquireElevatorPermission() {
        if (elevatorPermission == ElevatorPermission.SYSTEM){
            return true;
        } else if (elevatorPermission == ElevatorPermission.PRE_PERSON || elevatorPermission == ElevatorPermission.PERSON || callQueue.size() > 0){
            return false;
        } else if (iObstacle){
            iAlarmObstacle = true;
            return false;
        } else {
            iAlarmObstacle = false;
            elevatorPermission = ElevatorPermission.SYSTEM;
            return true;
        }

    }

    public void resetElevatorPermission() {
        elevatorPermission = ElevatorPermission.FREE;
    }

    public int getElevatorPersonCount(){
        return elevatorPersonCount;
    }

    public boolean getIAlarmObstacle(){
        return iAlarmObstacle;
    }

    public Optional<String[]> crawlStatus() {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(agvUrl + "/callers"))  // TODO: fix
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String webpageContent = response.body();
            String[] data = Arrays.stream(webpageContent.split("<br>"))
                    .map(String::trim)
                    .toArray(String[]::new);
            return Optional.of(data);
        } catch (IOException | InterruptedException ignored) {
        }

        return Optional.empty();
    }

    public boolean controlElevatorDoor(int floor, boolean iOpen){
        String cmd = iOpen ? "J0130" : "J0132";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(agvUrl + "/cmd=" + floor + "&Q" + cmd + "X"))  // TODO: fix
                .GET()
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            String webpageContent = response.body();
            return webpageContent.trim().equals("OK");
        } catch (IOException | InterruptedException ignored) {
            return false;
        }
    }


    public void sendCaller(int id, int value){
        if(lastCallerStatus[id-1] != value) {
            System.out.println("Id: " + id + "  Value: " + value);
            lastCallerStatus[id-1] = value;
            HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("http://192.168.0.100:20100/caller=" + id + "&" + value + "&output"))
                    .uri(URI.create(agvUrl + "/caller=" + id + "&" + value + "&output"))
                    .GET()
                    .build();
            try {
                HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
