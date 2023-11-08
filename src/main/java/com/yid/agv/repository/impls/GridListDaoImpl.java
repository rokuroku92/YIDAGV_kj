package com.yid.agv.repository.impls;

import com.yid.agv.backend.station.Grid;
import com.yid.agv.model.GridList;
import com.yid.agv.repository.GridListDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class GridListDaoImpl implements GridListDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<GridList> queryAllGrids(){
        String sql = "SELECT gl.id, sd.name AS station, gl.status, gl.work_number_1, gl.work_number_2, gl.work_number_3, gl.work_number_4, " +
                "gl.object_name_1, gl.object_name_2, gl.object_name_3, gl.object_name_4, gl.object_number_1, gl.object_number_2, gl.object_number_3, gl.object_number_4, " +
                "gl.create_time FROM grid_list gl INNER JOIN station_data sd ON gl.station_id = sd.id ORDER BY id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(GridList.class));
    }

    @Override
    public boolean updateStatus(int stationId, Grid.Status status){
        String sql = "UPDATE `grid_list` SET `status` = ? WHERE `station_id` = ?";

        int rowsAffected = jdbcTemplate.update(sql, status.getValue(), stationId);
        return (rowsAffected > 0);
    }

    @Override
    public boolean updateWorkOrder(int stationId, String createTime){
        String sql = "UPDATE `grid_list` SET `create_time` = ? WHERE `station_id` = ?";

        int rowsAffected = jdbcTemplate.update(sql, createTime, stationId);
        return (rowsAffected > 0);
    }

    @Override
    public boolean updateWorkOrder(int stationId, String createTime, String workNumber, String objectName, String objectNumber){
        String sql = "UPDATE `grid_list` SET `work_number_1` = ?, `object_name_1` = ?, `object_number_1` = ?, `create_time` = ? WHERE `station_id` = ?";

        int rowsAffected = jdbcTemplate.update(sql, workNumber, objectName, objectNumber, createTime, stationId);
        return (rowsAffected > 0);
    }

    @Override
    public boolean updateWorkOrder(int stationId, String createTime, String workNumber1, String workNumber2, String objectName1,
                            String objectName2, String objectNumber1, String objectNumber2){
        String sql = "UPDATE `grid_list` SET `work_number_1` = ?, `work_number_2` = ?, `object_name_1` = ?, `object_name_2` = ?, " +
                "`object_number_1` = ?, `object_number_2` = ?, `create_time` = ? WHERE `station_id` = ?";

        int rowsAffected = jdbcTemplate.update(sql, workNumber1, workNumber2, objectName1, objectName2, objectNumber1, objectNumber2, createTime, stationId);
        return (rowsAffected > 0);
    }

    @Override
    public boolean updateWorkOrder(int stationId, String createTime, String workNumber1, String workNumber2, String workNumber3,
                            String objectName1, String objectName2, String objectName3, String objectNumber1,
                            String objectNumber2, String objectNumber3){
        String sql = "UPDATE `grid_list` SET `work_number_1` = ?, `work_number_2` = ?, `work_number_3` = ?, `object_name_1` = ?, `object_name_2` = ?, " +
                "`object_name_3` = ?, `object_number_1` = ?, `object_number_2` = ?, `object_number_3` = ?, `create_time` = ? WHERE `station_id` = ?";

        int rowsAffected = jdbcTemplate.update(sql, workNumber1, workNumber2, workNumber3, objectName1, objectName2, objectName3, objectNumber1,
                objectNumber2, objectNumber3, createTime, stationId);
        return (rowsAffected > 0);
    }

    @Override
    public boolean updateWorkOrder(int stationId, String createTime, String workNumber1, String workNumber2, String workNumber3,
                            String workNumber4, String objectName1, String objectName2, String objectName3,
                            String objectName4, String objectNumber1, String objectNumber2, String objectNumber3,
                            String objectNumber4){
        String sql = "UPDATE `grid_list` SET `work_number_1` = ?, `work_number_2` = ?, `work_number_3` = ?, `work_number_4` = ?, " +
                "`object_name_1` = ?, `object_name_2` = ?, `object_name_3` = ?, `object_name_4` = ?, `object_number_1` = ?, " +
                "`object_number_2` = ?, `object_number_3` = ?, `object_number_4` = ?, `create_time` = ? WHERE `station_id` = ?";

        int rowsAffected = jdbcTemplate.update(sql, workNumber1, workNumber2, workNumber3, workNumber4, objectName1, objectName2,
                objectName3, objectName4, objectNumber1, objectNumber2, objectNumber3, objectNumber4, createTime, stationId);
        return (rowsAffected > 0);
    }

    @Override
    public boolean clearWorkOrder(int stationId){
        String sql = "UPDATE `grid_list` SET `work_number_1` = null, `work_number_2` = null, `work_number_3` = null, `work_number_4` = null, " +
                "`object_name_1` = null, `object_name_2` = null, `object_name_3` = null, `object_name_4` = null, `object_number_1` = null, " +
                "`object_number_2` = null, `object_number_3` = null, `object_number_4` = null, `create_time` = null WHERE `station_id` = ?";

        int rowsAffected = jdbcTemplate.update(sql, stationId);
        return (rowsAffected > 0);
    }
}
