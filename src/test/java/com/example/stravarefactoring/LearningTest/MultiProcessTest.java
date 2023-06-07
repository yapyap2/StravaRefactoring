package com.example.stravarefactoring.LearningTest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
public class MultiProcessTest {

    @Autowired
    AsyncTarget target;

    @Autowired
    ApplicationContext context;

    int size = 1234;
    @Test
    public void parallelTest() throws InterruptedException {
        ThreadPoolTaskExecutor taskExecutor = context.getBean("TestAsyncExecutor", ThreadPoolTaskExecutor.class);

        List<Integer> parameter = new ArrayList<>();

        for (int i = 1; i <= size; i++) {
            parameter.add(i);
        }

        CompletableFuture<List<String>> future = target.processing(parameter);
        log.info("processing started....");

        taskExecutor.getThreadPoolExecutor().shutdown();
        taskExecutor.getThreadPoolExecutor().awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);

        future.thenAccept(list -> list.forEach(i -> System.out.println(i)));
    }


    @Test
    public void linearProcessingTest() throws InterruptedException {
        ThreadPoolTaskExecutor taskExecutor = context.getBean("TestAsyncExecutor", ThreadPoolTaskExecutor.class);

        List<Integer> parameter = new ArrayList<>();

        for (int i = 0; i <= size; i++) {
            parameter.add(i);
        }

        CompletableFuture<List<String>> future = target.processing2(parameter);

        log.info("processing started....");

        taskExecutor.getThreadPoolExecutor().shutdown();
        taskExecutor.getThreadPoolExecutor().awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);

        future.thenAccept(list -> list.forEach(i -> System.out.println(i)));
    }

}
