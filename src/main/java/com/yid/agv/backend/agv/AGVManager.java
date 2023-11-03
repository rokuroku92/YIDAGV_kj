package com.yid.agv.backend.agv;


import com.yid.agv.repository.AGVIdDao;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AGVManager {
    @Autowired
    private AGVIdDao agvIdDao;
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

    public int getAgvLength(){
        return agvStatusMap.size();
    }

    public AGV[] getAgvCopyArray(){
        return agvStatusMap.values().toArray(AGV[]::new);
    }

}
