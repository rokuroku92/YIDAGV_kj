package com.yid.agv.backend.station;
import com.yid.agv.repository.GridListDao;
import com.yid.agv.repository.StationDao;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GridManager {
    @Autowired
    private GridListDao gridListDao;
    private final Map<String, Grid> gridMap;

    public GridManager(){
        gridMap = new HashMap<>();
    }

    @PostConstruct
    public void initialize() {
        gridListDao.queryAllGrids().forEach(grid -> gridMap.put(grid.getStation(), new Grid(grid)));
        System.out.println("Initialize gridMap: " + gridMap);
    }


    public int getGirdStationId(String stationName){
        return gridMap.get(stationName).getStationId();
    }

    public Grid.Status getGridStatus(String stationName){
        return gridMap.get(stationName).getStatus();
    }

    public boolean setGridStatus(String gridName, Grid.Status status){
        boolean dbResult = gridListDao.updateStatus(getGirdStationId(gridName), status.getValue());
        if(dbResult){
            gridMap.get(gridName).setStatus(status);
            return true;
        } else {
            return false;
        }
    }


}
