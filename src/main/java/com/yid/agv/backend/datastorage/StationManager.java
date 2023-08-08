package com.yid.agv.backend.datastorage;
import com.yid.agv.model.StationStatus;
import com.yid.agv.repository.StationDao;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StationManager {
    @Autowired
    private StationDao stationDao;
    private final Map<Integer, StationStatus> stationStatusMap;

    private StationManager(){
        stationStatusMap = new HashMap<>();
    }

    @SuppressWarnings("unused")
    private static class Holder{
        private static final StationManager INSTANCE = new StationManager();
    }

    @PostConstruct
    public void _init() {
        stationDao.queryStations().forEach(station -> stationStatusMap.put(station.getId(), new StationStatus()));
        System.out.println("Initialize stationStatusMap: "+stationStatusMap);
    }

    public StationStatus getStationStatus(int stationId){
        return stationStatusMap.get(stationId);
    }

    public StationStatus[] getStationStatusCopyArray(){
        return stationStatusMap.values().toArray(StationStatus[]::new);
    }
}
