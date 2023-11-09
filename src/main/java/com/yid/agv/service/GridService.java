package com.yid.agv.service;

import com.yid.agv.model.GridList;
import com.yid.agv.repository.GridListDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GridService {
    @Autowired
    private GridListDao gridListDao;

    public List<GridList> getGridsStatus(){
        return gridListDao.queryAllGrids();
    }
}
