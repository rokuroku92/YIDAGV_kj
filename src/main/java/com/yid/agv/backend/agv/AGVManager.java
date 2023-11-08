package com.yid.agv.backend.agv;


import com.yid.agv.repository.AGVIdDao;
import com.yid.agv.repository.StationDao;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AGVManager {
    @Autowired
    private AGVIdDao agvIdDao;
    @Autowired
    private StationDao stationDao;
    private final Map<Integer, AGV> agvStatusMap;

    public AGVManager(){
        agvStatusMap = new HashMap<>();
    }

    @PostConstruct
    public void initialize() {
        agvIdDao.queryAGVList().forEach(agvId -> agvStatusMap.put(agvId.getId(), new AGV(agvId.getId())));
        System.out.println("Initialize agvStatusMap: "+agvStatusMap);
    }

    public int getAgvSize(){
        return agvStatusMap.size();
    }

    public AGV getAgv(int agvId){
        return agvStatusMap.get(agvId);
    }

    public boolean iAgvInElevator(int agvId){
        String place = agvStatusMap.get(agvId).getPlace();
        List<String> tags = stationDao.getStationTagByAreaName("E-");
        for (String tag: tags) {
            if (place.equals(tag)){
                return true;
            }
        }
        return false;
    }

    public int getAgvLength(){
        return agvStatusMap.size();
    }

    public AGV[] getAgvCopyArray(){
        return agvStatusMap.values().toArray(AGV[]::new);
    }

}
