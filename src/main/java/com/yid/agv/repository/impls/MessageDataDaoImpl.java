package com.yid.agv.repository.impls;

import com.yid.agv.model.MessageData;
import com.yid.agv.repository.MessageDataDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageDataDaoImpl implements MessageDataDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<MessageData> queryMessageData(){
        String sql = "SELECT * FROM `notification_history_message_data`";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MessageData.class));
    }
}
