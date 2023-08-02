package com.yid.agv.backend;

import java.util.Optional;
import java.util.Random;

public class TestFakeData {


    public Optional<String[]> crawlAGVStatus() {
        return Optional.of(new String[]{
                ("1,"+Integer.toString(new Random().nextInt(501) + 1000)+",100,100,0,2")
        });
    }
}
