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
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

public class StravaServiceTest {

    StravaService stravaService = new StravaService();
    RideRepository rideRepository;
    StravaApiClient client;
    Token token;
    List<Ride> rideList;
    User user;
    UserStatus userStatus;
    UserInfo userInfo;
    @BeforeEach
    public void before() throws IOException {
        rideRepository = mock(RideRepository.class);
        client = mock(StravaApiClient.class);

        stravaService.setClient(client);
        stravaService.setRepository(rideRepository);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());

        token = objectMapper.readValue(new File("src/main/resources/static/json/Token.json"), Token.class);
        userInfo = objectMapper.readValue(new File("src/main/resources/static/json/UserInfo.json"), UserInfo.class);
        userStatus = objectMapper.readValue(new File("src/main/resources/static/json/UserStatus.json"), UserStatus.class);
        rideList = objectMapper.readValue(new File("src/main/resources/static/activities.json"), new TypeReference<List<Ride>>() {});
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

        stravaService.getRide(user);
        ArgumentCaptor<List<Ride>> captor = ArgumentCaptor.forClass(List.class);
        verify(rideRepository).saveAll(captor.capture());

        assertEquals("size", captor.getValue().size(), 30);
        assertEquals("name", captor.getValue().get(0).getName(), "KNU 200 Brevet");
    }
}
