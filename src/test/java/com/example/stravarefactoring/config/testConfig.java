package com.example.stravarefactoring.config;

import com.example.stravarefactoring.Repository.RideBatchRepository;
import com.example.stravarefactoring.Repository.RideRepository;
import com.example.stravarefactoring.Repository.UserRepository;
import com.example.stravarefactoring.Service.LocationQueue;
import com.example.stravarefactoring.Service.ParallelLocationMapper;
import com.example.stravarefactoring.Service.StravaService;
import com.example.stravarefactoring.Service.UserService;
import com.example.stravarefactoring.StravaApiClient;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@ComponentScan(basePackageClasses = StravaApiClient.class)
public class testConfig {

    @Autowired
    StravaApiClient stravaApiClient;

    @Autowired
    RideBatchRepository rideBatchRepository;

    @Autowired
    RideRepository rideRepository;

    @Autowired
    StravaService stravaService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    @Qualifier("locationQueue")
    LocationQueue locationQueue;

    @Bean
    public UserService userServiceMockMapper(){
        ParallelLocationMapper mapper = mock(ParallelLocationMapper.class);
        Answer<CompletableFuture< HashSet<String>>> answer = new Answer<CompletableFuture<HashSet<String>>>() {
            @Override
            public CompletableFuture<HashSet<String>> answer(InvocationOnMock invocation) throws Throwable {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                thread.start();
                HashSet<String> set = new HashSet<>(Arrays.asList("test"));
                return CompletableFuture.completedFuture(set);
            }
        };

        when(mapper.getLocation(anyList())).thenAnswer(answer);

        return new UserService(userRepository, stravaApiClient, stravaService, mapper, locationQueue);
    }
}
