package com.yid.agv.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/agv")
public class AgvController {
    
    @GetMapping(value = "/")
    public String pageAgv() {
        return "agv";
    }
    
    @GetMapping(value = "/analysis")
    public String pageAnalysis() {
        return "analysis";
    }

    @GetMapping(value = "/message")
    public String pageMessage() {
        return "message";
    }


    /*
    static int tempId;
    @GetMapping(value = "/json")
    @ResponseBody
    public String getJson() throws IOException { 
        URL url = new URL("http://192.168.1.143:20100/cars");
        String[] arr;
        try (BufferedReader reader = new BufferedReader
                        (new InputStreamReader(url.openStream()))) {
            String line;
            line = reader.readLine().replace("<br>","");
            arr = line.split(",");
        }
        if(agv != null){
            Place place = new Place(Integer.parseInt(arr[1]));
//            Place place = new Place(10);
            Station station = new Station((tempId+2)%4,(tempId+1)%4,(tempId+0)%4,(tempId+2)%4,(tempId+1)%4,(tempId+3)%4,(tempId+2)%4,(tempId+0)%4,
                (tempId+1)%4,(tempId+0)%4,(tempId+0)%4,(tempId+1)%4,(tempId+3)%4,(tempId+0)%4,(tempId+1)%4);
            agv.setStation(station);
            agv.setPlace(place);
            return new Gson().toJson(agv);
        }
        Place place = new Place(1001+tempId++%10);
        Station station = new Station((tempId+2)%4,(tempId+1)%4,(tempId+0)%4,(tempId+2)%4,(tempId+1)%4,(tempId+3)%4,(tempId+2)%4,(tempId+0)%4,
                (tempId+1)%4,(tempId+0)%4,(tempId+0)%4,(tempId+1)%4,(tempId+3)%4,(tempId+0)%4,(tempId+1)%4);
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task("202301040001",1001,1006,6));
        tasks.add(new Task("202301040002",1007,1002,2));
        tasks.add(new Task("202301040003",1003,1008,8));
        tasks.add(new Task("202301040004",1004,1012,12));
        tasks.add(new Task("202301040005",1013,1005,5));
        agv = new AGV();
        agv.setStatus(1);
        agv.setPlace(place);
        agv.setTask("202301040001");
        agv.setBattery(100);
        agv.setStation(station);
        agv.setTasks(tasks);
        
        String jsonString = new Gson().toJson(agv);
        System.out.println(jsonString);
        return jsonString;
    }
    */
}
