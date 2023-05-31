package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.StravaModifier;
import com.example.stravarefactoring.domain.*;
import com.example.stravarefactoring.StravaApiClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StravaServiceTestWithApi {
    StravaService stravaService = new StravaService(new StravaApiClient());
    StravaModifier stravaModifier = new StravaModifier();

    Token token;
    User user;
    @BeforeAll
    public void before() throws SQLException, ClassNotFoundException {
        token = stravaModifier.getToken();
        user = new User(token);
        user.setName("yapyap");
    }

    @Test
    public void rideGetTest(){
        List<Ride> rides = stravaService.getRide(user);

        rides.forEach(r -> System.out.println(r));
    }

    @Test
    public void updateRideTest(){

        LocalDateTime dateTime = LocalDateTime.now();
        dateTime = dateTime.minusSeconds(10);
        user.setLastUpdated(dateTime);

        stravaModifier.addRide(token);

        List<Ride> rides = stravaService.getRide(user);


        assertTrue(rides.get(rides.size() - 1).getStart_date_local().isAfter(dateTime));
    }


    @Test
    public void getRideExceptionTest(){

        user.setLastUpdated(LocalDateTime.now());

        Exception exception = assertThrows(NoUpdateDataException.class, () ->
                stravaService.getRide(user));

        exception.printStackTrace();
    }
}
