package com.example.stravarefactoring.Service;

import com.example.stravarefactoring.Repository.RideRepository;
import com.example.stravarefactoring.Repository.UserRepository;
import com.example.stravarefactoring.domain.RankRide;
import com.example.stravarefactoring.domain.RankUser;
import com.example.stravarefactoring.domain.Ride;
import com.example.stravarefactoring.domain.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RankService {

    private final UserRepository userRepository;
    private final RideRepository rideRepository;

    private final EntityManager entityManager;

    public List<RankUser> getTop5Distance(){

        List<User> list = userRepository.top5Distance();

        List<RankUser> returnList = new ArrayList<>();
        list.forEach(u -> {
                    RankUser r = new RankUser(u);
                    r.setField(u.getTotalDistance());
                    returnList.add(r);
                    }
                );
        return returnList;
    }

    public List<RankUser> getTop5Elevation(){

        List<User> list = userRepository.top5Elevation();

        List<RankUser> returnList = new ArrayList<>();
        list.forEach(u -> {
                    RankUser r = new RankUser(u);
                    r.setField(u.getTotalElevation());
                    returnList.add(r);
                }
        );
        return returnList;
    }

    public List<RankUser> getTop5Climber(){
        List<User> list = userRepository.top5Climber();
        List<RankUser> returnList = new ArrayList<>();

        list.forEach(u ->{
            RankUser r = new RankUser(u);
            HashMap<String, Double> map = new HashMap<>();

            map.put("distance", u.getTotalDistance());
            map.put("elevation", u.getTotalElevation());
            r.setField(map);

            returnList.add(r);
        });

        return returnList;
    }

    public List<RankRide> getTop5DistanceRide(){

        List<Ride> list = rideRepository.getTop5Distance();
        List<RankRide> returnList = new ArrayList<>();

        list.forEach(r -> {
                RankRide rankRide = new RankRide(r);
                returnList.add(rankRide);
            }
        );
        return returnList;
    }

}
