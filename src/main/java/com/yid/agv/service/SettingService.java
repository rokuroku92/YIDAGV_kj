package com.yid.agv.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.yid.agv.dto.SettingRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class SettingService {
    @Value("classpath:application.yml") // 指定 YAML 文件路径，根据实际情况修改
    private Resource yamlFile;


    public Map<String, Object> getConfig() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        // 读取 YAML 文件并转换为 Map
        return (Map<String, Object>) objectMapper.readValue(yamlFile.getInputStream(), Map.class);
    }

    public String updateConfig(SettingRequest settingRequest){
        if (settingRequest != null) {
            if(settingRequest.getAgvControlUrl() == null){
                return "AgvControlUrl參數為空值";
            } else if(settingRequest.getAgvLowBattery() == null){
                return "AgvLowBattery參數為空值";
            } else if(settingRequest.getAgvLowBatteryDuration() == null){
                return "AgvLowBatteryDuration參數為空值";
            } else if(settingRequest.getAgvObstacleDuration() == null){
                return "AgvObstacleDuration參數為空值";
            } else if(settingRequest.getHttpTimeout() == null){
                return "HttpTimeout參數為空值";
            } else if(settingRequest.getHttpMaxRetry() == null){
                return "HttpMaxRetry參數為空值";
            }
            return writeConfigToFile(settingRequest);
        } else {
            return "設置參數為空值";
        }
    }

    private String writeConfigToFile(SettingRequest settingRequest){
        Map<String, Object> yamlMap = new HashMap<>();
        yamlMap.put("jdbc.url", "jdbc:mysql://localhost:3306/AGV_kj?zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Taipei&characterEncoding=utf-8&useUnicode=true");
        yamlMap.put("jdbc.username", "root");
        yamlMap.put("jdbc.password", "12345678");
        yamlMap.put("agvControl.url", settingRequest.getAgvControlUrl());
        yamlMap.put("agv.low_battery", settingRequest.getAgvLowBattery());
        yamlMap.put("agv.low_battery_duration", settingRequest.getAgvLowBatteryDuration());
        yamlMap.put("agv.obstacle_duration", settingRequest.getAgvObstacleDuration());
        yamlMap.put("http.timeout", settingRequest.getHttpTimeout());
        yamlMap.put("http.max_retry", settingRequest.getHttpMaxRetry());


        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter("src/main/resources/application.yml")) {
            yaml.dump(yamlMap, writer);
            return "OK";
        } catch (IOException e) {
            e.printStackTrace();
            return "寫入失敗";
        }
    }
}
