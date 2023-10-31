package com.yid.agv.repository;

import com.yid.agv.model.NowTaskList;

import java.util.List;

public interface NowTaskListDao {


    List<NowTaskList> queryNowTaskLists();

    boolean updateNowTaskListPhase(String taskNumber, Phase phase);

    boolean updateNowTaskListProgress(String taskNumber, int progress);

    boolean deleteNowTaskList(String taskNumber);
}
