package com.yid.agv.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutorOfReDispatch() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3); // 設定核心執行緒數量
        executor.setMaxPoolSize(5); // 設定最大執行緒數量
        executor.setQueueCapacity(10); // 設定等待佇列容量
        executor.setThreadNamePrefix("ReDispatchThread-"); // 設定執行緒名稱前綴
        executor.initialize(); // 初始化執行緒池
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutorOfSendCaller() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20); // 設定核心執行緒數量
        executor.setMaxPoolSize(100); // 設定最大執行緒數量
        executor.setQueueCapacity(200); // 設定等待佇列容量
        executor.setThreadNamePrefix("SendCallerThread-"); // 設定執行緒名稱前綴
        executor.initialize(); // 初始化執行緒池
        return executor;
    }
}
