package com.yid.agv.service;

import com.yid.agv.backend.station.Grid;
import com.yid.agv.backend.station.GridManager;
import com.yid.agv.model.GridList;
import com.yid.agv.repository.GridListDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GridService {
    @Autowired
    private GridListDao gridListDao;
    @Autowired
    private GridManager gridManager;

    public List<GridList> getGridsStatus(){
        return gridListDao.queryAllGrids();
    }

    public String setGridStatus(String stationName, Grid.Status status){
        Integer stationId = gridManager.getGirdStationId(stationName);
        if(stationId == null){
            return "無此站點！";
        } else {
            gridListDao.updateStatus(stationId, status);
            return "OK!";
        }
    }
}
