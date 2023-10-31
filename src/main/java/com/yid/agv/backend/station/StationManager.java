package com.yid.agv.backend.station;
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
    private final Map<Integer, Grid> stationStatusMap;

    public StationManager(){
        stationStatusMap = new HashMap<>();
    }

    @PostConstruct
    public void initialize() {
        stationDao.queryStations().forEach(station -> stationStatusMap.put(station.getId(), new Grid()));
        System.out.println("Initialize stationStatusMap: "+stationStatusMap);
    }

    public Grid getStationStatus(int stationId){
        return stationStatusMap.get(stationId);
    }

    public StationStatus[] getStationStatusCopyArray(){
        return stationStatusMap.values().toArray(StationStatus[]::new);
    }
}
