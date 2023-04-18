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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;




@SpringBootTest
public class StravaServiceTestWithSpring {
    @Autowired
    StravaService stravaService;
    @Autowired
    RideRepository rideRepository;
    @Autowired
    StravaApiClient client;

    Token token;
    List<Ride> beforeRideList;
    User user;
    UserStatus userStatus;
    UserInfo userInfo;
    @BeforeEach
    public void before() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());

        token = objectMapper.readValue(new File("src/main/resources/static/json/Token.json"), Token.class);
        userInfo = objectMapper.readValue(new File("src/main/resources/static/json/UserInfo.json"), UserInfo.class);
        userStatus = objectMapper.readValue(new File("src/main/resources/static/json/UserStatus.json"), UserStatus.class);

        beforeRideList = objectMapper.readValue(new File("src/main/resources/static/beforeActivity.json"), new TypeReference<List<Ride>>() {});

        user = new User(token);
        user.setUserInfo(userInfo);
        user.setUserStatus(userStatus);
        user.setAccessToken("7b6c8b4903efd7541d3d02b8fcd46bb4319ae13c");

    }

    @Test
    public void rideGetTest(){
        List<Ride> rides = stravaService.getRide(user);

        rides.forEach(r -> System.out.println(r));
    }

    @Test
    public void updateRideTest(){

        LocalDateTime dateTime = LocalDateTime.parse("2022-12-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        user.setLastUpdated(dateTime);

        List<Ride> rides = stravaService.getRide(user);


        assertTrue(rides.get(rides.size() - 1).getStart_date_local().isAfter(dateTime));
        assertTrue(user.getLastUpdated().isAfter(dateTime));

    }


    @Test
    public void getRideExceptionTest(){

        Ride ride = client.getOneRide(user.getAccessToken()).get(0);

        user.setLastUpdated(ride.getStart_date_local());

        Exception exception = assertThrows(NoUpdateDataException.class, () ->
                stravaService.getRide(user));

        exception.printStackTrace();
    }

}
