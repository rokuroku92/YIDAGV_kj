
package com.yid.agv.repository.impls;

import com.yid.agv.model.NotificationStation;
import com.yid.agv.model.Station;
import com.yid.agv.repository.StationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StationDaoImpl implements StationDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Override
    public List<Station> queryStations(){
        String sql = "SELECT * FROM `station_data`";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Station.class));
    }
    @Override
    public List<String> queryStandbyTags(){
        String sql = "SELECT `tag` FROM `station_data` where `name` LIKE '%-10'";
        return jdbcTemplate.queryForList(sql, String.class);
    }
    @Override
    public List<NotificationStation> queryNotificationStations(){
        String sql = "SELECT * FROM `notification_station_data`";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(NotificationStation.class));
    }
}
