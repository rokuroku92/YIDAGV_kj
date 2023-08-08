package com.yid.agv.backend.datastorage;


import com.yid.agv.model.AgvStatus;
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
    private final Map<Integer, AgvStatus> agvStatusMap;

    private AGVManager(){
        agvStatusMap = new HashMap<>();
    }

    @SuppressWarnings("unused")
    private static class Holder{
        private static final AGVManager INSTANCE = new AGVManager();
    }

    @PostConstruct
    public void _init() {
        agvIdDao.queryAGVList().forEach(agvId -> agvStatusMap.put(agvId.getId(), new AgvStatus()));
        System.out.println("Initialize agvStatusMap: "+agvStatusMap);
    }

    public AgvStatus getAgvStatus(int agvId){
        return agvStatusMap.get(agvId);
    }

    public int getAgvLength(){
        return agvStatusMap.size();
    }

    public AgvStatus[] getAgvStatusCopyArray(){
        return agvStatusMap.values().toArray(AgvStatus[]::new);
    }

}
