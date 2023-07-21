package com.yid.agv.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Place {
    
    private int id;
    private int[] coordinate; // left, top (x, y) 的資訊
    private static Map<Integer, int[]> map = new LinkedHashMap<>();
    static {
        int deltaX = 13, deltaY = -5;
        map.put(1001, new int[]{1110+deltaX, 95+deltaY});
        map.put(1002, new int[]{1110+deltaX, 175+deltaY});
        map.put(1003, new int[]{1110+deltaX, 230+deltaY});
        map.put(1004, new int[]{900+deltaX, 230+deltaY});
        map.put(1005, new int[]{670+deltaX, 230+deltaY});
        map.put(1006, new int[]{670+deltaX, 175+deltaY});
        map.put(1007, new int[]{670+deltaX, 130+deltaY});
        map.put(1008, new int[]{310+deltaX, 110+deltaY});
        map.put(1009, new int[]{90+deltaX, 80+deltaY});
        map.put(1010, new int[]{90+deltaX, 160+deltaY});
        
        map.put(1501, new int[]{1110+deltaX, 95+deltaY});
        map.put(1502, new int[]{1110+deltaX, 175+deltaY});
        map.put(1503, new int[]{1110+deltaX, 230+deltaY});
        map.put(1504, new int[]{900+deltaX, 230+deltaY});
        map.put(1505, new int[]{670+deltaX, 230+deltaY});
        map.put(1506, new int[]{670+deltaX, 175+deltaY});
        map.put(1507, new int[]{670+deltaX, 130+deltaY});
        map.put(1508, new int[]{310+deltaX, 110+deltaY});
        map.put(1509, new int[]{90+deltaX, 80+deltaY});
        map.put(1510, new int[]{90+deltaX, 160+deltaY});
        
    }
    
    public Place(int id) {
        this.id = id;
        this.coordinate = map.get(id);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int[] getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(int[] coordinate) {
        this.coordinate = coordinate;
    }

    public static Map<Integer, int[]> getMap() {
        return map;
    }

    public static void setMap(Map<Integer, int[]> map) {
        Place.map = map;
    }
    
}