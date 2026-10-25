package com.example.supplysync_worker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AppConfig {
    @Value("${worker.pool-size:1}")
    private int poolSize;
    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(poolSize);
    }
}
