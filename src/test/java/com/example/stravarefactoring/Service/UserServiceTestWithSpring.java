package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.domain.*;
import com.example.stravarefactoring.Repository.RideRepository;
import com.example.stravarefactoring.Repository.UserRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
public class UserServiceTestWithSpring {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RideRepository rideRepository;

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
        user.setAccessToken("7d97761f8f8e12ab719756b31518324240fc7228");

        token.setAccess_token("fed72fddcad74f210653c0e99c38a268f388520e");
    }

    @Test
    public void addNewUserTest(){

        User user = userService.addUser(token);

        User getUser = userRepository.findUserById(user.getId());
        List<Ride> list = rideRepository.findAllByUserId(user.getId());

        assertEquals(user.getId(), getUser.getId());
        assertEquals(user.getRides().size(), list.size());

        System.out.println(list.get(0).toString());
    }
}
