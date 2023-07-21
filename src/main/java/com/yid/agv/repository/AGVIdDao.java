
package com.yid.agv.repository;


import com.yid.agv.model.AGVId;

import java.util.List;
    
public interface AGVIdDao {
    List<AGVId> queryAGVList();
}
