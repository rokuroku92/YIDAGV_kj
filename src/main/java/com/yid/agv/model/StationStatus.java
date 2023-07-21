package com.yid.agv.model;

public class StationStatus {

    /* status
    * 0 無棧板
    * 1 有棧板
    * 2 Booking
    * 3 Error | Booking 後還有棧板 | StartStation 的棧板被拿走
    * 4 完成移轉 */

    private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
