package com.yid.agv.repository.impls;

import com.yid.agv.backend.station.Grid;
import com.yid.agv.model.GridList;
import com.yid.agv.repository.GridListDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GridListDaoImpl implements GridListDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<GridList> queryAllGrids(){
        String sql = "SELECT gl.id, sd.name AS station, gl.status, gl.create_time FROM grid_list gl INNER JOIN station_data sd ON gl.station_id = sd.id ORDER BY id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(GridList.class));
    }

    @Override
    public boolean updateStatus(int stationId, Grid.Status status){
        String sql = "UPDATE `grid_list` SET `status` = ? WHERE `station_id` = ?";

        int rowsAffected = jdbcTemplate.update(sql, status.getValue(), stationId);
        return (rowsAffected > 0);
    }
}
