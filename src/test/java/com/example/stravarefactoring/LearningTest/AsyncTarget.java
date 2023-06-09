package com.example.stravarefactoring.LearningTest;

import jakarta.persistence.Basic;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Component
public class AsyncTarget {

    Integer threadPoolSize = 10;
    Integer waitTime = 10;
    Double assignmentSize = 5.0;
    BasicThreadFactory factory = new BasicThreadFactory.Builder().namingPattern("ThreadTest-%d").build();
    ExecutorService service = Executors.newFixedThreadPool(threadPoolSize, factory);

    @Async("TestAsyncExecutor")
    public CompletableFuture<List<String>> processing(List<Integer> list, int number){
        log.info("{} processing start       {}", Thread.currentThread().getName(), number);


        double a = list.size() / assignmentSize;
        int segmentSize = (int) Math.ceil(a);

        List<Callable<List<String>>> callables = new ArrayList<>();

        for(int i = 0; i < list.size(); i+=segmentSize){
            List<Integer> subList;
            try{
                subList = list.subList(i, i + segmentSize);
            } catch (IndexOutOfBoundsException e){
                subList = list.subList(i, list.size());
            }

            ParameterStruct parameterStruct = new ParameterStruct();
            parameterStruct.setList(subList);
            parameterStruct.setName(subList.get(0).toString() + "~" + subList.get(subList.size()-1).toString());
            Callable<List<String>> callable = () -> multiProcessing(parameterStruct);
            callables.add(callable);
        }

        List<Future<List<String>>> futures;
        try {
            futures = service.invokeAll(callables);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<String> returnList = new ArrayList<>();
        for(Future<List<String>> future : futures){
            try {
                returnList.addAll(future.get());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        log.info("{} processing complete.   {}", Thread.currentThread().getName(), number);

        return CompletableFuture.completedFuture(returnList);
    }


    public List<String> multiProcessing(ParameterStruct parameterStruct){

        List<String> returnList = new ArrayList<>();
//        System.out.println("parameter "  + parameterStruct.getName() + " is processed in " + Thread.currentThread().getName());

        for(Integer i :parameterStruct.getList()){

            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            returnList.add("Processed  /// " + String.valueOf(i) + "  ///   " + Thread.currentThread().getName());
        }

//        log.info("{} is now available ", Thread.currentThread().getName());
        return returnList;
    }

    @Async("TestAsyncExecutor")
    public CompletableFuture<List<String>> processing2(List<Integer> list) throws InterruptedException {

        List<String> returnList = new ArrayList<>();

        for(int i : list){
            Thread.sleep(waitTime);
            returnList.add("Processed  /// " + String.valueOf(i) + "  ///   " + Thread.currentThread().getName());
        }
        return CompletableFuture.completedFuture(returnList);
     }


}
