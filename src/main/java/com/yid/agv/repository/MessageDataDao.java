package com.yid.agv.repository;

import com.yid.agv.model.MessageData;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import java.util.List;

public interface MessageDataDao {
    List<MessageData> queryMessageData();
}
