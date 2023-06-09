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

        CompletableFuture<List<String>> future = target.processing(parameter, 1);
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

    @Test
    public void multiUserParallelTest() throws InterruptedException {
        ThreadPoolTaskExecutor taskExecutor = context.getBean("TestAsyncExecutor", ThreadPoolTaskExecutor.class);

        List<Integer> parameter1 = new ArrayList<>();

        for (int i = 1; i <= size; i++) {
            parameter1.add(i);
        }
        List<Integer> parameter2 = new ArrayList<>();
        for (int i = size + 1; i <= size * 2; i++) {
            parameter2.add(i);
        }
        List<Integer> parameter3 = new ArrayList<>();
        for (int i = size * 2 + 1; i <= size * 3; i++) {
            parameter3.add(i);
        }


        CompletableFuture<List<String>> result1 = target.processing(parameter1, 1);
        log.info("1   processing started....");
        CompletableFuture<List<String>> result2 = target.processing(parameter2, 2);
        log.info("2   processing started....");
        CompletableFuture<List<String>> result3 = target.processing(parameter3, 3);
        log.info("3   processing started....");


        taskExecutor.getThreadPoolExecutor().shutdown();
        taskExecutor.getThreadPoolExecutor().awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
    }


    @Test
    public void maxQueueTest() throws InterruptedException {
        ThreadPoolTaskExecutor taskExecutor = context.getBean("TestAsyncExecutor", ThreadPoolTaskExecutor.class);

        List<Integer> parameter1 = new ArrayList<>();

        for (int i = 1; i <= size; i++) {
            parameter1.add(i);
        }


        CompletableFuture<List<String>> result1 = target.processing(parameter1, 1);
        CompletableFuture<List<String>> result2 = target.processing(parameter1, 2);
        log.info("1, 2   processing started. now coreThread is full");

        CompletableFuture<List<String>> result3 = target.processing(parameter1, 3);
        log.info("3   processing started. now queue is full");

        target.processing(parameter1, 4);
        target.processing(parameter1, 5);
        log.info("4,5 processing started");


        taskExecutor.getThreadPoolExecutor().shutdown();
        taskExecutor.getThreadPoolExecutor().awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
    }


    @Test
    public void threadTimeOutTest() throws InterruptedException {
        ThreadPoolTaskExecutor taskExecutor = context.getBean("TestAsyncExecutor", ThreadPoolTaskExecutor.class);

        List<Integer> parameter1 = new ArrayList<>();

        for (int i = 1; i <= size; i++) {
            parameter1.add(i);
        }

        System.out.println(taskExecutor.getActiveCount() + "/" + taskExecutor.getCorePoolSize() + "   " + taskExecutor.getPoolSize() + "   " + taskExecutor.getQueueSize());

        target.processing(parameter1, 1);
        target.processing(parameter1, 2);

        System.out.println(taskExecutor.getActiveCount() + "/" + taskExecutor.getCorePoolSize() + "   " + taskExecutor.getPoolSize() + "   " + taskExecutor.getQueueSize());


        target.processing(parameter1, 3);

        System.out.println(taskExecutor.getActiveCount() + "/" + taskExecutor.getCorePoolSize() + "   " + taskExecutor.getPoolSize() + "   " + taskExecutor.getQueueSize());

        Thread.sleep(15000);

        System.out.println(taskExecutor.getActiveCount() + "/" + taskExecutor.getCorePoolSize() + "   " + taskExecutor.getPoolSize() + "   " + taskExecutor.getQueueSize());

        target.processing(parameter1, 4);

        System.out.println(taskExecutor.getActiveCount() + "/" + taskExecutor.getCorePoolSize() + "   " + taskExecutor.getPoolSize() + "   " + taskExecutor.getQueueSize());



        taskExecutor.getThreadPoolExecutor().shutdown();
        taskExecutor.getThreadPoolExecutor().awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
    }






}
