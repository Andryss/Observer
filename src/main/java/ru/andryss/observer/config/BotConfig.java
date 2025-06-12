package ru.andryss.observer.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    @Bean
    public ExecutorService updateExecutors() {
        return Executors.newSingleThreadExecutor();
    }
}
