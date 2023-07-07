package com.example.stravarefactoring.Repository;

import com.example.stravarefactoring.domain.Ride;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class RideRepositoryTest {

    @Autowired
    RideRepository rideRepository;
    @Test
    public void getTop5DistanceTest(){

        List<Ride> list = rideRepository.getTop5Distance();

        list.forEach(r -> System.out.println(r));
    }
}
