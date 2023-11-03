
package com.yid.agv.repository;


import com.yid.agv.model.Station;

import java.util.List;

public interface StationDao {
    List<Station> queryStations();
    List<Station> queryStandbyStations();
}
