package com.yid.agv.backend.station;

public class StationStatus {

    /* status
    * 0 無棧板
    * 1 有棧板
    * 2 Booking
    * 3 Error | Booking 後還有棧板 | StartStation 的棧板被拿走
    * 4 完成移轉 */

    public enum Status{
        NOT_OWN_PALLET(0), OWN_PALLET(1), BOOKING(2),
        UNEXPECTED_PALLET(3), COMPLETED(4), DISABLE(6);
        private final int value;
        Status(int value) {
            this.value = value;
        }

        public int getValue(){
            return value;
        }

    }

    private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status.getValue();
    }
}
