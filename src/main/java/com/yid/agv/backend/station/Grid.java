package com.yid.agv.backend.station;

public class Grid {
    /* status
     * 0 空狀態
     * 1 Booked
     * 2 occupied
     * 3 over time */

    public enum Status{
        FREE(0), BOOKED(1), OCCUPIED(2),
        OVER_TIME(3), DISABLE(6);
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

    public void setStatus(StationStatus.Status status) {
        this.status = status.getValue();
    }
}
