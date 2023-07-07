package com.example.stravarefactoring.config;

import com.example.stravarefactoring.Repository.RideRepository;
import com.example.stravarefactoring.Repository.UserJDBCRepository;
import com.example.stravarefactoring.Repository.UserRepository;
import com.example.stravarefactoring.Service.LocationQueue;
import com.example.stravarefactoring.Service.ParallelLocationMapper;
import com.example.stravarefactoring.Service.StravaService;
import com.example.stravarefactoring.Service.UserService;
import com.example.stravarefactoring.StravaApiClient;
import com.example.stravarefactoring.TestKakaoApiClient;
import com.example.stravarefactoring.domain.Ride;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ComponentScan(basePackageClasses = StravaApiClient.class)
@Configuration
public class LocationQueueConfig {

    @Autowired
    StravaApiClient stravaApiClient;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RideRepository rideRepository;
    @Autowired
    StravaService stravaService;

    @Autowired
    TestKakaoApiClient testKakaoApiClient;

    ParallelLocationMapper mapper;

    @Autowired
    UserJDBCRepository userJDBCRepository;

    @Bean
    public ParallelLocationMapper mockMapper(){
        mapper = mock(ParallelLocationMapper.class);
        when(mapper.isAvailable()).thenReturn(true);
        return mapper;
    }

    public void setCount(int i){
        Answer<CompletableFuture<HashMap<String, Object>>> answer = new Answer<CompletableFuture<HashMap<String, Object>>>() {
            int count = 0;
            @Override
            public CompletableFuture<HashMap<String, Object>> answer(InvocationOnMock invocation) throws Throwable {
                List<Ride> arg = invocation.getArgument(0);
                HashMap<String, Object> map = new HashMap<>();
                if(count == 0) {
                    map.put("status", "finish");
                    map.put("result", Set.of("춘천", "홍천", "인제", "속초"));
                }
                else if(0 < count && count <= i){
                    map.put("status", "exception");
                    map.put("result", Set.of("춘천"));
                    map.put("remain", arg);
                }
                else{
                    map.put("status", "finish");
                    map.put("result", Set.of("춘천", "원주", "파주", "서울", String.valueOf(count)));
                }
                count += 1;
                return CompletableFuture.completedFuture(map);
            }
        };
        when(mapper.getLocation(anyList())).thenAnswer(answer);
    }

    @Bean
    public LocationQueue mockQueue(){
        LocationQueue locationQueue = new LocationQueue(mockMapper(), userJDBCRepository);

        return locationQueue;
    }

    @Bean
    public UserService userServiceForQueue(){
        return new UserService(rideRepository, userRepository, stravaApiClient, stravaService, mockMapper(), mockQueue(),userJDBCRepository);
    }

    @Bean
    public ParallelLocationMapper kakaoExceptionMapper(){
        return new ParallelLocationMapper(testKakaoApiClient);
    }
    @Bean
    public LocationQueue mockQueueKakao(){
        return new LocationQueue(kakaoExceptionMapper(), userJDBCRepository);
    }

    @Bean
    public UserService mockUserServiceKakao(){
        return new UserService(rideRepository ,userRepository,stravaApiClient,stravaService,kakaoExceptionMapper(),mockQueueKakao(), userJDBCRepository);
    }

}
