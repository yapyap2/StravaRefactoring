package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.Repository.RideBatchRepository;
import com.example.stravarefactoring.Repository.RideRepository;
import com.example.stravarefactoring.Repository.UserRepository;
import com.example.stravarefactoring.StravaApiClient;
import com.example.stravarefactoring.domain.*;
import com.example.stravarefactoring.exception.NoUpdateDataException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.transaction.Transactional;
import org.junit.Before;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

@SpringBootTest
public class StravaServiceBatchTest {

    StravaApiClient client = mock(StravaApiClient.class);

    @Autowired
    RideBatchRepository rideBatchRepository;
    @Autowired
    RideRepository rideRepository;

    @Autowired
    UserRepository userRepository;

    List<Ride> rideList;
    Token token;
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
        rideList = objectMapper.readValue(new File("src/main/resources/static/activities.json"), new TypeReference<List<Ride>>() {});

        user = new User(token, userInfo, userStatus);
    }

    @Test
    @Transactional
    public void batchInsertTest() throws NoUpdateDataException {
        userRepository.save(user);

        StravaService stravaService = new StravaService(client, rideBatchRepository, rideRepository);

        Answer<List<Ride>> answer = getAnswer();

        when(client.getRide(any(String.class), any(Integer.class))).thenAnswer(answer);

        List<Ride> list = stravaService.getRide(user);

        List<Ride> list2 = rideRepository.findAllByUserId(user.getId());


//        assertEquals(list.size(), list2.size());
    }


    private Answer<List<Ride>> getAnswer(){
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
        return answer;
    }


}
