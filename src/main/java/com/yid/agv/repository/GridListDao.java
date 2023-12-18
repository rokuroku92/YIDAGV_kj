package com.yid.agv.repository;

import com.yid.agv.backend.station.Grid;
import com.yid.agv.model.GridList;

import java.util.List;

public interface GridListDao {
    List<GridList> queryAllGrids();
    boolean updateStatus(int stationId, Grid.Status status);
}
