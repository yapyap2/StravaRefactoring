package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.StravaModifier;
import com.example.stravarefactoring.domain.Ride;
import com.example.stravarefactoring.domain.Token;
import com.example.stravarefactoring.domain.User;
import com.example.stravarefactoring.Repository.RideRepository;
import com.example.stravarefactoring.Repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserStravaIntergrateTest {
    Token token;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RideRepository rideRepository;

    WebClient webClient = WebClient.builder().build();

    StravaModifier stravaModifier;

    @BeforeAll
    public void getToken() throws SQLException, ClassNotFoundException {
        stravaModifier = new StravaModifier();
        token = stravaModifier.getToken();
    }


    @Test
    @Transactional
    public void newUserTest(){

        User user = userService.addUser(token);

        User findUser  = userRepository.findUserById(token.getId());

        assertTrue(findUser != null);
        assertTrue(user.getRides().size() == rideRepository.findAllByUserId(user.getId()).size());

    }

    @Transactional
    @Test
    public void duplicatedUserProfileUpdate(){

        User user1 = userService.addUser(token);

        Random r = new Random();
        double w = Math.round(r.nextDouble(80 - 60 - 1) + 60);
        stravaModifier.updateProfile(token, w);

        User user2 = userService.addUser(token);

        assertTrue(user2.getWeight() == w);
        assertTrue(user2.getWeight() != user1.getWeight());
        assertTrue(user1.getRides().size() == user2.getRides().size());
    }


    @Test
    @Transactional
    public void duplicatedUserRideUpdate(){
        User user1 = userService.addUser(token);

        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        stravaModifier.addRide(token);

        User user2 = userService.addUser(token);

        assertTrue(user2.getRides().size() == user1.getRides().size() + 1);
        assertTrue(user2.getLastUpdated().isAfter(user1.getLastUpdated()));
        List<Ride> findRide = rideRepository.findAllByUserId(user2.getId());
        assertTrue(rideRepository.findAllByUserId(user2.getId()).size() == user1.getRides().size() + 1);

    }


}
