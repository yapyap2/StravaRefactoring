package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.Repository.UserRepository;
import com.example.stravarefactoring.domain.Ride;
import com.example.stravarefactoring.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component("locationQueue")
@RequiredArgsConstructor
@Slf4j
public class LocationQueue {

    private Queue<HashMap<String, Object>> waitingQueue = new ConcurrentLinkedQueue<>();

    private final ParallelLocationMapper parallelLocationMapper;

    private final UserRepository userRepository;

    public void addQueue(HashMap<String, Object> map){
        waitingQueue.add(map);
        User user = (User) map.get("user");
        List list =(List) map.get("remain");
        log.info("location queue add.   userName: {}  remainRide: {}",user.getName(),  list.size());
    }

    @Scheduled(cron = "0 0 00 * * ?")
    public void scheduleProcessing(){
        log.info("scheduled mapping logic start.   queueSize: {}", waitingQueue.size());
        parallelLocationMapper.setAvailable(true);

        while(!waitingQueue.isEmpty() && parallelLocationMapper.isAvailable()){

            HashMap<String, Object> map = waitingQueue.poll();
            CompletableFuture<HashMap<String, Object>> future = parallelLocationMapper.getLocation((List<Ride>) map.get("remain"));
            User user = (User) map.get("user");
            future.thenAccept(result ->{
                if(result.get("status").equals("finish")){
                    user.getLocation().addAll((Collection<? extends String>) result.get("result"));
                    user.setLocationComplete(true);
                    log.info("userName : {}    add new Location {}",user.getName(), result.get("result"));
                    userRepository.save(user);
                }

                else{
                    user.getLocation().addAll((Collection<? extends String>) result.get("result"));
                    result.put("user", user);
                    addQueue(result);
                    userRepository.save(user);
                }
            });
        }
    }
}
