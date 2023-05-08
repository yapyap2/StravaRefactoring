package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.DTO.*;
import com.example.stravarefactoring.Repository.RideRepository;
import com.example.stravarefactoring.StravaApiClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

public class StravaServiceTest {

    RideRepository rideRepository;
    StravaService stravaService;
    StravaApiClient client;
    Token token;
    List<Ride> rideList;
    List<Ride> beforeRidelist;
    List<Ride> afterRideList;
    User user;
    UserStatus userStatus;
    UserInfo userInfo;

    @BeforeEach
    public void before() throws IOException {
        client = mock(StravaApiClient.class);
        rideRepository = mock(RideRepository.class);

        stravaService = new StravaService(client, rideRepository);


        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());

        token = objectMapper.readValue(new File("src/main/resources/static/json/Token.json"), Token.class);
        userInfo = objectMapper.readValue(new File("src/main/resources/static/json/UserInfo.json"), UserInfo.class);
        userStatus = objectMapper.readValue(new File("src/main/resources/static/json/UserStatus.json"), UserStatus.class);
        rideList = objectMapper.readValue(new File("src/main/resources/static/activities.json"), new TypeReference<List<Ride>>() {});
        beforeRidelist = objectMapper.readValue(new File("src/main/resources/static/beforeActivity.json"), new TypeReference<List<Ride>>() {});
        afterRideList = objectMapper.readValue(new File("src/main/resources/static/afterActivity.json"), new TypeReference<List<Ride>>() {});

    }

    private void initializeUser(){
        user = new User(token);
        user.setUserInfo(userInfo);
        user.setUserStatus(userStatus);
    }

    @Test
    public void getRideTest(){
        initializeUser();

        Answer<List<Ride>> answer = new Answer<List<Ride>>() {
            int count = 1;
            @Override
            public List<Ride> answer(InvocationOnMock invocation) throws Throwable {
                if(count == 1) {
                    count++;
                    return rideList;
                }
                else return new ArrayList<>();
            }
        };

        when(client.getRide(any(String.class), any(Integer.class))).thenAnswer(answer);

        List<Ride> rides = stravaService.getRide(user);


        assertEquals("size",11,  rides.size());
        assertEquals("name", "KNU 200 Brevet", rides.get(0).getName());
    }

    @Test
    public void updateRideTest(){
        initializeUser();

        user.setLastUpdated(beforeRidelist.get(0).getStart_date_local());

        Answer<List<Ride>> answer = new Answer<List<Ride>>() {
            int count = 1;
            @Override
            public List<Ride> answer(InvocationOnMock invocation) throws Throwable {
                if(count == 1) {
                    count++;
                    return afterRideList;
                }
                else return new ArrayList<>();
            }
        };

        when(client.getOneRide(any(String.class))).thenReturn(List.of(afterRideList.get(0)));
        when(client.getRideAfter(any(String.class), any(Integer.class), any(LocalDateTime.class))).thenAnswer(answer);

        List<Ride> saveList = stravaService.getRide(user);


        assertTrue("dateTime", saveList.get(saveList.size() - 1).getStart_date_local().isAfter(beforeRidelist.get(0).getStart_date_local()));
    }


}