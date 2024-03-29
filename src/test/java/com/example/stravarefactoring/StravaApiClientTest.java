package com.example.stravarefactoring;

import com.example.stravarefactoring.domain.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StravaApiClientTest {

    StravaApiClient client = new StravaApiClient();

    Token token;
    UserInfo userInfo;
    UserStatus userStatus;

    List<Ride> rideList;

    @BeforeEach
    public void before() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());

        token = objectMapper.readValue(new File("src/main/resources/static/json/Token.json"), Token.class);
        userInfo = objectMapper.readValue(new File("src/main/resources/static/json/UserInfo.json"), UserInfo.class);
        userStatus = objectMapper.readValue(new File("src/main/resources/static/json/UserStatus.json"), UserStatus.class);
        rideList = objectMapper.readValue(new File("src/main/resources/static/activities.json"), new TypeReference<List<Ride>>() {});
    }

    private void mocking(){
        this.client = mock(StravaApiClient.class);

        when(client.getToken(anyString())).thenReturn(token);
        when(client.getUserInfo(any(Token.class))).thenReturn(userInfo);
        when(client.getUserStatus(any(Token.class))).thenReturn(userStatus);
        when(client.getRide(any(String.class), any(Integer.class))).thenReturn(rideList);
    }

    @Test
    public void StravaApiClientTest(){

        Token token = client.getToken("94c1333f0a2070b91bfb09bf7cbfb2d865d5c1ff");
        User user = new User(token);

        user.setUserInfo(client.getUserInfo(token));
        user.setUserStatus(client.getUserStatus(token));

        System.out.println(user);
    }

    @Test
    public void mockingTest(){
        mocking();

        Token t = client.getToken("fake Code");

        User user = new User(t);
        user.setUserInfo(client.getUserInfo(t));
        user.setUserStatus(client.getUserStatus(t));


        System.out.println(user);
    }

    @Test
    public void getRideTest(){
        List<Ride> list = client.getRide("7b6c8b4903efd7541d3d02b8fcd46bb4319ae13c", 1);

        list.forEach(i -> System.out.println(i));
    }

    @Test
    public void getRideTestMocking(){
        mocking();

        List<Ride> list = client.getRide( "sdad", 23);

        System.out.println(list.size());
    }

    @Test
    public void updateRideTest(){

        String token = "7b6c8b4903efd7541d3d02b8fcd46bb4319ae13c";

        String time = "2023-02-01 00:00:00";
        LocalDateTime localDateTime = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        List<Ride> rides = client.getRideAfter(token, 1, localDateTime);

        rides.forEach(r -> System.out.println(r));
    }
}
