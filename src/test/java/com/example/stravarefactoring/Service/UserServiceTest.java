package com.example.stravarefactoring.Service;


import com.example.stravarefactoring.DTO.Token;
import com.example.stravarefactoring.DTO.User;
import com.example.stravarefactoring.DTO.UserInfo;
import com.example.stravarefactoring.DTO.UserStatus;
import com.example.stravarefactoring.Repository.UserRepository;
import com.example.stravarefactoring.StravaApiClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    Token token;
    UserInfo userInfo;
    UserStatus userStatus;
    @Mock
    StravaApiClient client;
    UserService userService;
    @Mock
    UserRepository userRepository;
    @Mock
    StravaService stravaService;

    @BeforeEach
    public void before() throws IOException {
        MockitoAnnotations.openMocks(this);

        userService = new UserService(userRepository, client, stravaService);


        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());

        token = objectMapper.readValue(new File("src/main/resources/static/json/Token.json"), Token.class);
        userInfo = objectMapper.readValue(new File("src/main/resources/static/json/UserInfo.json"), UserInfo.class);
        userStatus = objectMapper.readValue(new File("src/main/resources/static/json/UserStatus.json"), UserStatus.class);
    }

    private void mocking(){
        when(client.getToken(anyString())).thenReturn(token);
        when(client.getUserInfo(any(Token.class))).thenReturn(userInfo);
        when(client.getUserStatus(any(Token.class))).thenReturn(userStatus);
    }
    @Test
    public void userAddTest(){
        mocking();

        userService.addUser(anyString());

        ArgumentCaptor<User> userArgumentCaptor= ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userArgumentCaptor.capture());

        System.out.println(userArgumentCaptor.getValue());
    }
}