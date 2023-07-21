
package com.yid.agv.repository.impls;

import com.yid.agv.model.Mode;
import com.yid.agv.repository.ModeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ModeDaoImpl implements ModeDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Override
    public List<Mode> queryModes(){
        String sql = "SELECT * FROM `mode_data`";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Mode.class));
    }
}
