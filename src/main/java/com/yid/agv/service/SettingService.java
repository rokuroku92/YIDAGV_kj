package com.yid.agv.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.yid.agv.dto.SettingRequest;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Service
public class SettingService {
    private static final Logger log = LoggerFactory.getLogger(SettingService.class);
    private static final String YAML_FILE_NAME = File.separator + "application.yml";
    private static String CONFIG_FILE_PATH;

    @PostConstruct
    public void init() {
        File source = new ApplicationHome(SettingService.class).getSource();
        if (source != null) {
            CONFIG_FILE_PATH = source.getParentFile().toString() + YAML_FILE_NAME;
        } else {
            log.error("Failed to load application.yml");
            CONFIG_FILE_PATH = System.getProperty("user.dir") + YAML_FILE_NAME;
        }
        log.info("Config file path: " + CONFIG_FILE_PATH);
    }

    public Map<String, Object> getConfig() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        File configFile = new File(CONFIG_FILE_PATH);
        return (Map<String, Object>) objectMapper.readValue(new FileInputStream(configFile), Map.class);
    }

    public String updateConfig(SettingRequest settingRequest) {
        if (settingRequest != null) {
            if (settingRequest.getAgvControlUrl() == null) {
                return "AgvControlUrl參數為空值";
            } else if (settingRequest.getAgvLowBattery() == null) {
                return "AgvLowBattery參數為空值";
            } else if (settingRequest.getAgvLowBatteryDuration() == null) {
                return "AgvLowBatteryDuration參數為空值";
            } else if (settingRequest.getAgvObstacleDuration() == null) {
                return "AgvObstacleDuration參數為空值";
            } else if (settingRequest.getAgvTaskExceptionOption() == null) {
                return "AgvTaskExceptionOption參數為空值";
            } else if (settingRequest.getHttpTimeout() == null) {
                return "HttpTimeout參數為空值";
            } else if (settingRequest.getHttpMaxRetry() == null) {
                return "HttpMaxRetry參數為空值";
            }
            return writeConfigToFile(settingRequest);
        } else {
            return "設置參數為空值";
        }
    }

    private String writeConfigToFile(SettingRequest settingRequest) {
        try {
            // 創建臨時文件
            File tempFile = File.createTempFile("application", ".yml");

            Yaml yaml = new Yaml();
            FileInputStream fileInputStream = new FileInputStream(CONFIG_FILE_PATH);
            Map<String, Object> yamlMap = yaml.load(fileInputStream);

            // 更新配置
            Map<String, Object> jdbcMap = (Map<String, Object>) yamlMap.get("jdbc");
            jdbcMap.put("url", "jdbc:mysql://localhost:3306/AGV_kj?zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Taipei&characterEncoding=utf-8&useUnicode=true");
            jdbcMap.put("username", "root");
            jdbcMap.put("password", "12345678");
            Map<String, Object> agvControlMap = (Map<String, Object>) yamlMap.get("agvControl");
            agvControlMap.put("url", settingRequest.getAgvControlUrl());
            Map<String, Object> agvMap = (Map<String, Object>) yamlMap.get("agv");
            agvMap.put("low_battery", settingRequest.getAgvLowBattery());
            agvMap.put("low_battery_duration", settingRequest.getAgvLowBatteryDuration());
            agvMap.put("obstacle_duration", settingRequest.getAgvObstacleDuration());
            agvMap.put("task_exception_option", settingRequest.getAgvTaskExceptionOption());
            Map<String, Object> httpMap = (Map<String, Object>) yamlMap.get("http");
            httpMap.put("timeout", settingRequest.getHttpTimeout());
            httpMap.put("max_retry", settingRequest.getHttpMaxRetry());

            // 將配置寫入臨時文件
            try (Writer writer = new FileWriter(tempFile)) {
                writer.write(yaml.dumpAsMap(yamlMap));
                writer.flush();
            } catch (IOException e) {
                log.error("Failed to write config file: " + CONFIG_FILE_PATH, e);
                return "寫入失敗";
            } finally {
                fileInputStream.close();
            }

            // 將臨時文件複製或移動到目標位置
            Path source = tempFile.toPath();
            Path target = Paths.get(CONFIG_FILE_PATH);
            log.info("tempFile: " + source);
            log.info("CONFIG_FILE_PATH: " + target);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

            // 刪除臨時文件
            tempFile.delete();

            return "OK";
        } catch (IOException e) {
            log.error("Failed to write config file: " + CONFIG_FILE_PATH, e);
            return "寫入失敗";
        }
    }


}
