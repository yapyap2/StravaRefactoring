package com.example.stravarefactoring.Repository;

import com.example.stravarefactoring.StravaApiClient;
import com.example.stravarefactoring.StravaModifier;
import com.example.stravarefactoring.domain.Ride;
import com.example.stravarefactoring.domain.Token;
import com.example.stravarefactoring.domain.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;
import java.util.List;

import static org.testng.AssertJUnit.assertTrue;

@SpringBootTest
public class RideBatchRepositoryTest {

    @Autowired
    RideBatchRepository rideBatchRepository;

    StravaModifier stravaModifier = new StravaModifier();

    @Autowired
    StravaApiClient stravaApiClient;

    @Autowired
    RideRepository rideRepository;

    @Autowired
    UserRepository userRepository;


    @Test
    @Transactional
    public void updateTest() throws SQLException, ClassNotFoundException {

        Token token = stravaModifier.getToken(2);

        User user = new User(token);

        userRepository.save(user);

        List<Ride> rideList = stravaApiClient.getRide(token.getAccess_token(), 1);
        rideList.forEach(r ->
                r.setUser(user));

        List<Ride> returnRide = rideBatchRepository.saveAll(rideList);

        List<Ride> findRide = rideRepository.findAllByUserId(token.getId());

        assertTrue(rideList.size() == findRide.size());

        List<Ride> subList = returnRide.subList(0,50);

        subList.forEach(r ->
                r.setMapped(true));

        rideBatchRepository.batchUpdateRides(subList);


        List<Ride> findList2 = rideRepository.findAllByUserIdAndMappedTrue(token.getId());

        for(int i = 0; i <40; i++){
            Ride r1 = findList2.get(i);
            Ride r2 = rideList.get(i);

            assertTrue(r1.isMapped());
            assertTrue(r1.getName().equals(r2.getName()));

        }
    }

    @Test
    @Transactional
    public void batchInsertIdTest() throws SQLException, ClassNotFoundException {
        Token token = stravaModifier.getToken(2);

        User user = new User(token);

        userRepository.save(user);

        List<Ride> rideList = stravaApiClient.getRide(token.getAccess_token(), 1);
        rideList.forEach(r ->
                r.setUser(user));


        List<Ride> r1 = rideBatchRepository.saveAll(rideList);
        r1.forEach(r -> {
                    assertTrue(r.getId() != null);
                    assertTrue(r.getName().equals(rideRepository.findById(r.getId()).get().getName()));
                }
        );

        List<Ride> rideList2 = stravaApiClient.getRide(token.getAccess_token(), 2);
        rideList2.forEach(r ->
                r.setUser(user));

        List<Ride> r2 = rideBatchRepository.saveAll(rideList2);

        assertTrue(r2.get(0).getId() == 101);

        r2.forEach(r -> {
                    assertTrue(r.getId() != null);
                    assertTrue(r.getName().equals(rideRepository.findById(r.getId()).get().getName()));
                }
        );

        assertTrue(r2.get(r2.size() - 1).getId() == 200);
    }

    @Test  // ID가 200까지 들어가 있어야 함.
    public void idInitializeTest() throws SQLException, ClassNotFoundException {
        Token token = stravaModifier.getToken(2);

        User user = new User(token);

        userRepository.save(user);

        Ride r = stravaApiClient.getOneRide(token.getAccess_token()).get(0);
        r.setUser(user);

        List<Ride> l1 = rideBatchRepository.saveAll(List.of(r));

        Ride findRide = rideRepository.findById(l1.get(0).getId()).get();

        assertTrue(findRide.getId() == 201);
        assertTrue(r.getId() == 201);

    }
}
