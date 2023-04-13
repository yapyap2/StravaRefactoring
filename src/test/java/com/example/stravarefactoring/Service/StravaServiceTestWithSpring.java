package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.DTO.*;
import com.example.stravarefactoring.Repository.RideRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@SpringBootTest
public class StravaServiceTestWithSpring {
    @Autowired
    StravaService stravaService;
    @Autowired
    RideRepository rideRepository;

    Token token;
    List<Ride> rideList;
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
    }
    private void initializeUser(){
        user = new User(token);
        user.setUserInfo(userInfo);
        user.setUserStatus(userStatus);
    }
    @Test
    public void rideSeqAspectTest(){
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

        user.setAccessToken("d20bf028e40935e1447bb8db0d23974a8f455d04");


        List<Ride> rides = stravaService.getRide(user);

        rides.forEach(r -> System.out.println(r));

    }


}
