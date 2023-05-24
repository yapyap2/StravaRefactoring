package com.example.stravarefactoring.Service;


import com.example.stravarefactoring.DTO.*;
import com.example.stravarefactoring.Repository.UserRepository;
import com.example.stravarefactoring.StravaApiClient;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    Token token;
    UserInfo userInfo;
    UserStatus userStatus;
    User user;
    @Mock
    StravaApiClient client;
    UserService userService;
    @Mock
    UserRepository userRepository;
    @Mock
    StravaService stravaService;

    List<Ride> rideList;
    List<Ride> beforeRidelist;
    List<Ride> afterRideList;

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

        rideList = objectMapper.readValue(new File("src/main/resources/static/activities.json"), new TypeReference<List<Ride>>() {});
        beforeRidelist = objectMapper.readValue(new File("src/main/resources/static/beforeActivity.json"), new TypeReference<List<Ride>>() {});
        afterRideList = objectMapper.readValue(new File("src/main/resources/static/afterActivity.json"), new TypeReference<List<Ride>>() {});

    }

    private void mocking(){
        when(client.getToken(anyString())).thenReturn(token);
        when(client.getUserInfo(any(Token.class))).thenReturn(userInfo);
        when(client.getUserStatus(any(Token.class))).thenReturn(userStatus);
        when(stravaService.getRide(any(User.class))).thenReturn(rideList);
    }

    private void makeUser(){
        user = new User(token);
        user.setUserStatus(userStatus);
        user.setUserInfo(userInfo);
    }

    @Test
    public void addNewUserTest(){
        mocking();

        userService.addUser(anyString());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        assertEquals(userInfo.getId(), captor.getValue().getId());
        assertFalse(captor.getValue().getRides().isEmpty());
    }

    @Test
    public void duplicatedUserNoUpdateTest(){

        mocking();
        makeUser();

        when(userRepository.findUserById(anyInt())).thenReturn(user);

        String modifiedName = "modifiedName";
        String modifiedBio = "modifiedBio";

        userInfo.setUpdate_at(LocalDateTime.now());
        userInfo.setBio(modifiedBio);
        userInfo.setName(modifiedName);

        when(client.getUserInfo(any(Token.class))).thenReturn(userInfo);
        when(stravaService.getRide(any(User.class))).thenThrow(new NoUpdateDataException("no update"));

        userService.addUser(anyString());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        verify(userRepository, times(1)).save(captor.capture());

        User modifiedUser = captor.getValue();

        assertEquals(modifiedUser.getBio(), modifiedBio);
        assertEquals(modifiedUser.getName(), modifiedName);

    }

}