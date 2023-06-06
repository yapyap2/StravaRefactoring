package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.Repository.RideBatchRepository;
import com.example.stravarefactoring.domain.*;
import com.example.stravarefactoring.Repository.RideRepository;
import com.example.stravarefactoring.StravaApiClient;
import com.example.stravarefactoring.exception.NoUpdateDataException;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

        stravaService = new StravaService(client,mock(RideBatchRepository.class), mock(RideRepository.class));


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
    public void getRideTest() throws NoUpdateDataException {
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


        assertEquals(11,  rides.size());
        assertEquals("KNU 200 Brevet", rides.get(0).getName());
    }

    @Test
    public void updateRideTest() throws NoUpdateDataException {
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


        assertTrue(saveList.get(saveList.size() - 1).getStart_date_local().isAfter(beforeRidelist.get(0).getStart_date_local()));
    }


//    @Test
//    public void rideSaveTest(){
//
//        initializeUser();
//        Answer<List<Ride>> beforeAnswer = new Answer<List<Ride>>() {
//            int count = 1;
//            @Override
//            public List<Ride> answer(InvocationOnMock invocation) throws Throwable {
//                if(count == 1) {
//                    count++;
//                    return beforeRidelist;
//                }
//                else return new ArrayList<>();
//            }
//        };
//        when(client.getRide(any(String.class), any(Integer.class))).thenAnswer(beforeAnswer);
//
//        stravaService.getRide(user);
//        ArgumentCaptor<List<Ride>> captor = ArgumentCaptor.forClass(List.class);
//
//        Answer<List<Ride>> afterAnswer = new Answer<List<Ride>>() {
//            int count = 1;
//            @Override
//            public List<Ride> answer(InvocationOnMock invocation) throws Throwable {
//                if(count == 1) {
//                    count++;
//                    return afterRideList;
//                }
//                else return new ArrayList<>();
//            }
//        };
//        when(client.getRideAfter(any(String.class), any(Integer.class), any(LocalDateTime.class))).thenAnswer(afterAnswer);
//        when(client.getOneRide(any(String.class))).thenReturn(List.of(afterRideList.get(0)));
//
//
//        stravaService.getRide(user);
//
//        verify(rideRepository, times(2)).saveAll(captor.capture());
//
//
//        List<Ride> before = captor.getAllValues().get(0);
//        List<Ride> after = captor.getAllValues().get(1);
//
//
//        assertTrue(before.get(0).getStart_date_local().isBefore(after.get(0).getStart_date_local()));
//        assertTrue(user.getUpdate_at().isAfter(before.get(0).getStart_date_local()));
//
//    }


}