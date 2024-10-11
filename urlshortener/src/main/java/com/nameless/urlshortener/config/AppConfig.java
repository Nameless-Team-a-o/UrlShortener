package com.nameless.urlshortener.config;

import com.nameless.urlshortener.service.SnowflakeIdGenerator;
import org.springframework.context.annotation.Bean;

public class AppConfig {
    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator() {
        long workerId = 1; // Replace this with your desired worker ID
        return new SnowflakeIdGenerator(workerId);
    }
}
