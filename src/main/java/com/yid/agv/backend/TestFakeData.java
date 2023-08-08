package com.yid.agv.backend;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;

@Component
public class TestFakeData {

    public Optional<String[]> crawlAGVStatus() {
        Random random = new Random();
        String[] data = new String[4];
        for (int i = 0; i < data.length; i++) {
            data[i] = "1,".concat(Integer.toString(random.nextInt(299)+1000)).concat(",")
                    .concat(Integer.toString(random.nextInt(101))).concat(",")
                    .concat(Integer.toString(random.nextInt(101))).concat(",0,2");
        }
        return Optional.of(data);
    }

    public Optional<String[]> crawlStationStatus() {
        Random random = new Random();
        String[] data = new String[10];
        for (int i = 0; i < data.length; i++) {
            if(i%2==0)
                data[i] = "1";
            else
                data[i] = "0";
//            data[i] = Integer.toString(random.nextInt(2));
        }
        return Optional.of(data);
    }

}
