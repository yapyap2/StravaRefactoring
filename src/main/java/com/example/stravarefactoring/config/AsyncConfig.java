package com.example.stravarefactoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean("MapperAsyncExecutor")
    public Executor customAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setThreadNamePrefix("mapper-");
        executor.initialize();
        return executor;
    }

    @Bean("TestAsyncExecutor")
    public Executor testAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setQueueCapacity(1);

        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds(5);

        executor.setThreadNamePrefix("test-");
        executor.initialize();
        return executor;
    }
}
