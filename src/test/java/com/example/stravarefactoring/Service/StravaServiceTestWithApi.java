package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.Repository.RideBatchRepository;
import com.example.stravarefactoring.Repository.RideRepository;
import com.example.stravarefactoring.StravaModifier;
import com.example.stravarefactoring.domain.*;
import com.example.stravarefactoring.StravaApiClient;
import com.example.stravarefactoring.exception.NoUpdateDataException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StravaServiceTestWithApi {
    StravaService stravaService = new StravaService(new StravaApiClient(), mock(RideBatchRepository.class), mock(RideRepository.class));
    StravaModifier stravaModifier = new StravaModifier();

    Token token;
    User user;
    @BeforeAll
    public void before() throws SQLException, ClassNotFoundException {
        token = stravaModifier.getToken(1);
        user = new User(token);
        user.setName("yapyap");
    }

    @Test
    public void rideGetTest() throws NoUpdateDataException {
        List<Ride> rides = stravaService.getRide(user);

        rides.forEach(r -> System.out.println(r));
    }

    @Test
    public void updateRideTest() throws NoUpdateDataException {

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

        Exception exception = assertThrows(Exception.class, () ->
                stravaService.getRide(user));

        exception.printStackTrace();
    }
}
